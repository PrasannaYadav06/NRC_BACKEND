package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.Ward;

public interface WardRepository extends JpaRepository<Ward, Integer> {

	@Query(value = "select c.ward_id, c.h_id, c.ward, c.description, c.created_on, c.last_updated_on from "
			+ "(select 1 as order_id, ward_id, h_id, ward, description, created_on, last_updated_on from config.ward "
			+ "where h_id = :hospitalId and ward = 'SAM Ward' "
			+ "union "
			+ "select 2 as order_id, ward_id, h_id, ward, description, created_on, last_updated_on from config.ward "
			+ "where h_id = :hospitalId and ward = 'PICU Ward' "
			+ "union "
			+ "select 3 as order_id, ward_id, h_id, ward, description, created_on, last_updated_on from config.ward "
			+ "where h_id = :hospitalId and ward = 'COVID Ward' "
			+ "union "
			+ "select 4 as order_id, ward_id, h_id, ward, description, created_on, last_updated_on from config.ward "
			+ "where h_id = :hospitalId and ward = 'SARI Ward' "
			+ "union "
			+ "select 5 as order_id, ward_id, h_id, ward, description, created_on, last_updated_on from config.ward "
			+ "where h_id = :hospitalId and ward = 'Others') c "
			+ "order by order_id", nativeQuery = true)
	List<Ward> findByHospitalIdOrdered(@Param("hospitalId") Integer hospitalId);
}
