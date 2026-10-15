package com.hfw.nrc.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.Rcho;

public interface RchoRepository extends JpaRepository<Rcho, Integer> {

	Optional<Rcho> findByDistrictId(int districtId);

	/**
	 * Derived findByUId(...) doesn't resolve to field uId via JavaBean
	 * decapitalize rules (same pitfall as findByCaseIdAndHospital_HId - see
	 * [[nrc-backend-migration]] memory) - Spring Data looks for a JPA
	 * attribute literally named "UId", not "uId". Explicit query instead.
	 */
	@Query("select r from Rcho r where r.uId = :uId")
	Optional<Rcho> findByUId(@Param("uId") int uId);

	/** "Rcho Data Table" list - search matches RCHO name or district name. */
	@Query(value = "select r.* from nr.rcho r join config.district d on r.district_id = d.district_id "
			+ "where (:search is null or :search = '' "
			+ "or r.rcho_name like concat('%', :search, '%') or d.district_name like concat('%', :search, '%')) "
			+ "order by d.district_name",
			countQuery = "select count(*) from nr.rcho r join config.district d on r.district_id = d.district_id "
			+ "where (:search is null or :search = '' "
			+ "or r.rcho_name like concat('%', :search, '%') or d.district_name like concat('%', :search, '%'))",
			nativeQuery = true)
	Page<Rcho> search(@Param("search") String search, Pageable pageable);
}
