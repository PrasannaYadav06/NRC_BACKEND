package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hfw.nrc.domain.Chc;

public interface ChcRepository extends JpaRepository<Chc, Integer> {

	List<Chc> findByTalukaIdOrderByChcNameAsc(Integer talukaId);
}
