package com.hfw.nrc.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcAdmission;

public interface NrcAdmissionRepository extends JpaRepository<NrcAdmission, Integer>, NrcAdmissionRepositoryCustom {

	/**
	 * Backs the In-Patient Care Records table (Stage 3): hId null = admin
	 * sees every hospital, otherwise scoped to the caller's own hospital
	 * (SecurityUtils.resolveHospitalId, same pattern as Stage 2's hospital
	 * search). search matches child name / UHID / SAM no; month+year filter
	 * admissionDate; minStatus/maxStatus bound formStatus (service layer
	 * translates the UI's "New/Monitoring/Discharged/Followup" dropdown into
	 * this numeric range - see NrcAdmissionService). districtId/talukaId
	 * (item 3) join through nr.hospital - only meaningful for an admin
	 * caller browsing every hospital; a hospital-user's own hId already
	 * pins them to one row so the join is a no-op filter for them.
	 */
	@Query(value = "select a.* from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id where "
			+ "(:hId is null or a.h_id = :hId) "
			+ "and (:search is null or :search = '' or a.child_name like concat('%',:search,'%') "
			+ "or cast(a.uh_id as varchar) like concat('%',:search,'%') or cast(a.sam_num as varchar) like concat('%',:search,'%')) "
			+ "and (:month is null or MONTH(a.admission_date) = :month) "
			+ "and (:year is null or YEAR(a.admission_date) = :year) "
			+ "and (:minStatus is null or a.form_status >= :minStatus) "
			+ "and (:maxStatus is null or a.form_status < :maxStatus) "
			+ "and (:districtId is null or h.district_id = :districtId) "
			+ "and (:talukaId is null or h.taluka_id = :talukaId) "
			+ "order by a.case_id desc",
			countQuery = "select count(*) from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id where "
			+ "(:hId is null or a.h_id = :hId) "
			+ "and (:search is null or :search = '' or a.child_name like concat('%',:search,'%') "
			+ "or cast(a.uh_id as varchar) like concat('%',:search,'%') or cast(a.sam_num as varchar) like concat('%',:search,'%')) "
			+ "and (:month is null or MONTH(a.admission_date) = :month) "
			+ "and (:year is null or YEAR(a.admission_date) = :year) "
			+ "and (:minStatus is null or a.form_status >= :minStatus) "
			+ "and (:maxStatus is null or a.form_status < :maxStatus) "
			+ "and (:districtId is null or h.district_id = :districtId) "
			+ "and (:talukaId is null or h.taluka_id = :talukaId)",
			nativeQuery = true)
	Page<NrcAdmission> search(@Param("hId") Integer hId, @Param("search") String search,
			@Param("month") Integer month, @Param("year") Integer year,
			@Param("minStatus") BigDecimal minStatus, @Param("maxStatus") BigDecimal maxStatus,
			@Param("districtId") Integer districtId, @Param("talukaId") Integer talukaId,
			Pageable pageable);

	/**
	 * Item 13 - Search Patient page's search bar. Broader than the list's own
	 * `search` (child name/UHID/SAM only) - also matches child/mother RCH ID,
	 * mother name, and child mobile number, since a walk-in/phone inquiry is
	 * as likely to give one of those as the UHID. Unpaged callers pass a
	 * small Pageable (see NrcAdmissionService#searchPatients) - this is a
	 * picker list, not the full records table.
	 */
	@Query(value = "select a.* from nr.nrc_admission a where "
			+ "(:hId is null or a.h_id = :hId) "
			+ "and (a.child_name like concat('%',:search,'%') "
			+ "or a.mother_name like concat('%',:search,'%') "
			+ "or cast(a.uh_id as varchar) like concat('%',:search,'%') "
			+ "or cast(a.sam_num as varchar) like concat('%',:search,'%') "
			+ "or cast(a.child_rch_id as varchar) like concat('%',:search,'%') "
			+ "or cast(a.mother_rch_id as varchar) like concat('%',:search,'%') "
			+ "or a.child_mobile_number like concat('%',:search,'%')) "
			+ "order by a.case_id desc",
			countQuery = "select count(*) from nr.nrc_admission a where "
			+ "(:hId is null or a.h_id = :hId) "
			+ "and (a.child_name like concat('%',:search,'%') "
			+ "or a.mother_name like concat('%',:search,'%') "
			+ "or cast(a.uh_id as varchar) like concat('%',:search,'%') "
			+ "or cast(a.sam_num as varchar) like concat('%',:search,'%') "
			+ "or cast(a.child_rch_id as varchar) like concat('%',:search,'%') "
			+ "or cast(a.mother_rch_id as varchar) like concat('%',:search,'%') "
			+ "or a.child_mobile_number like concat('%',:search,'%'))",
			nativeQuery = true)
	Page<NrcAdmission> searchForPatientLookup(@Param("hId") Integer hId, @Param("search") String search, Pageable pageable);

