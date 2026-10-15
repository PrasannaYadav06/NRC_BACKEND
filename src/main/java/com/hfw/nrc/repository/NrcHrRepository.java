package com.hfw.nrc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.NrcHr;

public interface NrcHrRepository extends JpaRepository<NrcHr, Integer> {

	/** Self-service form prefill - the caller's own hospital's most recent submission, whatever month/year that was. */
	@Query(value = "select top 1 * from nr.nrc_hr where h_id = :hId order by year desc, month desc, hr_rec_id desc", nativeQuery = true)
	NrcHr findLatestForHospital(@Param("hId") Integer hId);

	/**
	 * Upsert target - this hospital's row for the given month/year, if one
	 * already exists. Was a derived query method (findByHIdAndMonthAndYear) -
	 * Spring Data's property-path parser splits that name into "HId" (capital
	 * H, matching the method-name convention) but then looks up that EXACT
	 * string against Hibernate's metamodel, which only has "hId" (the real
	 * Java field name, lowercase h) - the mismatch throws
	 * QueryCreationException at repository-bean-creation time (app startup),
	 * not at compile time, so `mvn compile` never caught it. Explicit @Query
	 * sidesteps property-path parsing entirely, matching this file's other
	 * methods.
	 */
	@Query(value = "select top 1 * from nr.nrc_hr where h_id = :hId and month = :month and year = :year", nativeQuery = true)
	NrcHr findByHIdAndMonthAndYear(@Param("hId") int hId, @Param("month") int month, @Param("year") int year);

	/** "Search HR Records" list - search matches hospital name/short name; a blank/null search returns everything. */
	@Query(value = "select hr.* from nr.nrc_hr hr join nr.hospital h on h.h_id = hr.h_id where "
			+ "(:hId is null or hr.h_id = :hId) "
			+ "and (:search is null or :search = '' or h.name like concat('%',:search,'%') or h.name_short like concat('%',:search,'%')) "
			+ "and (:month is null or hr.month = :month) "
			+ "and (:year is null or hr.year = :year) "
			+ "order by hr.year desc, hr.month desc, hr.h_id",
			countQuery = "select count(*) from nr.nrc_hr hr join nr.hospital h on h.h_id = hr.h_id where "
			+ "(:hId is null or hr.h_id = :hId) "
			+ "and (:search is null or :search = '' or h.name like concat('%',:search,'%') or h.name_short like concat('%',:search,'%')) "
			+ "and (:month is null or hr.month = :month) "
			+ "and (:year is null or hr.year = :year)",
			nativeQuery = true)
	Page<NrcHr> search(@Param("hId") Integer hId, @Param("search") String search,
			@Param("month") Integer month, @Param("year") Integer year, Pageable pageable);

	/**
	 * Dashboard "HR" card (Stage 6 repoint from nr.hospital_details) - total
	 * recruited (sum of every category's "working" column) vs total
	 * sanctioned posts, across all six designations including MSW (the old
	 * hospital_details-backed query excluded MSW entirely since it never
	 * reliably had data - now it does, see V16). null hospitalId = every
	 * hospital (admin); a real hospitalId = that hospital only, same
	 * convention as the rest of DashboardService.
	 */
	@Query(value = "select "
			+ "sum(isnull(officer_working,0)+isnull(nurse_working,0)+isnull(dietician_working,0)+isnull(cook_working,0)+isnull(tender_working,0)+isnull(msw_working,0)) as recruited, "
			+ "sum(isnull(officer_sanctioned,0)+isnull(nurse_sanctioned,0)+isnull(dietician_sanctioned,0)+isnull(cook_sanctioned,0)+isnull(tender_sanctioned,0)+isnull(msw_sanctioned,0)) as sanctioned "
			+ "from nr.nrc_hr where month = :month and year = :year and (:hospitalId is null or h_id = :hospitalId)",
			nativeQuery = true)
	java.util.List<Object[]> sumHrRecruitedAndSanctioned(@Param("hospitalId") Integer hospitalId,
			@Param("month") Integer month, @Param("year") Integer year);
}
