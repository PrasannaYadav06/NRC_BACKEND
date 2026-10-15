package com.hfw.nrc.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.hfw.nrc.domain.NrcAdmission;

/**
 * Multi-column ordering across the full result set - see
 * NrcAdmissionRepositoryCustom's javadoc for why this can't be a plain
 * {@code @Query}. Shared by both In-Patient Care (NrcAdmissionService
 * #listAdmissions) and Follow-up (FollowupEventService#listFollowups),
 * same as the {@code search()} method this parallels - both list nr.
 * nrc_admission rows, just with a different formStatus bucket.
 */
@Repository
public class NrcAdmissionRepositoryImpl implements NrcAdmissionRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Frontend sort key -> real SQL column. ONLY these exact keys are ever
	 * turned into SQL text - anything else in the incoming `sort` string is
	 * silently dropped (see buildOrderBy), so this is the actual injection
	 * defense, not the `sort` param's shape. statusLabel sorts by the raw
	 * form_status decimal it's derived from (same ordering, no extra join
	 * needed); samNo/contactNo/dischargeDate/lastFollowupDate are only
	 * meaningful for Follow-up's columns, harmless no-ops for In-Patient Care.
	 */
	private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<>();
	static {
		SORT_COLUMNS.put("uhId", "a.uh_id");
		SORT_COLUMNS.put("childName", "a.child_name");
		SORT_COLUMNS.put("childGender", "a.child_gender");
		SORT_COLUMNS.put("targetWeight", "a.target_weight");
		SORT_COLUMNS.put("admissionDate", "a.admission_date");
		SORT_COLUMNS.put("lastMeasuredWeight", "a.last_measured_weight");
		SORT_COLUMNS.put("lastMeasuredDate", "a.last_measured_date");
		SORT_COLUMNS.put("statusLabel", "a.form_status");
		SORT_COLUMNS.put("samNo", "a.sam_num");
		SORT_COLUMNS.put("contactNo", "a.child_mobile_number");
		SORT_COLUMNS.put("dischargeDate", "a.last_discharge_date");
		SORT_COLUMNS.put("lastFollowupDate", "a.last_followup_date");
		// Next Follow-up No's/Dates aren't real nr.nrc_admission columns - each
		// row can carry up to 4 scheduled checkpoints (see
		// FollowupEventService#generateSchedule) - so both sort by the same
		// underlying value: the soonest still-active scheduled date for that
		// case, i.e. "who's due next". Two separate keys (rather than one
		// shared between both headers) so each column's own sort-arrow/
		// priority indicator only lights up for the header actually clicked.
		String nextScheduleDate = "(select min(f.followup_date) from nr.nrc_followup f "
				+ "where f.case_id = a.case_id and f.record_type = 'SCHEDULE' and f.is_active = 1)";
		SORT_COLUMNS.put("nextFollowupNos", nextScheduleDate);
		SORT_COLUMNS.put("nextFollowupDates", nextScheduleDate);
	}

	@Override
	public Page<NrcAdmission> searchSorted(Integer hId, String search, Integer month, Integer year,
			BigDecimal minStatus, BigDecimal maxStatus, Integer districtId, Integer talukaId,
			Integer requireCaptures, String sort, Pageable pageable) {

		String where = "(:hId is null or a.h_id = :hId) "
				+ "and (:search is null or :search = '' or a.child_name like concat('%',:search,'%') "
				+ "or cast(a.uh_id as varchar) like concat('%',:search,'%') or cast(a.sam_num as varchar) like concat('%',:search,'%')) "
				+ "and (:month is null or MONTH(a.admission_date) = :month) "
				+ "and (:year is null or YEAR(a.admission_date) = :year) "
				+ "and (:minStatus is null or a.form_status >= :minStatus) "
				+ "and (:maxStatus is null or a.form_status < :maxStatus) "
				+ "and (:districtId is null or h.district_id = :districtId) "
				+ "and (:talukaId is null or h.taluka_id = :talukaId) "
				+ "and (:requireCaptures is null "
				+ "     or (:requireCaptures = 1 and exists (select 1 from nr.nrc_weight_capture wc where wc.case_id = a.case_id)) "
				+ "     or (:requireCaptures = 0 and not exists (select 1 from nr.nrc_weight_capture wc where wc.case_id = a.case_id)))";

		String orderBy = buildOrderBy(sort);

		String dataSql = "select a.* from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id where " + where
				+ " order by " + orderBy + " offset :offset rows fetch next :limit rows only";
		String countSql = "select count(*) from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id where " + where;

		Query dataQuery = entityManager.createNativeQuery(dataSql, NrcAdmission.class);
		Query countQuery = entityManager.createNativeQuery(countSql);
		bindParams(dataQuery, hId, search, month, year, minStatus, maxStatus, districtId, talukaId, requireCaptures);
		bindParams(countQuery, hId, search, month, year, minStatus, maxStatus, districtId, talukaId, requireCaptures);
		dataQuery.setParameter("offset", pageable.getOffset());
		dataQuery.setParameter("limit", pageable.getPageSize());

		@SuppressWarnings("unchecked")
		List<NrcAdmission> content = dataQuery.getResultList();
		long total = ((Number) countQuery.getSingleResult()).longValue();

		return new PageImpl<>(content, pageable, total);
	}

	private void bindParams(Query query, Integer hId, String search, Integer month, Integer year,
			BigDecimal minStatus, BigDecimal maxStatus, Integer districtId, Integer talukaId, Integer requireCaptures) {
		query.setParameter("hId", hId);
		query.setParameter("search", search);
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("minStatus", minStatus);
		query.setParameter("maxStatus", maxStatus);
		query.setParameter("districtId", districtId);
		query.setParameter("talukaId", talukaId);
		query.setParameter("requireCaptures", requireCaptures);
	}

	/**
	 * "childName:asc,admissionDate:desc" -> "a.child_name asc, a.admission_date desc, a.case_id desc".
	 * Only whitelisted keys are ever concatenated; unknown keys/malformed
	 * parts are silently skipped (never trusted as raw SQL). Always ends
	 * with case_id desc as a deterministic tiebreaker - without one, rows
	 * with equal sort values could shuffle between pages as data changes,
	 * same reasoning the original hardcoded "order by a.case_id desc" had.
	 */
	private String buildOrderBy(String sort) {
		List<String> clauses = new ArrayList<>();
		if (sort != null && !sort.isEmpty()) {
			for (String part : sort.split(",")) {
				String[] kv = part.split(":");
				if (kv.length != 2) {
					continue;
				}
				String column = SORT_COLUMNS.get(kv[0].trim());
				if (column == null) {
					continue;
				}
				String direction = "desc".equalsIgnoreCase(kv[1].trim()) ? "desc" : "asc";
				clauses.add(column + " " + direction);
			}
		}
		clauses.add("a.case_id desc");
		return String.join(", ", clauses);
	}
}
