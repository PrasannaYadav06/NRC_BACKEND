package com.hfw.nrc.analytics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.util.RchoScopeResolver;
import com.hfw.nrc.util.SecurityUtils;

/**
 * NRC Analytics - new separate frontend app (nrc_analytics_frontend, port
 * 4201 dev - already allow-listed in application-local.properties). Mirrors
 * KABHI's AnalyticsController shape exactly (see the approved plan):
 * ADMIN/RCHO/HOSPITAL_USER class-level gate, districtId scoping via the
 * same RchoScopeResolver In-Patient Care already uses, plain Map rows.
 * HOSPITAL_USER (district/taluka/CHC/PHC login) was added later, at the
 * user's explicit request - they get the exact same district-scoped view
 * RCHO does (RchoScopeResolver resolves their own hospital's district),
 * but NOT the per-taluka drill-down within it - see
 * #talukasForDistrict's own tighter override below.
 */
@RestController
@RequestMapping("/analytics")
@PreAuthorize("hasAnyAuthority('ADMIN','RCHO','HOSPITAL_USER')")
public class NrcAnalyticsController {

	@Autowired
	private NrcAnalyticsService service;
	@Autowired
	private RchoScopeResolver rchoScopeResolver;

	@GetMapping("/admissionDischarge")
	public List<Map<String, Object>> admissionDischarge(
			@RequestParam(required = false) Integer startYear,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer districtId) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		LocalDate[] range = service.resolveDateRange(startYear, month);
		return service.admissionDischarge(range[0], range[1], resolved);
	}

	@GetMapping("/inPatientCare")
	public List<Map<String, Object>> inPatientCare(
			@RequestParam(required = false) Integer startYear,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer districtId) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		LocalDate[] range = service.resolveDateRange(startYear, month);
		return service.inPatientCare(range[0], range[1], resolved);
	}

	/** Forecast page (was Outcome Distribution) - simple linear-trend projection, see NrcAnalyticsService#forecast's javadoc. type: ALL/ADMISSION/INPATIENTCARE/DISCHARGE/FOLLOWUP. window: 3/6/9/12 (months ahead). */
	@GetMapping("/forecast")
	public Map<String, Object> forecast(
			@RequestParam(required = false, defaultValue = "ALL") String type,
			@RequestParam(required = false) Integer districtId,
			@RequestParam(required = false, defaultValue = "3") Integer window) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		return service.forecast(type, resolved, window);
	}

	@GetMapping("/outcomeDistribution")
	public List<Map<String, Object>> outcomeDistribution(
			@RequestParam(required = false) Integer startYear,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer districtId) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		LocalDate[] range = service.resolveDateRange(startYear, month);
		return service.outcomeDistribution(range[0], range[1], resolved);
	}

	@GetMapping("/bedOccupancy")
	public List<Map<String, Object>> bedOccupancy(
			@RequestParam(required = false) Integer startYear,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer districtId) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		LocalDate[] range = service.resolveDateRange(startYear, month);
		return service.bedOccupancy(range[0], range[1], resolved);
	}

	/** Bed Occupancy comprehensive dashboard's in-built worksheet - one row per NRC, not per district. Same ADMIN/RCHO scoping as every other endpoint here (RCHO sees their own district only). */
	@GetMapping("/bedOccupancyWorksheet")
	public List<Map<String, Object>> bedOccupancyWorksheet(
			@RequestParam(required = false) Integer startYear,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer districtId) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		LocalDate[] range = service.resolveDateRange(startYear, month);
		return service.bedOccupancyWorksheet(range[0], range[1], resolved);
	}

	@GetMapping("/hr")
	public List<Map<String, Object>> hr(
			@RequestParam String role,
			@RequestParam(required = false) Integer districtId,
			@RequestParam(required = false) Integer fyStartYear) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		return service.hr(role, resolved, fyStartYear);
	}

	/** HR Staffing tooltip - all-designation breakdown per district, same shape as In-Patient Care's map tooltip. */
	@GetMapping("/hrDesignationBreakdown")
	public List<Map<String, Object>> hrDesignationBreakdown(
			@RequestParam(required = false) Integer districtId,
			@RequestParam(required = false) Integer fyStartYear) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		return service.hrDesignationBreakdown(resolved, fyStartYear);
	}

	@GetMapping("/hrGauge")
	@PreAuthorize("hasAuthority('ADMIN')")
	public Map<String, Object> hrGauge(@RequestParam(required = false) Integer fyStartYear) {
		return service.hrGauge(fyStartYear);
	}

	/** Karnataka Facilities page - plain From/To date range (not the FY-anchored startYear/month the other dashboards use), same as the reference design's own From/To picker. ADMIN/RCHO only (not HOSPITAL_USER) - this is statewide, un-scoped data with no districtId filtering at all, unlike every other endpoint here; method-level @PreAuthorize overrides the class-level one for just this endpoint. */
	@GetMapping("/facilities")
	@PreAuthorize("hasAnyAuthority('ADMIN','RCHO')")
	public List<Map<String, Object>> facilities(
			@RequestParam("start") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam("end") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate end) {
		return service.facilities(start, end);
	}

	/** Per-taluka breakdown within a district - deliberately NOT opened to HOSPITAL_USER along with the rest of this controller: a taluka/CHC/PHC-level login is scoped to its own district (RchoScopeResolver), but every OTHER taluka inside that district is still not "their own" data. Method-level @PreAuthorize overrides the class-level one for just this endpoint. */
	@GetMapping("/talukas/{districtId}")
	@PreAuthorize("hasAnyAuthority('ADMIN','RCHO')")
	public List<Map<String, Object>> talukasForDistrict(
			@PathVariable Integer districtId,
			@RequestParam(required = false) Integer startYear,
			@RequestParam(required = false) Integer month) {
		Integer resolved = rchoScopeResolver.resolveDistrictId(districtId, SecurityUtils.currentUser());
		LocalDate[] range = service.resolveDateRange(startYear, month);
		return service.talukasForDistrict(resolved != null ? resolved : districtId, range[0], range[1]);
	}
}
