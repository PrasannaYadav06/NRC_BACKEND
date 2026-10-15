package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.Religion;

public interface ReligionRepository extends JpaRepository<Religion, Integer> {

	@Query(value = "select * from config.religion order by religion_id", nativeQuery = true)
	List<Religion> findAllOrdered();
}
