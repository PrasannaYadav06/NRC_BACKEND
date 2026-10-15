package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.MedicalRisk;

public interface MedicalRiskRepository extends JpaRepository<MedicalRisk, Integer> {

	@Query(value = "select * from config.medical_risk order by medical_risk_id", nativeQuery = true)
	List<MedicalRisk> findAllOrdered();
}
