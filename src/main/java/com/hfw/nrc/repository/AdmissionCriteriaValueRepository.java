package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.AdmissionCriteriaValue;

public interface AdmissionCriteriaValueRepository extends JpaRepository<AdmissionCriteriaValue, Integer> {

	@Query(value = "select * from config.admission_criteria_values "
			+ "order by case "
			+ "when criteria_id = 8 then 3 "
			+ "when criteria_id = 9 then 4 "
			+ "when criteria_id = 3 then 5 "
			+ "when criteria_id = 7 then 6 "
			+ "when criteria_id = 4 then 7 "
			+ "when criteria_id = 5 then 8 "
			+ "when criteria_id = 6 then 9 "
			+ "end", nativeQuery = true)
	List<AdmissionCriteriaValue> findAllOrdered();

	/** Ported from NRCOperationDaoImpl#getOtherAdmissionCriteria - options not yet selected for this case's admission. */
	@Query(value = "select * from config.admission_criteria_values where criteria_id not in "
			+ "(select criteria_id from nr.admission_criteria where case_id = :caseId)", nativeQuery = true)
	List<AdmissionCriteriaValue> findNotYetSelectedForCase(@Param("caseId") Integer caseId);
}
