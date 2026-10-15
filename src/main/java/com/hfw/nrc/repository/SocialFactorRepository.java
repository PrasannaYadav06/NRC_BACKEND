package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.SocialFactor;

public interface SocialFactorRepository extends JpaRepository<SocialFactor, Integer> {

	@Query(value = "select * from config.social_factors order by social_factors_id asc", nativeQuery = true)
	List<SocialFactor> findAllOrdered();

	/** Ported from NRCOperationDaoImpl#getOtherSocialFactors - options not yet selected for this case's mother. */
	@Query(value = "select * from config.social_factors where social_factors_id not in "
			+ "(select social_factors_id from nr.case_mother_social_factors where case_id = :caseId)", nativeQuery = true)
	List<SocialFactor> findNotYetSelectedForCase(@Param("caseId") Integer caseId);
}
