package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hfw.nrc.domain.RiskCategory;

public interface RiskCategoryRepository extends JpaRepository<RiskCategory, Integer> {

	@Query(value = "select * from config.risk_category order by risk_category_id asc", nativeQuery = true)
	List<RiskCategory> findAllOrdered();
}