	/** Dashboard "In-Patient Care" card (Stage 1 repoint): not yet discharged (formStatus &lt; 4), by hospital level. */
	@Query(value = "select h.hospital_level, count(*) from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id "
			+ "where a.form_status < 4 and (:hospitalId is null or a.h_id = :hospitalId) "
			+ "and (:month is null or MONTH(a.admission_date) = :month) and (:year is null or YEAR(a.admission_date) = :year) "
			+ "group by h.hospital_level", nativeQuery = true)
	List<Object[]> countInpatientsByLevel(@Param("hospitalId") Integer hospitalId, @Param("month") Integer month, @Param("year") Integer year);

	/** Dashboard "Discharge" card (Stage 1 repoint): formStatus &gt;= 4, by hospital level. */
	@Query(value = "select h.hospital_level, count(*) from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id "
			+ "where a.form_status >= 4 and (:hospitalId is null or a.h_id = :hospitalId) "
			+ "and (:month is null or MONTH(a.last_discharge_date) = :month) and (:year is null or YEAR(a.last_discharge_date) = :year) "
			+ "group by h.hospital_level", nativeQuery = true)
	List<Object[]> countDischargesByLevel(@Param("hospitalId") Integer hospitalId, @Param("month") Integer month, @Param("year") Integer year);

	/**
	 * District/Taluka report (NrcReportService): every admission opened at this hospital within
	 * [start,end]. A derived findByHIdAnd... method name doesn't parse - Spring Data's property-path
	 * splitter can't resolve "HId" back to the hId field, same reason every other query in this
	 * repository is already an explicit @Query rather than method-name-derived.
	 */
	@Query(value = "select * from nr.nrc_admission where h_id = :hId and admission_date between :start and :end",
			nativeQuery = true)
	List<NrcAdmission> findByHIdAndAdmissionDateBetween(@Param("hId") Integer hId,
			@Param("start") java.sql.Timestamp start, @Param("end") java.sql.Timestamp end);

	/**
	 * District/Taluka report's Bed Occupancy Rate (NrcReportService): every admission at this
	 * hospital that was "active" (occupying a bed) at any point within [start,end] - either opened
	 * inside the window, or opened before it and not yet discharged (or discharged on/after start).
	 * Direct port of old nrcapp's nr.bedoccupancyrate stored procedure's own WHERE clause, rewritten
	 * against NrcAdmission's denormalized last_discharge_date instead of joining nrc_discharge.
	 */
	@Query(value = "select * from nr.nrc_admission a where a.h_id = :hId "
			+ "and a.admission_date <= :end "
			+ "and (a.last_discharge_date is null or a.last_discharge_date >= :start)",
			nativeQuery = true)
	List<NrcAdmission> findActiveDuringPeriod(@Param("hId") Integer hId, @Param("start") java.sql.Timestamp start, @Param("end") java.sql.Timestamp end);

	/**
	 * NRC Analytics (NrcAnalyticsService#admissionDischarge): one grouped query for every hospital
	 * in scope instead of a per-hospital findByHIdAndAdmissionDateBetween call - the per-hospital
	 * loop was ~150 round trips for an ADMIN's all-districts view, the actual cause of the "charts
	 * take a long time to load" complaint. Each row is [h_id, count].
	 */
	@Query(value = "select h_id, count(*) from nr.nrc_admission where h_id in (:hIds) "
			+ "and admission_date between :start and :end group by h_id",
			nativeQuery = true)
	List<Object[]> countAdmissionsGroupedByHospital(@Param("hIds") List<Integer> hIds,
			@Param("start") java.sql.Timestamp start, @Param("end") java.sql.Timestamp end);

	/**
	 * NRC Analytics Bed Occupancy (NrcAnalyticsService#bedOccupancy): bulk
	 * equivalent of NrcReportService#computeBedOccupancyRate's per-admission
	 * patient-days loop, across every hospital in scope in one query instead
	 * of one computeHospitalReportRow() call (itself several queries plus a
	 * full in-memory report-row build) per hospital - the main cause of
	 * "Bed Occupancy is slow". :start/:end are the SAME start-of-day/
	 * end-of-day timestamps every other query here uses (NOT plain dates) -
	 * the active-period WHERE filter below is byte-for-byte
	 * findActiveDuringPeriod's own clause, needing the full end-of-day
	 * timestamp so same-day late admissions aren't dropped. The per-row
	 * clamp/DATEDIFF math then works in CAST(...AS DATE) terms, matching
	 * the original Java code's .toLocalDate()-only day arithmetic exactly:
	 * effectiveStart = greatest(admission_date, start-date), admEnd =
	 * least(last_discharge_date or end-date, end-date), days = DATEDIFF+1
	 * when positive else 0. Each row is [h_id, patientDays].
	 */
	@Query(value = "select h_id, sum( "
			+ "case when datediff(day, "
			+ "  case when cast(admission_date as date) < cast(:start as date) then cast(:start as date) else cast(admission_date as date) end, "
			+ "  case when last_discharge_date is null then cast(:end as date) "
			+ "       when cast(last_discharge_date as date) > cast(:end as date) then cast(:end as date) "
			+ "       else cast(last_discharge_date as date) end "
			+ ") + 1 > 0 then datediff(day, "
			+ "  case when cast(admission_date as date) < cast(:start as date) then cast(:start as date) else cast(admission_date as date) end, "
			+ "  case when last_discharge_date is null then cast(:end as date) "
			+ "       when cast(last_discharge_date as date) > cast(:end as date) then cast(:end as date) "
			+ "       else cast(last_discharge_date as date) end "
			+ ") + 1 else 0 end "
			+ ") as patientDays "
			+ "from nr.nrc_admission "
			+ "where h_id in (:hIds) and admission_date <= :end "
			+ "and (last_discharge_date is null or last_discharge_date >= :start) "
			+ "group by h_id",
			nativeQuery = true)
	List<Object[]> sumPatientDaysGroupedByHospital(@Param("hIds") List<Integer> hIds,
			@Param("start") java.sql.Timestamp start, @Param("end") java.sql.Timestamp end);

