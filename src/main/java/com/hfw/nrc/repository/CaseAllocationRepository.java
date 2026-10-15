package com.hfw.nrc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.CaseAllocation;

public interface CaseAllocationRepository extends JpaRepository<CaseAllocation, Integer> {

	CaseAllocation findFirstByCaseDetail_CaseId(Integer caseId);

	/** Backs the admin cascade-delete (nrcadmin.service.CaseDetailsService#deleteAllByCaseId). */
	void deleteByCaseDetail_CaseId(Integer caseId);
}
