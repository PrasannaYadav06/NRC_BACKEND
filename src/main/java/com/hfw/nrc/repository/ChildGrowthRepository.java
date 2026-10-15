package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.ChildGrowth;

public interface ChildGrowthRepository extends JpaRepository<ChildGrowth, Integer> {

	/**
	 * Ported verbatim from ChildGrowthDaoImpl#getLastFourMeasurements: de-dupes
	 * same-measurement_date rows (keeping the most-recently-created one), then
	 * takes the 4 most recently-inserted (by rec_id desc, not necessarily the
	 * 4 most recent by measurement_date). Returns raw columns
	 * [case_id, weight_kg, measurement_date] like the source, mapped in the
	 * service layer.
	 */
	@Query(value = "with cte as ("
			+ " select *, ROW_NUMBER() over (partition by measurement_date order by created_on desc) as rn"
			+ " from nr.child_growth where case_id = :caseId and height_cm is not null"
			+ ") "
			+ "select top 4 case_id, weight_kg, measurement_date "
			+ "from cte where rn = 1 "
			+ "order by rec_id desc", nativeQuery = true)
	List<Object[]> findLastFourMeasurements(@Param("caseId") Integer caseId);

	/** Matches the source's inline filter: recordedAtDischarge == true && heightCm != null. */
	List<ChildGrowth> findByCaseDetail_CaseIdAndRecordedAtDischargeTrueAndHeightCmIsNotNull(Integer caseId);

	ChildGrowth findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(Integer caseId);

	List<ChildGrowth> findByCaseDetail_CaseIdOrderByMeasurementDateDesc(Integer caseId);

	/** Ordered by rec_id (insertion order), not measurement_date - matches ChildGrowthDaoImpl#getAllDischargeGrowths exactly. */
	@Query(value = "select * from nr.child_growth where case_id = :caseId and recorded_at_discharge = 1 order by rec_id", nativeQuery = true)
	List<ChildGrowth> findDischargeGrowthsOrdered(@Param("caseId") Integer caseId);

	/** Ported from ChildGrowthDaoImpl#getChildGrowth - the "daily care" measurements (neither admission nor discharge). */
	List<ChildGrowth> findByCaseDetail_CaseIdAndRecordedAtAdmissionFalseAndRecordedAtDischargeFalseOrderByMeasurementDateDesc(Integer caseId);

	/** Ported from ChildGrowthDaoImpl#deleteMsmtList - only ever deletes "daily care" rows, never admission/discharge ones. */
	@Query(value = "delete from nr.child_growth where rec_id in (:ids) and recorded_at_admission = 0 and recorded_at_discharge = 0",
			nativeQuery = true)
	@Modifying
	int deleteDailyCareByIdIn(@Param("ids") List<Integer> ids);

	/** Ported from ChildGrowthDaoImpl#getCaseIdForChildGrowthId. */
	@Query(value = "select case_id from nr.child_growth where rec_id = :recId", nativeQuery = true)
	Integer findCaseIdByRecId(@Param("recId") Integer recId);

	/** Ported from NRCFollowupDaoImpl#deleteFollowupList's first delete statement. */
	@Query(value = "delete from nr.child_growth where followup_id in (:ids)", nativeQuery = true)
	@Modifying
	int deleteByFollowupIdIn(@Param("ids") List<Integer> ids);

	/** The child_growth row a given followup visit produced (source: followupInd.getChildGrowths().get(0)). */
	ChildGrowth findFirstByFollowup_FollowupId(Integer followupId);

	/**
	 * Backs the admin cascade-delete (nrcadmin.service.CaseDetailsService#
	 * deleteAllByCaseId) - unlike deleteDailyCareByIdIn above, this removes
	 * every growth row for the case (admission/discharge/daily-care alike),
	 * matching the source's own unscoped delete for this one admin flow.
	 */
	void deleteByCaseDetail_CaseId(Integer caseId);
}
