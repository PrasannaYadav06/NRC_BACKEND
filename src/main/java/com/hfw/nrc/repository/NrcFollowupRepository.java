package com.hfw.nrc.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcFollowup;

public interface NrcFollowupRepository extends JpaRepository<NrcFollowup, Integer> {

	List<NrcFollowup> findByCaseIdAndRecordTypeOrderByFollowupDateAsc(int caseId, String recordType);

	/**
	 * Monthly Report (NrcReportService#computeMonthlyReportDetail): every VISIT or SCHEDULE row at
	 * this hospital with followupDate in [start,end] - "Children followed up during the month"
	 * (VISIT) / "Children due for follow-up" (SCHEDULE). nrc_followup has no h_id column, joined
	 * through nrc_admission via case_id, same pattern as NrcDischargeRepository's hospital-scoped query.
	 */
	@Query(value = "select f.* from nr.nrc_followup f join nr.nrc_admission a on a.case_id = f.case_id "
			+ "where a.h_id = :hId and f.record_type = :recordType and f.followup_date between :start and :end",
			nativeQuery = true)
	List<NrcFollowup> findByHospitalAndRecordTypeAndFollowupDateBetween(@Param("hId") Integer hId,
			@Param("recordType") String recordType, @Param("start") LocalDate start, @Param("end") LocalDate end);

	/**
	 * Forecast page: VISIT-type follow-ups per calendar month in [start,end],
	 * optionally scoped to one district - same case_id join as
	 * findByHospitalAndRecordTypeAndFollowupDateBetween above, plus
	 * nr.hospital for the district filter. Each row is [year, month, count].
	 */
	@Query(value = "select year(f.followup_date) as yr, month(f.followup_date) as mo, count(*) as cnt "
			+ "from nr.nrc_followup f join nr.nrc_admission a on a.case_id = f.case_id join nr.hospital h on h.h_id = a.h_id "
			+ "where f.record_type = 'VISIT' and f.followup_date between :start and :end "
			+ "and (:districtId is null or h.district_id = :districtId) "
			+ "group by year(f.followup_date), month(f.followup_date)",
			nativeQuery = true)
	List<Object[]> countFollowupVisitsByMonth(@Param("districtId") Integer districtId,
			@Param("start") LocalDate start, @Param("end") LocalDate end);

	/**
	 * In-Patient Care dashboard: VISIT-type follow-ups per hospital in
	 * [start,end], across every hospital in scope in one query - same
	 * case_id join as findByHospitalAndRecordTypeAndFollowupDateBetween
	 * above. Each row is [h_id, count].
	 */
	@Query(value = "select a.h_id, count(*) from nr.nrc_followup f join nr.nrc_admission a on a.case_id = f.case_id "
			+ "where a.h_id in (:hIds) and f.record_type = 'VISIT' and f.followup_date between :start and :end "
			+ "group by a.h_id",
			nativeQuery = true)
	List<Object[]> countFollowupVisitsGroupedByHospital(@Param("hIds") List<Integer> hIds,
			@Param("start") LocalDate start, @Param("end") LocalDate end);
}
