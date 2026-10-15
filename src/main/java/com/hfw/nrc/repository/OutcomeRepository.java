package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.Outcome;

public interface OutcomeRepository extends JpaRepository<Outcome, Integer> {

	@Query(value = "select * from config.outcome order by outcome_id", nativeQuery = true)
	List<Outcome> findAllOrdered();
}
