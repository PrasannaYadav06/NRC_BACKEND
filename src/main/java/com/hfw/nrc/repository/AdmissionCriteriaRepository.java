package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.AdmissionCriteria;

public interface AdmissionCriteriaRepository extends JpaRepository<AdmissionCriteria, Integer> {

	List<AdmissionCriteria> findByCaseDetail_CaseId(Integer caseId);

	void deleteByCaseDetail_CaseId(Integer caseId);
}
