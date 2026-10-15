package com.hfw.nrc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hfw.nrc.domain.HospitalDetail;

public interface HospitalDetailRepository extends JpaRepository<HospitalDetail, Integer> {

	/**
	 * Ported verbatim from NRCOperationDaoImpl#getHospitalDetails(Integer):
	 * latest month/year of non-bed HR values (hr_type_id <> 25) unioned with
	 * latest month/year of the bed count (hr_type_id = 25), for one hospital.
	 */
	@Query(value = "select details_id, h_id, month, year, hr_type_id, value, created_on, created_by, last_updated_on, last_updated_by from "
			+ "(select convert(date, max(created_on)) as year_month from nr.hospital_details where h_id = :hospitalId and hr_type_id <> 25) a, "
			+ "nr.hospital_details h where h.month = month(a.year_month) and h.year = year(a.year_month) and h_id = :hospitalId and hr_type_id <> 25 "
			+ "union all "
			+ "select details_id, h_id, month, year, hr_type_id, value, created_on, created_by, last_updated_on, last_updated_by from "
			+ "(select convert(date, max(created_on)) as year_month from nr.hospital_details where h_id = :hospitalId and hr_type_id = 25) a, "
			+ "nr.hospital_details h where h.month = month(a.year_month) and h.year = year(a.year_month) and h_id = :hospitalId and hr_type_id = 25",
			nativeQuery = true)
	List<HospitalDetail> findLatestForHospital(@Param("hospitalId") Integer hospitalId);

	/** Ported from NRCOperationDaoImpl#getHospitalDetail(month, year, hrTypeVal, hospitalId). */
	@Query(value = "select * from nr.hospital_details where month = :month and year = :year "
			+ "and hr_type_id = :hrTypeId and h_id = :hospitalId", nativeQuery = true)
	HospitalDetail findByMonthYearTypeAndHospital(@Param("month") Integer month, @Param("year") Integer year,
			@Param("hrTypeId") Integer hrTypeId, @Param("hospitalId") Integer hospitalId);

	/**
	 * Dashboard HR card: total recruited (sum of "working" values, hr_type_id
	 * %4=2) vs total sanctioned posts (hr_type_id %4=1), across hr_type_id
	 * 1-20 (officers/nurses/dieticians/cooks/tenders - same category set
	 * ResourceService uses; hr_type_id 25 is the bed count, not staff, so
	 * deliberately excluded). One combined number, not split by hospital
	 * level, per the user's spec ("Total recruited in District + taluka").
	 * null hospitalId = every hospital (admin); a real hospitalId = that
	 * hospital only (non-admin user) - same pattern as the other dashboard
	 * queries.
	 */
	@Query(value = "select "
			+ "sum(case when hd.hr_type_id % 4 = 2 then hd.value else 0 end) as recruited, "
			+ "sum(case when hd.hr_type_id % 4 = 1 then hd.value else 0 end) as sanctioned "
			+ "from nr.hospital_details hd "
			+ "where hd.month = :month and hd.year = :year and hd.hr_type_id between 1 and 20 "
			+ "and (:hospitalId is null or hd.h_id = :hospitalId)", nativeQuery = true)
	List<Object[]> sumHrRecruitedAndSanctioned(@Param("hospitalId") Integer hospitalId,
			@Param("month") Integer month, @Param("year") Integer year);

