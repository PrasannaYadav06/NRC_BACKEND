package com.hfw.nrc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcInfrastructure;

public interface NrcInfrastructureRepository extends JpaRepository<NrcInfrastructure, Integer> {

	/**
	 * A derived findByHId(int) doesn't parse - Spring Data's property-path
	 * splitter can't resolve "HId" back to the entity's hId field, same
	 * reason every hId-keyed query elsewhere in this project (e.g.
	 * NrcAdmissionRepository) is already an explicit @Query rather than
	 * method-name-derived. Confirmed live: this exact method crashed
	 * context startup with "Unable to locate Attribute HId" before being
	 * rewritten here.
	 */
	@Query("select i from NrcInfrastructure i where i.hId = :hId")
	Optional<NrcInfrastructure> findByHId(@Param("hId") int hId);
}
