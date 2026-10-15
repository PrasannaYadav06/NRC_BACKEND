package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.Taluka;

public interface TalukaRepository extends JpaRepository<Taluka, Integer> {

	List<Taluka> findByDistrictIdOrderByTalukaNameAsc(Integer districtId);
}
