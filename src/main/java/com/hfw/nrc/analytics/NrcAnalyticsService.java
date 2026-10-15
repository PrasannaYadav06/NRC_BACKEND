package com.hfw.nrc.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.District;
import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.NrcDischarge;
import com.hfw.nrc.domain.Outcome;
import com.hfw.nrc.domain.Taluka;
import com.hfw.nrc.report.NrcReportService;
import com.hfw.nrc.repository.DistrictRepository;
import com.hfw.nrc.repository.HospitalDetailRepository;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.NrcAdmissionRepository;
import com.hfw.nrc.repository.NrcDischargeRepository;
import com.hfw.nrc.repository.OutcomeRepository;
import com.hfw.nrc.repository.TalukaRepository;

/**
 * NRC Analytics - backing logic for the 4 dashboards (Admission/Discharge
 * trends, Outcome distribution, Bed Occupancy, HR staffing), one row per
 * district (or a single row when scoped to one district for the taluka
 * drill-down). Modeled on KABHI's own AnalyticsService (same
 * resolveDateRange/district-row shape, plain Map rows not DTO classes -
 * see the approved plan). Bed occupancy and HR both read straight off
 * hospital_details via bulk grouped queries (HospitalDetailRepository/
 * NrcAdmissionRepository) rather than NrcReportService's per-hospital
 * formulas/ResourceService - those did one DB round trip (bed occupancy:
 * several) per hospital, the actual cause of "the dashboards are slow on
 * every filter change" (confirmed this session).
 */
@Service
public class NrcAnalyticsService {

	@Autowired
	private HospitalRepository hospitalRepository;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private TalukaRepository talukaRepository;
	@Autowired
	private NrcAdmissionRepository nrcAdmissionRepository;
	@Autowired
	private NrcDischargeRepository nrcDischargeRepository;
	@Autowired
	private com.hfw.nrc.repository.NrcFollowupRepository nrcFollowupRepository;
	@Autowired
	private OutcomeRepository outcomeRepository;
	@Autowired
	private HospitalDetailRepository hospitalDetailRepository;

	/** FY = Apr 1 - Mar 31, capped at today; optional month narrows to one calendar month within that FY. Mirrors KABHI's own resolveDateRange. */
	public LocalDate[] resolveDateRange(Integer startYear, Integer month) {
		int fyStartYear = startYear != null ? startYear : NrcReportService.fiscalYearStart(LocalDate.now()).getYear();
		LocalDate fyStart = LocalDate.of(fyStartYear, 4, 1);
		LocalDate today = LocalDate.now();
		LocalDate fyEnd = fyStart.plusYears(1).minusDays(1);
		if (fyEnd.isAfter(today)) fyEnd = today;

		if (month != null) {
			int year = month >= 4 ? fyStartYear : fyStartYear + 1;
			LocalDate monthStart = LocalDate.of(year, month, 1);
			LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
			if (monthEnd.isAfter(today)) monthEnd = today;
			if (monthStart.isAfter(today)) monthStart = today;
			return new LocalDate[] { monthStart, monthEnd };
		}
		return new LocalDate[] { fyStart, fyEnd };
	}

	private List<District> districtsInScope(Integer onlyDistrictId) {
		if (onlyDistrictId != null) {
			return districtRepository.findById((long) (int) onlyDistrictId)
					.map(List::of).orElse(List.of());
		}
		return districtRepository.findAll();
	}

	/** Every hospital in scope, keyed by hId -> its districtId - built once per request from the cheap per-district hospital lookup, so the heavier admission/discharge/outcome queries below can run once in bulk instead of once per hospital. */
	private Map<Integer, Integer> hospitalToDistrictMap(List<District> districts) {
		Map<Integer, Integer> map = new HashMap<>();
		for (District d : districts) {
			for (Hospital h : hospitalRepository.findByDistrictId(d.getDistrictId().intValue())) {
				map.put(h.getHId(), d.getDistrictId().intValue());
			}
		}
		return map;
	}

