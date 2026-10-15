package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.AdmissionComorbidCondition;

public interface AdmissionComorbidConditionRepository extends JpaRepository<AdmissionComorbidCondition, Integer> {

	List<AdmissionComorbidCondition> findByCaseDetail_CaseId(Integer caseId);

	void deleteByCaseDetail_CaseId(Integer caseId);
}
