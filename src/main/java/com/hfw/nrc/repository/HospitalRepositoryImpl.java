package com.hfw.nrc.repository;

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

import com.hfw.nrc.domain.Hospital;

@Repository
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	/** Frontend sort key -> real SQL column, only these 3 - see HospitalRepositoryCustom's javadoc for why the other 4 columns aren't here. */
	private static final Map<String, String> SORT_COLUMNS = new LinkedHashMap<>();
	static {
		SORT_COLUMNS.put("hCode", "h.h_code");
		SORT_COLUMNS.put("name", "h.name");
		SORT_COLUMNS.put("nameShort", "h.name_short");
	}

	@Override
	public Page<Hospital> searchSorted(String search, Integer districtId, Integer talukaId, String sort, Pageable pageable) {
		String where = "(:search is null or :search = '' or h.name like concat('%', :search, '%') "
				+ "or h.h_code like concat('%', :search, '%') or h.name_short like concat('%', :search, '%')) "
				+ "and (:districtId is null or h.district_id = :districtId) "
				+ "and (:talukaId is null or h.taluka_id = :talukaId)";

		String orderBy = buildOrderBy(sort);

		String dataSql = "select h.* from nr.hospital h where " + where
				+ " order by " + orderBy + " offset :offset rows fetch next :limit rows only";
		String countSql = "select count(*) from nr.hospital h where " + where;

		Query dataQuery = entityManager.createNativeQuery(dataSql, Hospital.class);
		Query countQuery = entityManager.createNativeQuery(countSql);
		dataQuery.setParameter("search", search);
		dataQuery.setParameter("districtId", districtId);
		dataQuery.setParameter("talukaId", talukaId);
		countQuery.setParameter("search", search);
		countQuery.setParameter("districtId", districtId);
		countQuery.setParameter("talukaId", talukaId);
		dataQuery.setParameter("offset", pageable.getOffset());
		dataQuery.setParameter("limit", pageable.getPageSize());

		@SuppressWarnings("unchecked")
		List<Hospital> content = dataQuery.getResultList();
		long total = ((Number) countQuery.getSingleResult()).longValue();

		return new PageImpl<>(content, pageable, total);
	}

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
		clauses.add("h.h_id desc");
		return String.join(", ", clauses);
	}
}
