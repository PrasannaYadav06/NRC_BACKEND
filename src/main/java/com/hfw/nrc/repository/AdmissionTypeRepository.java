package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.AdmissionType;

public interface AdmissionTypeRepository extends JpaRepository<AdmissionType, Integer> {

	// is_active excludes "Relapse" (deactivated via V20 migration, not
	// deleted - historical rows may still reference its admission_type_id).
	@Query(value = "select * from config.admission_type where is_active = 1 order by admission_type_id", nativeQuery = true)
	List<AdmissionType> findAllOrdered();
}