	/**
	 * Was a per-hospital findByHIdAndAdmissionDateBetween/findByHospitalAndDischargeDateBetween
	 * loop (~150 DB round trips for an ADMIN's all-districts view - the real cause of the "charts
	 * take a long time to load" report). Now two grouped queries total, regardless of how many
	 * hospitals are in scope.
	 */
	public List<Map<String, Object>> admissionDischarge(LocalDate start, LocalDate end, Integer onlyDistrictId) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));

		List<District> districts = districtsInScope(onlyDistrictId);
		Map<Integer, Integer> hospitalToDistrict = hospitalToDistrictMap(districts);
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());

		Map<Integer, Integer> admissionsByDistrict = new HashMap<>();
		Map<Integer, Integer> dischargesByDistrict = new HashMap<>();
		if (!hIds.isEmpty()) {
			for (Object[] r : nrcAdmissionRepository.countAdmissionsGroupedByHospital(hIds, startTs, endTs)) {
				Integer districtId = hospitalToDistrict.get(((Number) r[0]).intValue());
				if (districtId != null) admissionsByDistrict.merge(districtId, ((Number) r[1]).intValue(), Integer::sum);
			}
			for (Object[] r : nrcDischargeRepository.countDischargesGroupedByHospital(hIds, startTs, endTs)) {
				Integer districtId = hospitalToDistrict.get(((Number) r[0]).intValue());
				if (districtId != null) dischargesByDistrict.merge(districtId, ((Number) r[1]).intValue(), Integer::sum);
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (District d : districts) {
			int districtId = d.getDistrictId().intValue();
			int admissions = admissionsByDistrict.getOrDefault(districtId, 0);
			int discharges = dischargesByDistrict.getOrDefault(districtId, 0);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("districtId", d.getDistrictId());
			row.put("districtName", d.getDistrictName());
			row.put("admissions", admissions);
			row.put("discharges", discharges);
			row.put("total", admissions + discharges);
			rows.add(row);
		}
		return rows;
	}

	/**
	 * In-Patient Care dashboard: how many children are currently in
	 * in-patient care (admitted on/before :end, not yet discharged or
	 * discharged on/after :start - same "active" definition bed occupancy
	 * uses), per district. Same bulk-query shape as admissionDischarge
	 * above via countActiveAdmissionsGroupedByHospital.
	 */
	/**
	 * In-Patient Care dashboard. Per district (or per taluka/hospital via
	 * talukasForDistrict below): admissions, discharges, in-patient-care
	 * (currently active) and follow-up visits in the selected reporting
	 * period, HR sanctioned/working (always current-state - HR has no date
	 * dimension, same as the HR Staffing page), and totalTreatedLive - an
	 * all-time admissions count, deliberately NOT scoped to the reporting
	 * period filter (computed fresh every call, see
	 * NrcAdmissionRepository#countAllTimeAdmissionsGroupedByHospital).
	 */
	public List<Map<String, Object>> inPatientCare(LocalDate start, LocalDate end, Integer onlyDistrictId) {
		List<District> districts = districtsInScope(onlyDistrictId);
		Map<Integer, Integer> hospitalToDistrict = hospitalToDistrictMap(districts);
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());

		Map<Integer, int[]> byDistrict = sumInPatientCareMetricsByGroup(hIds, hospitalToDistrict, start, end);

		List<Map<String, Object>> rows = new ArrayList<>();
		for (District d : districts) {
			int[] m = byDistrict.getOrDefault(d.getDistrictId().intValue(), new int[7]);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("districtId", d.getDistrictId());
			row.put("districtName", d.getDistrictName());
			row.put("admissions", m[0]);
			row.put("discharges", m[1]);
			row.put("inPatientCare", m[2]);
			row.put("followups", m[3]);
			row.put("hrSanctioned", m[4]);
			row.put("hrWorking", m[5]);
			row.put("totalTreatedLive", m[6]);
			row.put("total", m[2]);
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Bulk metrics shared by inPatientCare() (grouped by district) and
	 * talukasForDistrict() (grouped by hospital, i.e. groupKey = hId itself)
	 * - one set of queries across hIds, summed into whatever grouping the
	 * caller's groupOf map provides. Index order: [admissions, discharges,
	 * inPatientCare, followups, hrSanctioned, hrWorking, totalTreatedLive].
	 */
	private Map<Integer, int[]> sumInPatientCareMetricsByGroup(List<Integer> hIds, Map<Integer, Integer> groupOf,
			LocalDate start, LocalDate end) {
		Map<Integer, int[]> byGroup = new HashMap<>();
		if (hIds.isEmpty()) return byGroup;

		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));

		for (Object[] r : nrcAdmissionRepository.countAdmissionsGroupedByHospital(hIds, startTs, endTs)) {
			addMetric(byGroup, groupOf, r, 0);
		}
		for (Object[] r : nrcDischargeRepository.countDischargesGroupedByHospital(hIds, startTs, endTs)) {
			addMetric(byGroup, groupOf, r, 1);
		}
		for (Object[] r : nrcAdmissionRepository.countActiveAdmissionsGroupedByHospital(hIds, startTs, endTs)) {
			addMetric(byGroup, groupOf, r, 2);
		}
		for (Object[] r : nrcFollowupRepository.countFollowupVisitsGroupedByHospital(hIds, start, end)) {
			addMetric(byGroup, groupOf, r, 3);
		}
		for (Object[] r : hospitalDetailRepository.sumHrByHospitalLatestSnapshot(hIds, 1, 20, null, null)) {
			Integer group = groupOf.get(((Number) r[0]).intValue());
			if (group == null) continue;
			int[] bucket = byGroup.computeIfAbsent(group, k -> new int[7]);
			bucket[4] += ((Number) r[1]).intValue(); // sanctioned
			bucket[5] += ((Number) r[2]).intValue(); // working
		}
		for (Object[] r : nrcAdmissionRepository.countAllTimeAdmissionsGroupedByHospital(hIds)) {
			addMetric(byGroup, groupOf, r, 6);
		}
		return byGroup;
	}

	private void addMetric(Map<Integer, int[]> byGroup, Map<Integer, Integer> groupOf, Object[] row, int index) {
		Integer group = groupOf.get(((Number) row[0]).intValue());
		if (group == null) return;
		int[] bucket = byGroup.computeIfAbsent(group, k -> new int[7]);
		bucket[index] += ((Number) row[1]).intValue();
	}

	/** Same N+1-to-bulk fix as admissionDischarge above, via countOutcomesGroupedByHospitalAndOutcome. */
	public List<Map<String, Object>> outcomeDistribution(LocalDate start, LocalDate end, Integer onlyDistrictId) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));
		Map<Integer, String> outcomeNames = new HashMap<>();
		for (Outcome o : outcomeRepository.findAll()) outcomeNames.put(o.getOutcomeId(), o.getOutcome());

		List<District> districts = districtsInScope(onlyDistrictId);
		Map<Integer, Integer> hospitalToDistrict = hospitalToDistrictMap(districts);
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());

		// index: 0=improved, 1=lama, 2=death, 3=referred, 4=nonRespondent
		Map<Integer, int[]> countsByDistrict = new HashMap<>();
		if (!hIds.isEmpty()) {
			for (Object[] r : nrcDischargeRepository.countOutcomesGroupedByHospitalAndOutcome(hIds, startTs, endTs)) {
				Integer districtId = hospitalToDistrict.get(((Number) r[0]).intValue());
				Integer outcomeId = r[1] == null ? null : ((Number) r[1]).intValue();
				if (districtId == null || outcomeId == null) continue;
				String outcome = outcomeNames.get(outcomeId);
				if (outcome == null) continue;
				int count = ((Number) r[2]).intValue();
				int[] bucket = countsByDistrict.computeIfAbsent(districtId, k -> new int[5]);
				if ("Improved".equalsIgnoreCase(outcome)) bucket[0] += count;
				else if ("LAMA".equalsIgnoreCase(outcome)) bucket[1] += count;
				else if ("Death".equalsIgnoreCase(outcome)) bucket[2] += count;
				else if ("Referred".equalsIgnoreCase(outcome)) bucket[3] += count;
				else if ("Non Respondent".equalsIgnoreCase(outcome)) bucket[4] += count;
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (District d : districts) {
			int[] c = countsByDistrict.getOrDefault(d.getDistrictId().intValue(), new int[5]);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("districtId", d.getDistrictId());
			row.put("districtName", d.getDistrictName());
			row.put("improved", c[0]);
			row.put("lama", c[1]);
			row.put("death", c[2]);
			row.put("referred", c[3]);
			row.put("nonRespondent", c[4]);
			row.put("total", c[0] + c[1] + c[2] + c[3] + c[4]);
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Per-district Bed Occupancy = mean of its hospitals' own rate (true
	 * average, not summed). Was a per-hospital computeHospitalReportRow()
	 * call (several queries + a full statewide-report-row build, just to
	 * read one field off it) - now 2 grouped queries total regardless of
	 * how many hospitals are in scope, same N+1-to-bulk fix as
	 * admissionDischarge/outcomeDistribution above.
	 */
	public List<Map<String, Object>> bedOccupancy(LocalDate start, LocalDate end, Integer onlyDistrictId) {
		List<District> districts = districtsInScope(onlyDistrictId);
		Map<Integer, Integer> hospitalToDistrict = hospitalToDistrictMap(districts);
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());
		Map<Integer, BigDecimal> rateByHospital = computeBedOccupancyRatesBulk(hIds, start, end);

		Map<Integer, BigDecimal> sumByDistrict = new HashMap<>();
		Map<Integer, Integer> countByDistrict = new HashMap<>();
		for (Map.Entry<Integer, BigDecimal> e : rateByHospital.entrySet()) {
			Integer districtId = hospitalToDistrict.get(e.getKey());
			if (districtId == null) continue;
			sumByDistrict.merge(districtId, e.getValue(), BigDecimal::add);
			countByDistrict.merge(districtId, 1, Integer::sum);
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (District d : districts) {
			int districtId = d.getDistrictId().intValue();
			int count = countByDistrict.getOrDefault(districtId, 0);
			BigDecimal avg = count == 0 ? BigDecimal.ZERO
					: sumByDistrict.getOrDefault(districtId, BigDecimal.ZERO).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("districtId", d.getDistrictId());
			row.put("districtName", d.getDistrictName());
			row.put("occupancyRate", avg);
			row.put("total", avg);
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Bed Occupancy comprehensive dashboard's "in-built worksheet": one row
	 * per NRC (hospital), not per district - the Department's own ask was
	 * "automatically calculating these indicators" per facility. Two
	 * indicators per row, both auto-computed from predefined formulae
	 * (current verified formulae - swap in one place once the Department
	 * shares its own standard methodology, per the user's go-ahead):
	 *  - bedOccupancyRate: patient-days / (beds x period days) x 100 (same
	 *    formula as bedOccupancy() above, ported from the old
	 *    nr.bedoccupancyrate stored procedure)
	 *  - admissionRate: admissions in [start,end] / beds - average number
	 *    of children admitted per bed over the period, the natural
	 *    per-capacity normalization mirroring how bed occupancy itself is
	 *    normalized. This is NOT an official Department formula (none
	 *    exists yet for "average admission rate") - a reasonable default,
	 *    clearly labelled as such on the frontend, swappable once the real
	 *    methodology arrives.
	 */
	public List<Map<String, Object>> bedOccupancyWorksheet(LocalDate start, LocalDate end, Integer onlyDistrictId) {
		List<District> districts = districtsInScope(onlyDistrictId);
		Map<String, String> districtNames = new HashMap<>();
		for (District d : districts) districtNames.put(d.getDistrictId().toString(), d.getDistrictName());

		List<Hospital> hospitals = new ArrayList<>();
		Map<Integer, Integer> hospitalToDistrict = new HashMap<>();
		for (District d : districts) {
			for (Hospital h : hospitalRepository.findByDistrictId(d.getDistrictId().intValue())) {
				hospitals.add(h);
				hospitalToDistrict.put(h.getHId(), d.getDistrictId().intValue());
			}
		}
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());

		Map<Integer, BigDecimal> occupancyByHospital = computeBedOccupancyRatesBulk(hIds, start, end);
		Map<Integer, Integer> bedsByHospital = new HashMap<>();
		for (Object[] r : hospitalDetailRepository.bedCountsGroupedByHospital(hIds)) {
			bedsByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
		}
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));
		Map<Integer, Integer> admissionsByHospital = new HashMap<>();
		if (!hIds.isEmpty()) {
			for (Object[] r : nrcAdmissionRepository.countAdmissionsGroupedByHospital(hIds, startTs, endTs)) {
				admissionsByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (Hospital h : hospitals) {
			Integer districtId = hospitalToDistrict.get(h.getHId());
			int beds = bedsByHospital.getOrDefault(h.getHId(), 0);
			int admissions = admissionsByHospital.getOrDefault(h.getHId(), 0);
			BigDecimal admissionRate = beds == 0 ? BigDecimal.ZERO
					: BigDecimal.valueOf(admissions).divide(BigDecimal.valueOf(beds), 2, RoundingMode.HALF_UP);

			Map<String, Object> row = new LinkedHashMap<>();
			row.put("hospitalId", h.getHId());
			row.put("hospitalName", h.getName());
			row.put("districtName", districtId != null ? districtNames.get(districtId.toString()) : null);
			row.put("beds", beds);
			row.put("admissions", admissions);
			row.put("bedOccupancyRate", occupancyByHospital.getOrDefault(h.getHId(), BigDecimal.ZERO));
			row.put("admissionRate", admissionRate);
			rows.add(row);
		}
		return rows;
	}

	/** Bulk equivalent of NrcReportService#computeBedOccupancyRate for every hospital in hIds at once - see NrcAdmissionRepository#sumPatientDaysGroupedByHospital/HospitalDetailRepository#bedCountsGroupedByHospital. Exposed (not private) so talukasForDistrict below can reuse it for a single hospital without a 3rd calculation path. */
	Map<Integer, BigDecimal> computeBedOccupancyRatesBulk(List<Integer> hIds, LocalDate start, LocalDate end) {
		Map<Integer, BigDecimal> result = new HashMap<>();
		if (hIds.isEmpty()) return result;

		Map<Integer, Integer> bedsByHospital = new HashMap<>();
		for (Object[] r : hospitalDetailRepository.bedCountsGroupedByHospital(hIds)) {
			bedsByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
		}

		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));
		Map<Integer, Long> patientDaysByHospital = new HashMap<>();
		for (Object[] r : nrcAdmissionRepository.sumPatientDaysGroupedByHospital(hIds, startTs, endTs)) {
			if (r[1] != null) patientDaysByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).longValue());
		}

		long periodDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
		for (Integer hId : hIds) {
			int beds = bedsByHospital.getOrDefault(hId, 0);
			if (beds == 0) {
				result.put(hId, BigDecimal.ZERO);
				continue;
			}
			long patientDays = patientDaysByHospital.getOrDefault(hId, 0L);
			BigDecimal denominator = BigDecimal.valueOf(beds).multiply(BigDecimal.valueOf(periodDays));
			result.put(hId, BigDecimal.valueOf(patientDays).multiply(BigDecimal.valueOf(100))
					.divide(denominator, 2, RoundingMode.HALF_UP));
		}
		return result;
	}

	private static final Map<String, Integer> ROLE_INDEX = Map.of(
			"officer", 0, "nurse", 1, "dietician", 2, "cook", 3, "tender", 4);

	/**
	 * Current-state HR snapshot (no date range), per district, for one role
	 * (or every role combined when role is null/"all") - via
	 * hospital_details directly now. Was resourceForHospital() called once
	 * per hospital per role (each call re-running findLatestForHospital AND
	 * rebuilding all 5 roles' data just to keep 1) - now a single grouped
	 * query across every hospital in scope.
	 */
	public List<Map<String, Object>> hr(String role, Integer onlyDistrictId, Integer fyStartYear) {
		List<District> districts = districtsInScope(onlyDistrictId);
		Map<Integer, Integer> hospitalToDistrict = hospitalToDistrictMap(districts);
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());

		boolean allRoles = role == null || "all".equalsIgnoreCase(role);
		int minTypeId = allRoles ? 1 : ROLE_INDEX.get(role) * 4 + 1;
		int maxTypeId = allRoles ? 20 : minTypeId + 3;
		Timestamp[] fy = fyBounds(fyStartYear);

		// index: 0=sanctioned, 1=working, 2=vacant, 3=training
		Map<Integer, int[]> byDistrict = new HashMap<>();
		if (!hIds.isEmpty()) {
			for (Object[] r : hospitalDetailRepository.sumHrByHospitalLatestSnapshot(hIds, minTypeId, maxTypeId, fy[0], fy[1])) {
				Integer districtId = hospitalToDistrict.get(((Number) r[0]).intValue());
				if (districtId == null) continue;
				int[] bucket = byDistrict.computeIfAbsent(districtId, k -> new int[4]);
				bucket[0] += ((Number) r[1]).intValue();
				bucket[1] += ((Number) r[2]).intValue();
				bucket[2] += ((Number) r[3]).intValue();
				bucket[3] += ((Number) r[4]).intValue();
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (District d : districts) {
			int[] c = byDistrict.getOrDefault(d.getDistrictId().intValue(), new int[4]);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("districtId", d.getDistrictId());
			row.put("districtName", d.getDistrictName());
			row.put("sanctioned", c[0]);
			row.put("working", c[1]);
			row.put("vacant", c[2]);
			row.put("training", c[3]);
			row.put("total", c[1]);
			rows.add(row);
		}
		return rows;
	}

	/** State-wide Sanctioned-vs-Working gauge, all roles combined, admin-only (enforced at the controller). Same bulk query as hr() above, unfiltered by district. */
	public Map<String, Object> hrGauge(Integer fyStartYear) {
		List<Integer> hIds = new ArrayList<>();
		for (Hospital h : hospitalRepository.findAll()) hIds.add(h.getHId());
		Timestamp[] fy = fyBounds(fyStartYear);

		int totalSanctioned = 0, totalWorking = 0;
		if (!hIds.isEmpty()) {
			for (Object[] r : hospitalDetailRepository.sumHrByHospitalLatestSnapshot(hIds, 1, 20, fy[0], fy[1])) {
				totalSanctioned += ((Number) r[1]).intValue();
				totalWorking += ((Number) r[2]).intValue();
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("totalSanctioned", totalSanctioned);
		result.put("totalWorking", totalWorking);
		return result;
	}

	/**
	 * HR Staffing tooltip: per-district breakdown by EVERY designation (not
	 * just one role / all-roles-combined) - same shape the In-Patient Care
	 * map tooltip uses (plain label+count rows + one highlighted total).
	 */
	public List<Map<String, Object>> hrDesignationBreakdown(Integer onlyDistrictId, Integer fyStartYear) {
		List<District> districts = districtsInScope(onlyDistrictId);
		Map<Integer, Integer> hospitalToDistrict = hospitalToDistrictMap(districts);
		List<Integer> hIds = new ArrayList<>(hospitalToDistrict.keySet());
		Timestamp[] fy = fyBounds(fyStartYear);

		// per district: [officerW, nurseW, dieticianW, cookW, tenderW, officerS, nurseS, dieticianS, cookS, tenderS]
		Map<Integer, int[]> byDistrict = new HashMap<>();
		if (!hIds.isEmpty()) {
			for (Object[] r : hospitalDetailRepository.sumHrByDesignationLatestSnapshot(hIds, fy[0], fy[1])) {
				Integer districtId = hospitalToDistrict.get(((Number) r[0]).intValue());
				if (districtId == null) continue;
				int roleIdx = ((Number) r[1]).intValue();
				if (roleIdx < 0 || roleIdx > 4) continue;
				int[] bucket = byDistrict.computeIfAbsent(districtId, k -> new int[10]);
				bucket[roleIdx] += ((Number) r[3]).intValue(); // working
				bucket[5 + roleIdx] += ((Number) r[2]).intValue(); // sanctioned
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (District d : districts) {
			int[] c = byDistrict.getOrDefault(d.getDistrictId().intValue(), new int[10]);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("districtId", d.getDistrictId());
			row.put("districtName", d.getDistrictName());
			int totalWorking = 0, totalSanctioned = 0;
			for (int i = 0; i < 5; i++) {
				row.put(ROLE_INDEX_KEYS[i] + "Working", c[i]);
				row.put(ROLE_INDEX_KEYS[i] + "Sanctioned", c[5 + i]);
				totalWorking += c[i];
				totalSanctioned += c[5 + i];
			}
			row.put("totalWorking", totalWorking);
			row.put("totalSanctioned", totalSanctioned);
			rows.add(row);
		}
		return rows;
	}

	private static final String[] ROLE_INDEX_KEYS = { "officer", "nurse", "dietician", "cook", "tender" };

	/** null fyStartYear -> {null,null} (unbounded = "latest ever", the original behavior). Otherwise the FY's own Apr 1 - Mar 31 window. */
	private Timestamp[] fyBounds(Integer fyStartYear) {
		if (fyStartYear == null) return new Timestamp[] { null, null };
		LocalDate start = LocalDate.of(fyStartYear, 4, 1);
		LocalDate end = LocalDate.of(fyStartYear + 1, 3, 31);
		return new Timestamp[] { Timestamp.valueOf(start.atStartOfDay()), Timestamp.valueOf(end.atTime(23, 59, 59)) };
	}

	/**
	 * Every real Taluka in one district (NRC's own master list - not just
	 * ones with a hospital), each flagged hasHospital + carrying every
	 * dashboard's own metric so the one drill-down panel works for all 4
	 * pages. Talukas with no registered hospital get zeroed metrics - the
	 * frontend renders those as disabled.
	 */
	public List<Map<String, Object>> talukasForDistrict(Integer districtId, LocalDate start, LocalDate end) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));
		Map<Integer, String> outcomeNames = new HashMap<>();
		for (Outcome o : outcomeRepository.findAll()) outcomeNames.put(o.getOutcomeId(), o.getOutcome());

		List<Hospital> districtHospitals = hospitalRepository.findByDistrictId(districtId);
		Map<Integer, Hospital> byTaluka = new HashMap<>();
		for (Hospital h : districtHospitals) {
			if (h.getTalukaId() != null) byTaluka.put(h.getTalukaId(), h);
		}
		List<Integer> districtHIds = new ArrayList<>();
		for (Hospital h : districtHospitals) districtHIds.add(h.getHId());
		Map<Integer, BigDecimal> bedOccupancyByHospital = computeBedOccupancyRatesBulk(districtHIds, start, end);
		Map<Integer, Integer> inPatientCareByHospital = new HashMap<>();
		Map<Integer, Integer> followupByHospital = new HashMap<>();
		Map<Integer, Integer> hrSanctionedByHospital = new HashMap<>();
		Map<Integer, Integer> hrWorkingByHospital = new HashMap<>();
		Map<Integer, Integer> totalTreatedLiveByHospital = new HashMap<>();
		if (!districtHIds.isEmpty()) {
			for (Object[] r : nrcAdmissionRepository.countActiveAdmissionsGroupedByHospital(districtHIds, startTs, endTs)) {
				inPatientCareByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
			}
			for (Object[] r : nrcFollowupRepository.countFollowupVisitsGroupedByHospital(districtHIds, start, end)) {
				followupByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
			}
			for (Object[] r : hospitalDetailRepository.sumHrByHospitalLatestSnapshot(districtHIds, 1, 20, null, null)) {
				hrSanctionedByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
				hrWorkingByHospital.put(((Number) r[0]).intValue(), ((Number) r[2]).intValue());
			}
			for (Object[] r : nrcAdmissionRepository.countAllTimeAdmissionsGroupedByHospital(districtHIds)) {
				totalTreatedLiveByHospital.put(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
			}
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (Taluka t : talukaRepository.findByDistrictIdOrderByTalukaNameAsc(districtId)) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("talukaId", t.getTalukaId());
			row.put("talukaName", t.getTalukaName());

			Hospital h = byTaluka.get(t.getTalukaId());
			boolean hasHospital = h != null;
			row.put("hasHospital", hasHospital);
			row.put("hospitalId", hasHospital ? h.getHId() : null);
			row.put("hospitalName", hasHospital ? h.getName() : null);

			int admissions = 0, discharges = 0, improved = 0, lama = 0, death = 0, referred = 0, nonRespondent = 0;
			int inPatientCare = 0, followups = 0, hrSanctioned = 0, hrWorking = 0, totalTreatedLive = 0;
			BigDecimal bedOccupancy = BigDecimal.ZERO;
			if (hasHospital) {
				inPatientCare = inPatientCareByHospital.getOrDefault(h.getHId(), 0);
				followups = followupByHospital.getOrDefault(h.getHId(), 0);
				hrSanctioned = hrSanctionedByHospital.getOrDefault(h.getHId(), 0);
				hrWorking = hrWorkingByHospital.getOrDefault(h.getHId(), 0);
				totalTreatedLive = totalTreatedLiveByHospital.getOrDefault(h.getHId(), 0);
				admissions = nrcAdmissionRepository.findByHIdAndAdmissionDateBetween(h.getHId(), startTs, endTs).size();
				List<NrcDischarge> discs = nrcDischargeRepository.findByHospitalAndDischargeDateBetween(h.getHId(), startTs, endTs);
				discharges = discs.size();
				for (NrcDischarge disc : discs) {
					String outcome = outcomeNames.get(disc.getOutcomeId());
					if (outcome == null) continue;
					if ("Improved".equalsIgnoreCase(outcome)) improved++;
					else if ("LAMA".equalsIgnoreCase(outcome)) lama++;
					else if ("Death".equalsIgnoreCase(outcome)) death++;
					else if ("Referred".equalsIgnoreCase(outcome)) referred++;
					else if ("Non Respondent".equalsIgnoreCase(outcome)) nonRespondent++;
				}
				bedOccupancy = bedOccupancyByHospital.getOrDefault(h.getHId(), BigDecimal.ZERO);
			}
			row.put("admissions", admissions);
			row.put("discharges", discharges);
			row.put("improved", improved);
			row.put("lama", lama);
			row.put("death", death);
			row.put("referred", referred);
			row.put("nonRespondent", nonRespondent);
			row.put("bedOccupancyRate", bedOccupancy);
			row.put("inPatientCare", inPatientCare);
			row.put("followups", followups);
			row.put("hrSanctioned", hrSanctioned);
			row.put("hrWorking", hrWorking);
			row.put("totalTreatedLive", totalTreatedLive);
			rows.add(row);
		}
		return rows;
	}

	private static final List<String> FACILITY_TIERS = List.of("DISTRICT", "TALUKA", "CHC", "PHC", "MEDICAL_COLLEGE");
	private static final Map<String, String> FACILITY_TIER_LABELS = Map.of(
			"DISTRICT", "District Hospital", "TALUKA", "Taluk Hospital", "CHC", "CHC", "PHC", "PHC",
			"MEDICAL_COLLEGE", "Medical College");

	/**
	 * Karnataka Facilities page: one row per facility tier (District
	 * Hospital/Taluk Hospital/CHC/PHC/Medical College - see HospitalRepository
	 * #countFacilitiesByTier's javadoc for exactly how NRC's 2 real
	 * hospital_level values, the nullable chc_id/phc_id, and the explicit
	 * is_medical_college flag (V36) map onto these 5 buckets). "treated" =
	 * admissions in [start,end] at a hospital in that tier (NRC's own
	 * equivalent of DMHP's "Consultations" - NRC doesn't have an
	 * OPD-consultation concept, admission is the actual unit of care here).
	 */
	public List<Map<String, Object>> facilities(LocalDate start, LocalDate end) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));

		Map<String, Integer> facilityCounts = new HashMap<>();
		for (Object[] r : hospitalRepository.countFacilitiesByTier()) {
			facilityCounts.put((String) r[0], ((Number) r[1]).intValue());
		}
		Map<String, Integer> treatedCounts = new HashMap<>();
		for (Object[] r : nrcAdmissionRepository.countAdmissionsByFacilityTier(startTs, endTs)) {
			treatedCounts.put((String) r[0], ((Number) r[1]).intValue());
		}

		List<Map<String, Object>> rows = new ArrayList<>();
		for (String tier : FACILITY_TIERS) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("tier", tier);
			row.put("label", FACILITY_TIER_LABELS.get(tier));
			row.put("totalFacilities", facilityCounts.getOrDefault(tier, 0));
			row.put("totalTreated", treatedCounts.getOrDefault(tier, 0));
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Forecast page (was Outcome Distribution). Simple linear-trend
	 * projection, per the user's explicit go-ahead ("Yes, simple trend
	 * projection") - not a real statistical/seasonal model. Takes the last
	 * 3 actual calendar months (fixed, matching the reference design's own
	 * "Actual (last 3 months)" legend) for the requested type/district,
	 * fits a least-squares line through those 3 points, and extrapolates
	 * windowMonths months beyond the current month. Negative projections
	 * are floored at 0 (a count can't go negative) - everything else,
	 * including volatile swings on short/sparse data, is left as-is: this
	 * is a simple projection, not a smoothed one, same spirit as the
	 * reference screenshots' own volatile predicted values.
	 */
	public Map<String, Object> forecast(String type, Integer districtId, int windowMonths) {
		String t = type == null ? "ALL" : type.toUpperCase();
		if (windowMonths <= 0) windowMonths = 3;

		LocalDate currentMonthStart = LocalDate.now().withDayOfMonth(1);
		List<LocalDate> historyMonths = List.of(
				currentMonthStart.minusMonths(2), currentMonthStart.minusMonths(1), currentMonthStart);
		LocalDate histStart = historyMonths.get(0);
		LocalDate histEnd = currentMonthStart.plusMonths(1).minusDays(1);

		Map<String, Integer> countsByMonthKey = monthlyCountsForType(t, districtId, histStart, histEnd);

		double[] actual = new double[3];
		for (int i = 0; i < 3; i++) {
			actual[i] = countsByMonthKey.getOrDefault(monthKey(historyMonths.get(i)), 0);
		}

		// Least squares over x = 0,1,2 (the 3 actual months).
		double sumX = 3, sumY = actual[0] + actual[1] + actual[2];
		double sumXY = actual[1] + 2 * actual[2];
		double sumXX = 5; // 0^2+1^2+2^2
		double denom = 3 * sumXX - sumX * sumX;
		double slope = denom == 0 ? 0 : (3 * sumXY - sumX * sumY) / denom;
		double intercept = (sumY - slope * sumX) / 3;

		List<Map<String, Object>> series = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			Map<String, Object> pt = new LinkedHashMap<>();
			pt.put("month", monthKey(historyMonths.get(i)));
			pt.put("type", "ACTUAL");
			pt.put("value", round2(actual[i]));
			pt.put("changeVsPrev", i == 0 ? null : pctChange(actual[i - 1], actual[i]));
			series.add(pt);
		}

		double lastActual = actual[2];
		double prevForChange = lastActual;
		double predictedFinal = lastActual;
		String predictedFinalMonth = monthKey(historyMonths.get(2));
		for (int m = 1; m <= windowMonths; m++) {
			double predicted = Math.max(0, slope * (2 + m) + intercept);
			LocalDate monthDate = currentMonthStart.plusMonths(m);
			Map<String, Object> pt = new LinkedHashMap<>();
			pt.put("month", monthKey(monthDate));
			pt.put("type", "PREDICTED");
			pt.put("value", round2(predicted));
			pt.put("changeVsPrev", pctChange(prevForChange, predicted));
			series.add(pt);
			prevForChange = predicted;
			predictedFinal = predicted;
			predictedFinalMonth = monthKey(monthDate);
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("series", series);
		Map<String, Object> lastRecorded = new LinkedHashMap<>();
		lastRecorded.put("month", monthKey(historyMonths.get(2)));
		lastRecorded.put("value", round2(lastActual));
		result.put("lastRecorded", lastRecorded);
		Map<String, Object> predicted = new LinkedHashMap<>();
		predicted.put("month", predictedFinalMonth);
		predicted.put("value", round2(predictedFinal));
		result.put("predicted", predicted);
		result.put("expectedChangePercent", pctChange(lastActual, predictedFinal));
		return result;
	}

	private Map<String, Integer> monthlyCountsForType(String type, Integer districtId, LocalDate start, LocalDate end) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));
		Map<String, Integer> result = new HashMap<>();
		switch (type) {
			case "ADMISSION":
				addMonthly(result, nrcAdmissionRepository.countAdmissionsByMonth(districtId, startTs, endTs));
				break;
			case "DISCHARGE":
				addMonthly(result, nrcDischargeRepository.countDischargesByMonth(districtId, startTs, endTs));
				break;
			case "FOLLOWUP":
				addMonthly(result, nrcFollowupRepository.countFollowupVisitsByMonth(districtId, start, end));
				break;
			case "INPATIENTCARE":
				for (LocalDate monthStart = start.withDayOfMonth(1); !monthStart.isAfter(end); monthStart = monthStart.plusMonths(1)) {
					LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
					result.merge(monthKey(monthStart), activeAdmissionsCount(districtId, monthStart, monthEnd), Integer::sum);
				}
				break;
			case "ALL":
			default:
				// Sum of flow events (admissions+discharges+followup visits) - deliberately excludes
				// INPATIENTCARE, which is a stock/census measure, not a flow event; summing the two
				// would double-count the same children and misrepresent "All" activity.
				addMonthly(result, nrcAdmissionRepository.countAdmissionsByMonth(districtId, startTs, endTs));
				addMonthly(result, nrcDischargeRepository.countDischargesByMonth(districtId, startTs, endTs));
				addMonthly(result, nrcFollowupRepository.countFollowupVisitsByMonth(districtId, start, end));
				break;
		}
		return result;
	}

	private void addMonthly(Map<String, Integer> target, List<Object[]> rows) {
		for (Object[] r : rows) {
			String key = String.format("%04d-%02d-01", ((Number) r[0]).intValue(), ((Number) r[1]).intValue());
			target.merge(key, ((Number) r[2]).intValue(), Integer::sum);
		}
	}

	private int activeAdmissionsCount(Integer districtId, LocalDate monthStart, LocalDate monthEnd) {
		List<Integer> hIds = new ArrayList<>(hospitalToDistrictMap(districtsInScope(districtId)).keySet());
		if (hIds.isEmpty()) return 0;
		Timestamp startTs = Timestamp.valueOf(monthStart.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(monthEnd.atTime(23, 59, 59));
		int sum = 0;
		for (Object[] r : nrcAdmissionRepository.countActiveAdmissionsGroupedByHospital(hIds, startTs, endTs)) {
			sum += ((Number) r[1]).intValue();
		}
		return sum;
	}

	private static String monthKey(LocalDate d) {
		return String.format("%04d-%02d-01", d.getYear(), d.getMonthValue());
	}

	private static Double pctChange(double from, double to) {
		if (from == 0) return null;
		return round2(((to - from) / from) * 100);
	}

	private static double round2(double v) {
		return Math.round(v * 100.0) / 100.0;
	}
}
