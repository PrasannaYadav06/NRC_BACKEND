package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.Phc;

public interface PhcRepository extends JpaRepository<Phc, Integer> {

	List<Phc> findByTalukaIdOrderByPhcNameAsc(Integer talukaId);
}
