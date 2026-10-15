package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.DischargeComorbidCondition;

public interface DischargeComorbidConditionRepository extends JpaRepository<DischargeComorbidCondition, Integer> {

	List<DischargeComorbidCondition> findByCaseDetail_CaseId(Integer caseId);

	void deleteByCaseDetail_CaseId(Integer caseId);
}
