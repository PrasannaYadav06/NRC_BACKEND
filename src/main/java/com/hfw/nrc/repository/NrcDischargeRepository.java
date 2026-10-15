package com.hfw.nrc.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcDischarge;

public interface NrcDischargeRepository extends JpaRepository<NrcDischarge, Integer> {

	List<NrcDischarge> findByCaseIdOrderByDischargeDateAsc(int caseId);

	/** Latest discharge event for a case - what the "already discharged, View or Edit?" interstitial and the Edit form both need. */
	Optional<NrcDischarge> findFirstByCaseIdOrderByDischargeRecIdDesc(int caseId);

	/** District/Taluka report (NrcReportService): every discharge event at this hospital within [start,end], joined through nrc_admission for h_id since nrc_discharge itself has no hospital column. */
	@Query(value = "select d.* from nr.nrc_discharge d join nr.nrc_admission a on a.case_id = d.case_id "
			+ "where a.h_id = :hId and d.discharge_date between :start and :end",
			nativeQuery = true)
	List<NrcDischarge> findByHospitalAndDischargeDateBetween(@Param("hId") Integer hId, @Param("start") Timestamp start, @Param("end") Timestamp end);

	/**
	 * NRC Analytics (NrcAnalyticsService#admissionDischarge): one grouped query for every hospital
	 * in scope instead of a per-hospital findByHospitalAndDischargeDateBetween call - see the same
	 * rationale on NrcAdmissionRepository#countAdmissionsGroupedByHospital. Each row is [h_id, count].
	 */
	@Query(value = "select a.h_id, count(*) from nr.nrc_discharge d join nr.nrc_admission a on a.case_id = d.case_id "
			+ "where a.h_id in (:hIds) and d.discharge_date between :start and :end group by a.h_id",
			nativeQuery = true)
	List<Object[]> countDischargesGroupedByHospital(@Param("hIds") List<Integer> hIds,
			@Param("start") Timestamp start, @Param("end") Timestamp end);

	/**
	 * NRC Analytics (NrcAnalyticsService#outcomeDistribution): one grouped query for every hospital
	 * in scope instead of a per-hospital discharge-list fetch - same N+1 rationale as above. Each
	 * row is [h_id, outcome_id, count].
	 */
	@Query(value = "select a.h_id, d.outcome_id, count(*) from nr.nrc_discharge d join nr.nrc_admission a on a.case_id = d.case_id "
			+ "where a.h_id in (:hIds) and d.discharge_date between :start and :end group by a.h_id, d.outcome_id",
			nativeQuery = true)
	List<Object[]> countOutcomesGroupedByHospitalAndOutcome(@Param("hIds") List<Integer> hIds,
			@Param("start") Timestamp start, @Param("end") Timestamp end);

	/**
	 * Forecast page: discharges per calendar month in [start,end], optionally
	 * scoped to one district (same nrc_admission/nr.hospital join as every
	 * other discharge query here). Each row is [year, month, count].
	 */
	@Query(value = "select year(d.discharge_date) as yr, month(d.discharge_date) as mo, count(*) as cnt "
			+ "from nr.nrc_discharge d join nr.nrc_admission a on a.case_id = d.case_id join nr.hospital h on h.h_id = a.h_id "
			+ "where d.discharge_date between :start and :end "
			+ "and (:districtId is null or h.district_id = :districtId) "
			+ "group by year(d.discharge_date), month(d.discharge_date)",
			nativeQuery = true)
	List<Object[]> countDischargesByMonth(@Param("districtId") Integer districtId,
			@Param("start") Timestamp start, @Param("end") Timestamp end);
}
