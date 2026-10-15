package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.CaseDetail;

public interface CaseDetailRepository extends JpaRepository<CaseDetail, Integer> {

	@Query(value = "select * from nr.case_details where discharge_date is null and admission_date is not null "
			+ "and h_id = :hospitalId order by admission_date desc", nativeQuery = true)
	List<CaseDetail> findInpatientsForHospital(@Param("hospitalId") Integer hospitalId);

	/**
	 * Dashboard card support: in-patients (currently admitted, not discharged)
	 * for the given reporting month/year, grouped by hospital_level. When
	 * hospitalId is null (admin, "all hospitals") this returns up to 2 rows
	 * (DISTRICT/TALUKA totals); when hospitalId is given (a single hospital
	 * user) it returns at most 1 row scoped to just that hospital - same
	 * query serves both dashboard shapes, see DashboardService.
	 */
	@Query(value = "select h.hospital_level, count(*) from nr.case_details c "
			+ "inner join nr.hospital h on h.h_id = c.h_id "
			+ "where c.discharge_date is null and c.admission_date is not null "
			+ "and month(c.admission_date) = :month and year(c.admission_date) = :year "
			+ "and (:hospitalId is null or c.h_id = :hospitalId) "
			+ "group by h.hospital_level", nativeQuery = true)
	List<Object[]> countInpatientsByLevel(@Param("hospitalId") Integer hospitalId,
			@Param("month") Integer month, @Param("year") Integer year);

	/** Same shape as countInpatientsByLevel, for discharged cases in the reporting period. */
	@Query(value = "select h.hospital_level, count(*) from nr.case_details c "
			+ "inner join nr.hospital h on h.h_id = c.h_id "
			+ "where c.discharge_date is not null "
			+ "and month(c.discharge_date) = :month and year(c.discharge_date) = :year "
			+ "and (:hospitalId is null or c.h_id = :hospitalId) "
			+ "group by h.hospital_level", nativeQuery = true)
	List<Object[]> countDischargesByLevel(@Param("hospitalId") Integer hospitalId,
			@Param("month") Integer month, @Param("year") Integer year);

