package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.FollowupSchedule;

public interface FollowupScheduleRepository extends JpaRepository<FollowupSchedule, Integer> {

	List<FollowupSchedule> findByCaseDetail_CaseIdOrderByFollowupDateDesc(Integer caseId);

	/** Ported from NRCFollowupDaoImpl#getAllFollowUpSchedule. */
	@Query(value = "select fs.* from nr.followup_schedule fs "
			+ "inner join nr.case_details cd on cd.case_id = fs.case_id "
			+ "where is_active = 1 and cd.h_id = :hospitalId order by cd.case_id", nativeQuery = true)
	List<FollowupSchedule> findActiveForHospital(@Param("hospitalId") Integer hospitalId);

	/** Ported from NRCFollowupDaoImpl#getfilteredFSchedule. */
	@Query(value = "select fs.* from nr.followup_schedule fs "
			+ "inner join nr.case_details cd on cd.case_id = fs.case_id "
			+ "where is_active = 1 and fs.case_id not in (:caseIds) and cd.h_id = :hospitalId order by cd.case_id",
			nativeQuery = true)
	List<FollowupSchedule> findActiveForHospitalExcludingCaseIds(@Param("hospitalId") Integer hospitalId,
			@Param("caseIds") List<Integer> caseIds);

	/** Backs the admin cascade-delete (nrcadmin.service.CaseDetailsService#deleteAllByCaseId). */
	void deleteByCaseDetail_CaseId(Integer caseId);
}
