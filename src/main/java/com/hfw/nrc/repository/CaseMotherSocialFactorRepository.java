package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.CaseMotherSocialFactor;

public interface CaseMotherSocialFactorRepository extends JpaRepository<CaseMotherSocialFactor, Integer> {

	List<CaseMotherSocialFactor> findByCaseDetail_CaseId(Integer caseId);

	void deleteByCaseDetail_CaseId(Integer caseId);
}
