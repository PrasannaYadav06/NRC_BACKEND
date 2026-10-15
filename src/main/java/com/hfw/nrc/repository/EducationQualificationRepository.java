package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.EducationQualification;

public interface EducationQualificationRepository extends JpaRepository<EducationQualification, Integer> {

	@Query(value = "select * from config.education_qualification order by education_qualification_id asc", nativeQuery = true)
	List<EducationQualification> findAllOrdered();
}
