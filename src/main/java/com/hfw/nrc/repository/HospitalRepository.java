package com.hfw.nrc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.Hospital;

public interface HospitalRepository extends JpaRepository<Hospital, Integer>, HospitalRepositoryCustom {

	/** Ported verbatim from nrcadmin.repository.HospitalRepository#findLatestHospital. */
	@Query(value = "SELECT TOP 1 * FROM nr.hospital ORDER BY created_on DESC", nativeQuery = true)
	Optional<Hospital> findLatestHospital();

	/**
	 * Dashboard "Hospitals" card: total registered hospitals by level. Admin
	 * sees the true state-wide split (DISTRICT count, TALUKA count); this
	 * card doesn't render for a non-admin user at all (see spec), so there's
	 * no hospitalId-scoped variant needed here unlike the other dashboard
	 * queries.
	 */
	@Query(value = "select hospital_level, count(*) from nr.hospital where hospital_level is not null group by hospital_level",
			nativeQuery = true)
	List<Object[]> countHospitalsByLevel();

	@Query(value = "select hospital_level from nr.hospital where h_id = :hospitalId", nativeQuery = true)
	String findHospitalLevel(@Param("hospitalId") Integer hospitalId);

	/**
	 * Karnataka Facilities page: facility count per tier. NRC only has 2 real
	 * hospital_level values (DISTRICT/TALUKA) - "CHC"/"PHC" here are TALUKA
	 * hospitals additionally registered under a CHC/PHC (chc_id/phc_id,
	 * V26 - mutually exclusive, nullable); a TALUKA hospital registered
	 * under neither stays in the plain "TALUKA" bucket. "MEDICAL_COLLEGE" is
	 * a DISTRICT-level hospital explicitly flagged is_medical_college=1
	 * (V36 - backfilled from the hospital-credentials Excel, not inferred
	 * from name at query time), checked before the plain DISTRICT bucket so
	 * it doesn't also get double-counted there. Each row is [tier, count].
	 */
	@Query(value = "select "
			+ "case when is_medical_college = 1 then 'MEDICAL_COLLEGE' "
			+ "     when hospital_level = 'DISTRICT' then 'DISTRICT' "
			+ "     when chc_id is not null then 'CHC' "
			+ "     when phc_id is not null then 'PHC' "
			+ "     else 'TALUKA' end as tier, "
			+ "count(*) as cnt "
			+ "from nr.hospital "
			+ "group by case when is_medical_college = 1 then 'MEDICAL_COLLEGE' "
			+ "     when hospital_level = 'DISTRICT' then 'DISTRICT' "
			+ "     when chc_id is not null then 'CHC' "
			+ "     when phc_id is not null then 'PHC' "
			+ "     else 'TALUKA' end",
			nativeQuery = true)
	List<Object[]> countFacilitiesByTier();

	/** District/Taluka report (NrcReportService): every hospital of the requested level, statewide. */
	List<Hospital> findByHospitalLevel(String hospitalLevel);

	/** NRC Analytics (NrcAnalyticsService): every hospital registered in one district. */
	List<Hospital> findByDistrictId(Integer districtId);

	/**
	 * Backs the "Hospital Records" list page (Stage 2) - search box matches
	 * against code/name/short-name; a blank/null search returns everything.
	 * SQL Server needs CONCAT() for the wildcard, not JPQL's bare "%:x%"
	 * shorthand (that's HQL-only sugar, not valid native T-SQL).
	 */
	@Query(value = "select * from nr.hospital h where (:search is null or :search = '' "
			+ "or h.name like concat('%', :search, '%') or h.h_code like concat('%', :search, '%') "
			+ "or h.name_short like concat('%', :search, '%')) order by h.h_id desc",
			countQuery = "select count(*) from nr.hospital h where (:search is null or :search = '' "
			+ "or h.name like concat('%', :search, '%') or h.h_code like concat('%', :search, '%') "
			+ "or h.name_short like concat('%', :search, '%'))",
			nativeQuery = true)
	Page<Hospital> search(@Param("search") String search, Pageable pageable);
}