	@Query(value = "select * from nr.case_details where admission_date between :startDate and :endDate "
			+ "and h_id = :hospitalId order by admission_date desc", nativeQuery = true)
	List<CaseDetail> findAdmittedForHospitalBetween(@Param("hospitalId") Integer hospitalId,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	@Query(value = "select * from nr.case_details where discharge_date is not null "
			+ "and discharge_date between :startDate and :endDate and h_id = :hospitalId order by discharge_date desc",
			nativeQuery = true)
	List<CaseDetail> findDischargedForHospitalBetween(@Param("hospitalId") Integer hospitalId,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	/**
	 * Stage 3: new admissions land in nr.nrc_admission, not nr.case_details -
	 * both are checked (UNION) so the SAM-number suggestion/uniqueness check
	 * still accounts for every admission, old and new. case_details keeps its
	 * full pre-migration history; nrc_admission holds the migrated copy of
	 * that same history plus everything created after Stage 3 shipped.
	 */
	@Query(value = "select max(sam_num) from (" +
			"select sam_num from nr.case_details where h_id = :hospitalId " +
			"union all select sam_num from nr.nrc_admission where h_id = :hospitalId) x", nativeQuery = true)
	Integer findMaxSamInHospital(@Param("hospitalId") Integer hospitalId);

	@Query(value = "select case when exists (" +
			"select 1 from nr.case_details where sam_num = :samNumber and h_id = :hospitalId " +
			"union all select 1 from nr.nrc_admission where sam_num = :samNumber and h_id = :hospitalId) " +
			"then 1 else 0 end", nativeQuery = true)
	Integer countSamExists(@Param("samNumber") Integer samNumber, @Param("hospitalId") Integer hospitalId);

	/**
	 * Deliberate native query, not a derived findByCaseIdAndHospital_HId -
	 * Spring Data's property-path resolver can't reliably parse "HId" back to
	 * the hId field (the classic single-lowercase-then-uppercase acronym
	 * pitfall), so it's spelled out explicitly instead.
	 */
	@Query(value = "select * from nr.case_details where case_id = :caseId and h_id = :hospitalId", nativeQuery = true)
	CaseDetail findByCaseIdAndHospitalId(@Param("caseId") Integer caseId, @Param("hospitalId") Integer hospitalId);

	/**
	 * Ported VERBATIM from NRCOperationDaoImpl#checkDischarged, including its
	 * join bug: "dc.case_id = dc.case_id" is a self-comparison (always true),
	 * not "dc.case_id = cd.case_id" - it degenerates into an implicit cross
	 * join scoped by "cd.h_id = :hospitalId" (true for any hospital with any
	 * case at all) rather than actually correlating the two tables by hospital.
	 * In practice this still correctly detects "does case X have a discharge
	 * record" since dc.case_id = :caseId is still applied - the h_id filter is
	 * just not doing real scoping work. Not fixed, per faithful-port instruction.
	 */
	@Query(value = "select 1 from nr.discharge_child_details dc inner join nr.case_details cd on dc.case_id = dc.case_id "
			+ "where dc.case_id = :caseId and cd.h_id = :hospitalId", nativeQuery = true)
	List<Integer> checkDischargedRaw(@Param("caseId") Integer caseId, @Param("hospitalId") Integer hospitalId);

	/** Ported verbatim from NRCOperationDaoImpl#getTargetStatus. */
	@Query(value = "select * from nr.case_details where case_id = ("
			+ " select cd.case_id from nr.case_details cd"
			+ " inner join nr.child_growth cg on cd.case_id = cg.case_id"
			+ " where cg.recorded_at_discharge = 1 and height_cm is not null and cd.case_id = :caseId"
			+ " and cg.weight_kg >= cd.target_weight)", nativeQuery = true)
	CaseDetail findTargetStatus(@Param("caseId") Integer caseId);

	/**
	 * Ported from NRCOperationDaoImpl#getCaseForSameChild - used to carry
	 * follow-up continuity across re-admissions. Source has no ORDER BY and
	 * just takes list.get(0) if multiple match; returning a List here (not a
	 * single CaseDetail) for the same reason - a single-result query method
	 * would throw if more than one row comes back, where the source silently
	 * picks the first.
	 */
	@Query(value = "select * from nr.case_details where child_id = :childId and admission_type_id = 1", nativeQuery = true)
	List<CaseDetail> findNewAdmissionsForChild(@Param("childId") Integer childId);

	/**
	 * Ported from NRCOperationDaoImpl#getAllDischargedPatientsForHospital(Integer)
	 * - backs the one-time /setTargetAchieved/{userId} batch fixup, unscoped by
	 * date range (every discharged case in the hospital, ever).
	 */
	@Query(value = "select * from nr.case_details where discharge_date is not null and h_id = :hospitalId "
			+ "order by discharge_date desc", nativeQuery = true)
	List<CaseDetail> findAllDischargedForHospital(@Param("hospitalId") Integer hospitalId);

	/**
	 * Ported verbatim from nrcadmin.repository.CaseDetailsRepository#
	 * findCaseDetailsBysamNo - deliberately NOT scoped by hospital, unlike
	 * every other patient-lookup query in this project. This backs
	 * nrcadmin's admin-only "find/delete by SAM number" tool
	 * (GET/DELETE /caseDetails/{..}), which is meant to search across every
	 * hospital, not just the caller's own - consistent with it already
	 * being gated to admins only (see HospitalAdminController's precedent).
	 */
	@Query(value = "select * from nr.case_details where sam_num = :samNum", nativeQuery = true)
	List<CaseDetail> findBySamNum(@Param("samNum") Integer samNum);
}
