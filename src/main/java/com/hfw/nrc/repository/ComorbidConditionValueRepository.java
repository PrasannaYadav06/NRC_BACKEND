package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.ComorbidConditionValue;

public interface ComorbidConditionValueRepository extends JpaRepository<ComorbidConditionValue, Integer> {

	@Query(value = "select * from config.comorbid_condition_values "
			+ "order by case when comorbid_id = 12 then 11 when comorbid_id = 11 then 12 end", nativeQuery = true)
	List<ComorbidConditionValue> findAllOrdered();

	/** Ported from NRCOperationDaoImpl#getOtherDischargeComordbids - options not yet selected for this case's discharge. */
	@Query(value = "select * from config.comorbid_condition_values where comorbid_id not in "
			+ "(select comorbid_id from nr.discharge_comorbid_condition where case_id = :caseId)", nativeQuery = true)
	List<ComorbidConditionValue> findNotYetSelectedForDischarge(@Param("caseId") Integer caseId);

	/** Ported from NRCOperationDaoImpl#getOtherComordbids - options not yet selected for this case's admission. */
	@Query(value = "select * from config.comorbid_condition_values where comorbid_id not in "
			+ "(select comorbid_id from nr.admission_comorbid_condition where case_id = :caseId)", nativeQuery = true)
	List<ComorbidConditionValue> findNotYetSelectedForAdmission(@Param("caseId") Integer caseId);
}
