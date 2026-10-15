package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.MalnutritionStatus;

public interface MalnutritionStatusRepository extends JpaRepository<MalnutritionStatus, Integer> {

	@Query(value = "select * from config.malnutrition_status order by status_id asc", nativeQuery = true)
	List<MalnutritionStatus> findAllOrdered();
}