	/**
	 * Karnataka Facilities page: admissions ("Treated") per facility tier in
	 * [start,end] - same MEDICAL_COLLEGE/DISTRICT/TALUKA/CHC/PHC bucketing as
	 * HospitalRepository#countFacilitiesByTier, joined against the
	 * admitting hospital's own flag/level/chc_id/phc_id. Each row is [tier, count].
	 */
	@Query(value = "select "
			+ "case when h.is_medical_college = 1 then 'MEDICAL_COLLEGE' "
			+ "     when h.hospital_level = 'DISTRICT' then 'DISTRICT' "
			+ "     when h.chc_id is not null then 'CHC' "
			+ "     when h.phc_id is not null then 'PHC' "
			+ "     else 'TALUKA' end as tier, "
			+ "count(*) as cnt "
			+ "from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id "
			+ "where a.admission_date between :start and :end "
			+ "group by case when h.is_medical_college = 1 then 'MEDICAL_COLLEGE' "
			+ "     when h.hospital_level = 'DISTRICT' then 'DISTRICT' "
			+ "     when h.chc_id is not null then 'CHC' "
			+ "     when h.phc_id is not null then 'PHC' "
			+ "     else 'TALUKA' end",
			nativeQuery = true)
	List<Object[]> countAdmissionsByFacilityTier(@Param("start") java.sql.Timestamp start,
			@Param("end") java.sql.Timestamp end);

	/**
	 * NRC Analytics In-Patient Care (NrcAnalyticsService#inPatientCare): how
	 * many admissions are "in in-patient care" as of :end - same
	 * active-during-period WHERE clause as findActiveDuringPeriod/
	 * sumPatientDaysGroupedByHospital (admitted on/before :end, and either
	 * still open or discharged on/after :start), grouped across every
	 * hospital in scope in one query instead of a per-hospital loop. Each
	 * row is [h_id, count].
	 */
	@Query(value = "select h_id, count(*) from nr.nrc_admission where h_id in (:hIds) "
			+ "and admission_date <= :end and (last_discharge_date is null or last_discharge_date >= :start) "
			+ "group by h_id",
			nativeQuery = true)
	List<Object[]> countActiveAdmissionsGroupedByHospital(@Param("hIds") List<Integer> hIds,
			@Param("start") java.sql.Timestamp start, @Param("end") java.sql.Timestamp end);

	/**
	 * Forecast page: admissions per calendar month in [start,end], optionally
	 * scoped to one district (joins nr.hospital - districtId null = every
	 * district). Each row is [year, month, count].
	 */
	@Query(value = "select year(a.admission_date) as yr, month(a.admission_date) as mo, count(*) as cnt "
			+ "from nr.nrc_admission a join nr.hospital h on h.h_id = a.h_id "
			+ "where a.admission_date between :start and :end "
			+ "and (:districtId is null or h.district_id = :districtId) "
			+ "group by year(a.admission_date), month(a.admission_date)",
			nativeQuery = true)
	List<Object[]> countAdmissionsByMonth(@Param("districtId") Integer districtId,
			@Param("start") java.sql.Timestamp start, @Param("end") java.sql.Timestamp end);

	/**
	 * In-Patient Care dashboard's "Total Treated (Live)" line - every
	 * admission ever recorded at each hospital, no date filter at all
	 * (deliberately ignores the reporting-period filter the rest of the
	 * dashboard uses - this is a running lifetime total, computed fresh on
	 * every call, not cached). Each row is [h_id, count].
	 */
	@Query(value = "select h_id, count(*) from nr.nrc_admission where h_id in (:hIds) group by h_id",
			nativeQuery = true)
	List<Object[]> countAllTimeAdmissionsGroupedByHospital(@Param("hIds") List<Integer> hIds);
}
