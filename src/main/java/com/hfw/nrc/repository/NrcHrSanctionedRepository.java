package com.hfw.nrc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcHrSanctioned;

/** Explicit @Query, not a derived findByHIdAndDesignation - see NrcHrPostRepository's javadoc for why. */
public interface NrcHrSanctionedRepository extends JpaRepository<NrcHrSanctioned, Integer> {

	@Query(value = "select * from nr.nrc_hr_sanctioned where h_id = :hId and designation = :designation", nativeQuery = true)
	Optional<NrcHrSanctioned> findByHIdAndDesignation(@Param("hId") int hId, @Param("designation") String designation);
}