	/**
	 * NRC Analytics HR staffing/gauge (NrcAnalyticsService#hr/hrGauge): bulk
	 * version of findLatestForHospital's non-bed branch across every hospital
	 * in scope in one query, instead of a resourceForHospital() call per
	 * hospital per role (which itself re-ran findLatestForHospital AND
	 * rebuilt all 5 roles' data just to keep one) - was the real N+1 behind
	 * "HR Staffing is slow". Same "latest batch" semantics: per hospital,
	 * find the (month,year) of its most recently touched non-bed HR row,
	 * then sum sanctioned/working/vacant/training (hr_type_id % 4 ==
	 * 1/2/3/0) for just that hospital's rows in that exact (month,year),
	 * restricted to [minTypeId,maxTypeId] (one role = a 4-id block, e.g.
	 * officer=1-4; all roles = 1-20). Each row is [h_id, sanctioned,
	 * working, vacant, training]. fyStart/fyEnd (both null or both set) let
	 * the FY dropdown change what "latest" means - null/null = latest ever
	 * (unbounded, the original behavior); a real range restricts "latest"
	 * to the most recent snapshot submitted WITHIN that FY, so picking a
	 * past FY shows that year's own numbers instead of always today's.
	 */
	@Query(value = "with latest as ( "
			+ "select h_id, month(max(created_on)) as mon, year(max(created_on)) as yr "
			+ "from nr.hospital_details where h_id in (:hIds) and hr_type_id between 1 and 20 "
			+ "and (:fyStart is null or created_on >= :fyStart) "
			+ "and (:fyEnd is null or created_on <= :fyEnd) "
			+ "group by h_id "
			+ ") "
			+ "select hd.h_id, "
			+ "sum(case when hd.hr_type_id % 4 = 1 then hd.value else 0 end) as sanctioned, "
			+ "sum(case when hd.hr_type_id % 4 = 2 then hd.value else 0 end) as working, "
			+ "sum(case when hd.hr_type_id % 4 = 3 then hd.value else 0 end) as vacant, "
			+ "sum(case when hd.hr_type_id % 4 = 0 then hd.value else 0 end) as training "
			+ "from nr.hospital_details hd "
			+ "join latest l on l.h_id = hd.h_id and hd.month = l.mon and hd.year = l.yr "
			+ "where hd.h_id in (:hIds) and hd.hr_type_id between :minTypeId and :maxTypeId "
			+ "group by hd.h_id", nativeQuery = true)
	List<Object[]> sumHrByHospitalLatestSnapshot(@Param("hIds") List<Integer> hIds,
			@Param("minTypeId") int minTypeId, @Param("maxTypeId") int maxTypeId,
			@Param("fyStart") java.sql.Timestamp fyStart, @Param("fyEnd") java.sql.Timestamp fyEnd);

	/**
	 * HR Staffing tooltip: per-hospital breakdown by INDIVIDUAL designation
	 * (not collapsed across all 5 like sumHrByHospitalLatestSnapshot above) -
	 * roleIdx 0-4 = officer/nurse/dietician/cook/tender (NrcAnalyticsService's
	 * own ROLE_INDEX order, derived here as (hr_type_id-1)/4). Same FY-bounded
	 * "latest" semantics as above. Each row is [h_id, roleIdx, sanctioned, working].
	 */
	@Query(value = "with latest as ( "
			+ "select h_id, month(max(created_on)) as mon, year(max(created_on)) as yr "
			+ "from nr.hospital_details where h_id in (:hIds) and hr_type_id between 1 and 20 "
			+ "and (:fyStart is null or created_on >= :fyStart) "
			+ "and (:fyEnd is null or created_on <= :fyEnd) "
			+ "group by h_id "
			+ ") "
			+ "select hd.h_id, (hd.hr_type_id - 1) / 4 as roleIdx, "
			+ "sum(case when hd.hr_type_id % 4 = 1 then hd.value else 0 end) as sanctioned, "
			+ "sum(case when hd.hr_type_id % 4 = 2 then hd.value else 0 end) as working "
			+ "from nr.hospital_details hd "
			+ "join latest l on l.h_id = hd.h_id and hd.month = l.mon and hd.year = l.yr "
			+ "where hd.h_id in (:hIds) and hd.hr_type_id between 1 and 20 "
			+ "group by hd.h_id, (hd.hr_type_id - 1) / 4", nativeQuery = true)
	List<Object[]> sumHrByDesignationLatestSnapshot(@Param("hIds") List<Integer> hIds,
			@Param("fyStart") java.sql.Timestamp fyStart, @Param("fyEnd") java.sql.Timestamp fyEnd);

	/**
	 * Bulk version of resolveBedCount (hr_type_id = 25) across every hospital
	 * in scope in one query - same "latest batch" semantics as
	 * sumHrByHospitalLatestSnapshot above, applied to the single bed-count
	 * bucket. Each row is [h_id, beds].
	 */
	@Query(value = "with latest as ( "
			+ "select h_id, month(max(created_on)) as mon, year(max(created_on)) as yr "
			+ "from nr.hospital_details where h_id in (:hIds) and hr_type_id = 25 group by h_id "
			+ ") "
			+ "select hd.h_id, max(hd.value) as beds "
			+ "from nr.hospital_details hd "
			+ "join latest l on l.h_id = hd.h_id and hd.month = l.mon and hd.year = l.yr "
			+ "where hd.h_id in (:hIds) and hd.hr_type_id = 25 "
			+ "group by hd.h_id", nativeQuery = true)
	List<Object[]> bedCountsGroupedByHospital(@Param("hIds") List<Integer> hIds);
}
