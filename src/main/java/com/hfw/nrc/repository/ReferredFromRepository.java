package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.ReferredFrom;

public interface ReferredFromRepository extends JpaRepository<ReferredFrom, Integer> {

	@Query(value = "select * from config.referred_from "
			+ "order by case referred_from_id when 8 then 11 else referred_from_id end asc", nativeQuery = true)
	List<ReferredFrom> findAllOrdered();
}
