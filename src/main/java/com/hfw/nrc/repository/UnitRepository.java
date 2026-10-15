package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.Unit;

public interface UnitRepository extends JpaRepository<Unit, Integer> {

	@Query(value = "select * from config.unit where h_id = :hospitalId order by unit asc", nativeQuery = true)
	List<Unit> findByHospitalIdOrdered(@Param("hospitalId") Integer hospitalId);
}
