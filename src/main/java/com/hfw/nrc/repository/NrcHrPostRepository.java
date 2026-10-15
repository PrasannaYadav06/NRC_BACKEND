package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcHrPost;

/**
 * Mirrors KabhiHrRepository's query method set, hId in place of districtId
 * (see NrcHrPost's javadoc).
 *
 * Every hId-touching finder here is an explicit @Query, not a derived
 * method name (e.g. NOT findByHId(int hId)) - Spring Data's property-path
 * parser derives "HId" (capital H, following the findBy-segment
 * capitalization convention) from a method name, then looks that EXACT
 * string up against Hibernate's JPA metamodel, which only has "hId" (the
 * real Java field name, lowercase h) - the mismatch throws
 * QueryCreationException at repository-bean-creation time (app startup),
 * not at compile time, so `mvn compile` never catches it. Found the hard
 * way: NrcHrRepository.findByHIdAndMonthAndYear had this exact bug and
 * silently prevented the whole app from starting. CaseDetailRepository
 * already avoids this same gotcha for its own h_id-touching queries (see
 * its own comment) - applying that same avoidance here.
 */
public interface NrcHrPostRepository extends JpaRepository<NrcHrPost, Integer> {

	@Query(value = "select * from nr.nrc_hr_post where h_id = :hId", nativeQuery = true)
	List<NrcHrPost> findByHId(@Param("hId") int hId);

	List<NrcHrPost> findByApprovalStatus(String approvalStatus);

	@Query(value = "select * from nr.nrc_hr_post where h_id = :hId and approval_status = :approvalStatus", nativeQuery = true)
	List<NrcHrPost> findByHIdAndApprovalStatus(@Param("hId") int hId, @Param("approvalStatus") String approvalStatus);

	@Query(value = "select * from nr.nrc_hr_post where h_id = :hId and designation = :designation and is_current = 1", nativeQuery = true)
	List<NrcHrPost> findByHIdAndDesignationAndIsCurrentTrue(@Param("hId") int hId, @Param("designation") String designation);

	@Query(value = "select * from nr.nrc_hr_post where h_id = :hId and designation = :designation", nativeQuery = true)
	List<NrcHrPost> findByHIdAndDesignation(@Param("hId") int hId, @Param("designation") String designation);
}
