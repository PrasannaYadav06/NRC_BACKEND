package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.Followup;

public interface FollowupRepository extends JpaRepository<Followup, Integer> {

	@Query(value = "select top 1 * from nr.followup where case_id = :caseId order by followup_date desc", nativeQuery = true)
	Followup findLastForCase(@Param("caseId") Integer caseId);

	List<Followup> findByCaseDetail_CaseIdOrderByFollowupDateDesc(Integer caseId);

	@Query(value = "select * from nr.followup where case_id = :caseId and followup_date = :date", nativeQuery = true)
	List<Followup> findByCaseIdAndDate(@Param("caseId") Integer caseId, @Param("date") String date);

	@Query(value = "delete from nr.followup where followup_id in (:ids)", nativeQuery = true)
	@Modifying
	int deleteByIdIn(@Param("ids") List<Integer> ids);

	@Query(value = "select case_id from nr.followup where case_id in (:caseIds) and status_id = 4", nativeQuery = true)
	List<Integer> findCaseIdsWithStatus4(@Param("caseIds") List<Integer> caseIds);

	/** Backs the admin cascade-delete (nrcadmin.service.CaseDetailsService#deleteAllByCaseId). */
	void deleteByCaseDetail_CaseId(Integer caseId);

	/**
	 * Dashboard card support: distinct patients with a follow-up visit in the
	 * given reporting month/year, grouped by hospital_level - same
	 * null-hospitalId-means-all-hospitals shape as CaseDetailRepository's
	 * countInpatientsByLevel/countDischargesByLevel.
	 */
	@Query(value = "select h.hospital_level, count(distinct f.case_id) from nr.followup f "
			+ "inner join nr.case_details c on c.case_id = f.case_id "
			+ "inner join nr.hospital h on h.h_id = c.h_id "
			+ "where month(f.followup_date) = :month and year(f.followup_date) = :year "
			+ "and (:hospitalId is null or c.h_id = :hospitalId) "
			+ "group by h.hospital_level", nativeQuery = true)
	List<Object[]> countFollowupPatientsByLevel(@Param("hospitalId") Integer hospitalId,
			@Param("month") Integer month, @Param("year") Integer year);
}
