package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.CaseMotherGrowth;

public interface CaseMotherGrowthRepository extends JpaRepository<CaseMotherGrowth, Integer> {

	CaseMotherGrowth findFirstByCaseDetail_CaseIdAndRecordedAtAdmissionTrue(Integer caseId);

	CaseMotherGrowth findFirstByCaseDetail_CaseIdAndRecordedAtDischargeTrue(Integer caseId);

	List<CaseMotherGrowth> findByCaseDetail_CaseId(Integer caseId);

	/** Backs the admin cascade-delete (nrcadmin.service.CaseDetailsService#deleteAllByCaseId). */
	void deleteByCaseDetail_CaseId(Integer caseId);
}
