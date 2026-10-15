package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.Caste;

public interface CasteRepository extends JpaRepository<Caste, Integer> {

	@Query(value = "select * from config.caste order by caste_id", nativeQuery = true)
	List<Caste> findAllOrdered();
}
