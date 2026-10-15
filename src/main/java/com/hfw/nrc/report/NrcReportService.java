package com.hfw.nrc.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hfw.nrc.domain.AdmissionType;
import com.hfw.nrc.domain.Caste;
import com.hfw.nrc.domain.District;
import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.domain.HospitalDetail;
import com.hfw.nrc.domain.MalnutritionStatus;
import com.hfw.nrc.domain.NrcAdmission;
import com.hfw.nrc.domain.NrcDischarge;
import com.hfw.nrc.domain.NrcFollowup;
import com.hfw.nrc.domain.NrcUser;
import com.hfw.nrc.domain.Outcome;
import com.hfw.nrc.domain.ReferredFrom;
import com.hfw.nrc.dto.AbstractResponse;
import com.hfw.nrc.dto.HrTypeModel;
import com.hfw.nrc.dto.ResourceRequestModel;
import com.hfw.nrc.dto.ResourceResponseModel;
import com.hfw.nrc.repository.AdmissionTypeRepository;
import com.hfw.nrc.repository.CasteRepository;
import com.hfw.nrc.repository.DistrictRepository;
import com.hfw.nrc.repository.HospitalDetailRepository;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.repository.MalnutritionStatusRepository;
import com.hfw.nrc.repository.NrcAdmissionRepository;
import com.hfw.nrc.repository.NrcDischargeRepository;
import com.hfw.nrc.repository.NrcFollowupRepository;
import com.hfw.nrc.repository.NrcUserRepository;
import com.hfw.nrc.repository.OutcomeRepository;
import com.hfw.nrc.repository.ReferredFromRepository;
import com.hfw.nrc.service.ResourceService;

/**
 * District/Taluka report - direct port of old nrcapp's
 * NRCReportServiceImpl#generateMonthlyReport / #generateExcelReport (see
 * cstep.nrc.rest.service.NRCReportServiceImpl in
 * D:\Projects\BACKEND\NRC Backend\NRCMiddleware_ehealth\nrcapp), with its
 * real formulas confirmed from the old nr.* stored procedures (bedoccupancyrate,
 * r2_avg_no_of_days, r2_discharge_admission_with_type, r2_referral,
 * r2_followupcount, etc. - see the approved plan for the full read-out) rather
 * than guessed. Two deliberate fixes over the old system, both called out in
 * the plan: averageLengthStay/averageWeightGain are true averages here (the
 * old system summed four gender/age-bucket averages together instead); and
 * deathRate's zero-guard checks totalAdmissions, not dischargeTotal.
 */
@Service
public class NrcReportService {

	private static final ZoneId ZONE = ZoneId.systemDefault();

	@Autowired
	private HospitalRepository hospitalRepository;
	@Autowired
	private HospitalDetailRepository hospitalDetailRepository;
	@Autowired
	private NrcUserRepository nrcUserRepository;
	@Autowired
	private NrcAdmissionRepository nrcAdmissionRepository;
	@Autowired
	private NrcDischargeRepository nrcDischargeRepository;
	@Autowired
	private DistrictRepository districtRepository;
	@Autowired
	private OutcomeRepository outcomeRepository;
	@Autowired
	private ReferredFromRepository referredFromRepository;
	@Autowired
	private AdmissionTypeRepository admissionTypeRepository;
	@Autowired
	private CasteRepository casteRepository;
	@Autowired
	private MalnutritionStatusRepository malnutritionStatusRepository;
	@Autowired
	private NrcFollowupRepository nrcFollowupRepository;
	@Autowired
	private ResourceService resourceService;

	private static final int[] FOLLOWUP_INTERVALS = { 7, 15, 30, 60 };

	/** Statewide report (NrcReportController's admin-only Excel endpoint): one row per hospital of the given level, plus a TOTAL row. */
	public List<ReportRowDto> generateStatewideReport(String hospitalLevel, LocalDate start, LocalDate end) {
		List<Hospital> hospitals = hospitalRepository.findByHospitalLevel(hospitalLevel);
		List<ReportRowDto> rows = new ArrayList<>();
		for (Hospital h : hospitals) {
			ReportRowDto row = computeHospitalReportRow(h.getHId(), start, end);
			if ("TALUKA".equalsIgnoreCase(hospitalLevel)) {
				districtRepository.findById((long) (int) h.getDistrictId())
						.ifPresent(d -> row.setDistrictName(d.getDistrictName()));
			}
			rows.add(row);
		}
		if ("TALUKA".equalsIgnoreCase(hospitalLevel)) {
			rows.sort((a, b) -> safe(a.getDistrictName()).compareToIgnoreCase(safe(b.getDistrictName())));
		}
		return rows;
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	/**
	 * Summary Report (NrcReportController's /report/summary/pdf) - direct port of old nrcapp's
	 * NRCReportServiceImpl#generateSummaryReport + DAO call `exec nr.r1_adm_dis_death_cnt`. One
	 * row per fiscal-year month (India FY: Apr-Mar) from FY start through endDate's month: count
	 * admissions by admissionDate, discharges by dischargeDate (any outcome), deaths by
	 * dischargeDate with outcome='Death'. Reuses the same repository methods
	 * computeHospitalReportRow already established - no new queries.
	 */
	public List<SummaryReportRowDto> generateSummaryReport(int hospitalId, LocalDate endDate) {
		LocalDate fyStart = fiscalYearStart(endDate);
		Map<Integer, String> outcomeNames = toIdNameMap(outcomeRepository.findAll(), Outcome::getOutcomeId, Outcome::getOutcome);

		List<SummaryReportRowDto> rows = new ArrayList<>();
		java.time.YearMonth cursor = java.time.YearMonth.from(fyStart);
		java.time.YearMonth last = java.time.YearMonth.from(endDate);
		while (!cursor.isAfter(last)) {
			LocalDate monthStart = cursor.atDay(1);
			LocalDate monthEnd = cursor.equals(last) ? endDate : cursor.atEndOfMonth();
			Timestamp startTs = Timestamp.valueOf(monthStart.atStartOfDay());
			Timestamp endTs = Timestamp.valueOf(monthEnd.atTime(23, 59, 59));

			int admissions = nrcAdmissionRepository.findByHIdAndAdmissionDateBetween(hospitalId, startTs, endTs).size();
			List<NrcDischarge> discharges = nrcDischargeRepository.findByHospitalAndDischargeDateBetween(hospitalId, startTs, endTs);
			int deaths = 0;
			for (NrcDischarge d : discharges) {
				if ("Death".equalsIgnoreCase(outcomeNames.get(d.getOutcomeId()))) {
					deaths++;
				}
			}

			String monthLabel = cursor.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
					+ " - " + cursor.getYear();
			rows.add(new SummaryReportRowDto(monthLabel, admissions, discharges.size(), deaths));
			cursor = cursor.plusMonths(1);
		}
		return rows;
	}

	/** India fiscal year: Apr 1 - Mar 31. Jan/Feb/Mar belong to the FY that started the PREVIOUS calendar year. */
	public static LocalDate fiscalYearStart(LocalDate date) {
		int fyStartYear = date.getMonthValue() <= 3 ? date.getYear() - 1 : date.getYear();
		return LocalDate.of(fyStartYear, 4, 1);
	}

	/** One hospital's full report row for [start,end] - shared by both the statewide Excel report and the single-hospital PDF. */
	public ReportRowDto computeHospitalReportRow(int hospitalId, LocalDate start, LocalDate end) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));

		ReportRowDto row = new ReportRowDto();

		Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
		row.setNrcName(hospital != null ? hospital.getName() : null);

		Optional<NrcUser> user = nrcUserRepository.findFirstByHospitalId(hospitalId);
		row.setNrcUserId(user.map(NrcUser::getUserId).orElse(null));

		row.setNoOfBeds(resolveBedCount(hospitalId));

		List<NrcAdmission> admissionsInRange = nrcAdmissionRepository
				.findByHIdAndAdmissionDateBetween(hospitalId, startTs, endTs);
		List<NrcDischarge> dischargesInRange = nrcDischargeRepository
				.findByHospitalAndDischargeDateBetween(hospitalId, startTs, endTs);

		// Was a findById() call per discharge - a real N+1 nested inside the
		// statewide report's own per-hospital loop (generateStatewideReport),
		// so a TALUKA-level report (~198 hospitals) could fire thousands of
		// individual lookups total - the actual cause of "report download is
		// slow/looks stuck". One batched findAllById instead, regardless of
		// how many discharges this hospital has.
		List<Integer> dischargeCaseIds = new ArrayList<>();
		for (NrcDischarge d : dischargesInRange) dischargeCaseIds.add(d.getCaseId());
		Map<Integer, NrcAdmission> admissionByCaseId = new HashMap<>();
		if (!dischargeCaseIds.isEmpty()) {
			for (NrcAdmission a : nrcAdmissionRepository.findAllById(dischargeCaseIds)) {
				admissionByCaseId.put(a.getCaseId(), a);
			}
		}

		Map<Integer, String> casteNames = toIdNameMap(casteRepository.findAll(), Caste::getCasteId, Caste::getCaste);
		Map<Integer, String> outcomeNames = toIdNameMap(outcomeRepository.findAll(), Outcome::getOutcomeId, Outcome::getOutcome);
		Map<Integer, String> referredFromNames = toIdNameMap(referredFromRepository.findAll(), ReferredFrom::getReferredFromId, ReferredFrom::getReferredFrom);
		Map<Integer, String> admissionTypeNames = toIdNameMap(admissionTypeRepository.findAll(), AdmissionType::getAdmissionTypeId, AdmissionType::getAdmissionType);

		// ===== Admission-side columns =====
		int sc = 0, st = 0, gen = 0, apl = 0, bpl = 0, others = 0;
		int maleBelow = 0, maleAbove = 0, femaleBelow = 0, femaleAbove = 0;
		int wfh = 0, muac = 0, oedema = 0, wasting = 0;
		int self = 0, ward = 0, flw = 0;
		int relapse = 0, readmission = 0;

		for (NrcAdmission a : admissionsInRange) {
			boolean below6m = a.getChildAgeInMonths() != null && a.getChildAgeInMonths() < 6;
			boolean male = "M".equalsIgnoreCase(a.getChildGender());
			if (male && below6m) maleBelow++;
			else if (male) maleAbove++;
			else if (below6m) femaleBelow++;
			else femaleAbove++;

			String caste = casteNames.get(a.getChildCasteId());
			if ("SC".equalsIgnoreCase(caste)) sc++;
			else if ("ST".equalsIgnoreCase(caste)) st++;
			else if ("GEN".equalsIgnoreCase(caste)) gen++;

			String category = a.getChildCategory();
			if ("APL".equalsIgnoreCase(category)) apl++;
			else if ("BPL".equalsIgnoreCase(category)) bpl++;
			else if ("Others".equalsIgnoreCase(category)) others++;

			if (Boolean.TRUE.equals(a.getAcWflWfhLt3z())) wfh++;
			if (Boolean.TRUE.equals(a.getAcMuacLt115())) muac++;
			if (Boolean.TRUE.equals(a.getAcBlEdema())) oedema++;
			if (Boolean.TRUE.equals(a.getAcSevereWasting())) wasting++;

			String referredFrom = referredFromNames.get(a.getReferredFromId());
			if (referredFrom != null) {
				if ("Self".equalsIgnoreCase(referredFrom)) self++;
				else if ("Paediatric Ward / OP".equalsIgnoreCase(referredFrom)) ward++;
				else if ("AWW".equalsIgnoreCase(referredFrom) || "ASHA".equalsIgnoreCase(referredFrom)
						|| "FHW".equalsIgnoreCase(referredFrom) || "RBSK/DEIC".equalsIgnoreCase(referredFrom)) flw++;
				// Private Doctor / Private Person / PHC / Others: excluded, matching the old report exactly.
			}

			String admissionType = admissionTypeNames.get(a.getAdmissionTypeId());
			if ("Relapse".equalsIgnoreCase(admissionType)) relapse++;
			else if ("Readmission".equalsIgnoreCase(admissionType)) readmission++;
		}

		row.setScCount(sc);
		row.setStCount(st);
		row.setGenCount(gen);
		row.setAplCount(apl);
		row.setBplCount(bpl);
		row.setOthersCount(others);
		row.setAdmissionMaleBelow(maleBelow);
		row.setAdmissionMaleAbove(maleAbove);
		row.setAdmissionFemaleBelow(femaleBelow);
		row.setAdmissionFemaleAbove(femaleAbove);
		row.setAdmissions(admissionsInRange.size());
		row.setWfhCount(wfh);
		row.setMuacCount(muac);
		row.setOedemaCount(oedema);
		row.setWastingCount(wasting);
		row.setSelfCount(self);
		row.setWardCount(ward);
		row.setFlwCount(flw);
		row.setRelapse(relapse);
		row.setReadmission(readmission);

		// ===== Discharge-side columns =====
		int lessThanWk = 0, oneToTwoWk = 0, moreThanTwoWk = 0;
		int discharges = dischargesInRange.size();
		int recovered = 0, defaulters = 0, nonResponders = 0, deaths = 0;
		int achievedTargetWeight = 0;
		long totalStayDays = 0;
		int stayCount = 0;
		BigDecimal totalWeightGain = BigDecimal.ZERO;
		int weightGainCount = 0;

		for (NrcDischarge d : dischargesInRange) {
			NrcAdmission a = admissionByCaseId.get(d.getCaseId());
			if (a == null || a.getAdmissionDate() == null || d.getDischargeDate() == null) continue;

			long days = java.time.temporal.ChronoUnit.DAYS.between(
					a.getAdmissionDate().toLocalDateTime().toLocalDate(),
					d.getDischargeDate().toLocalDateTime().toLocalDate());
			totalStayDays += days;
			stayCount++;
			if (days < 7) lessThanWk++;
			else if (days <= 15) oneToTwoWk++;
			else moreThanTwoWk++;

			if (d.getAvgWeightGainKg() != null) {
				totalWeightGain = totalWeightGain.add(d.getAvgWeightGainKg());
				weightGainCount++;
			}

			if (Boolean.TRUE.equals(a.getTargetWeightAchieved())) achievedTargetWeight++;

			String outcome = outcomeNames.get(d.getOutcomeId());
			if (outcome != null) {
				if ("Improved".equalsIgnoreCase(outcome)) recovered++;
				else if ("LAMA".equalsIgnoreCase(outcome)) defaulters++;
				else if ("Non Respondent".equalsIgnoreCase(outcome)) nonResponders++;
				else if ("Death".equalsIgnoreCase(outcome)) deaths++;
			}
		}

		row.setLessThanOneWkCount(lessThanWk);
		row.setOneToTwoWkCount(oneToTwoWk);
		row.setMoreThanTwoWkCount(moreThanTwoWk);
		row.setDischarges(discharges);
		row.setRecovered(recovered);
		row.setDefaulters(defaulters);
		row.setNonResponders(nonResponders);
		row.setDeaths(deaths);
		row.setAchievedTargetWeight(achievedTargetWeight);
		row.setAchievedPartialWeight(discharges - achievedTargetWeight);

		row.setAverageLengthStay(stayCount == 0 ? BigDecimal.ZERO
				: BigDecimal.valueOf(totalStayDays).divide(BigDecimal.valueOf(stayCount), 2, RoundingMode.HALF_UP));
		row.setAverageWeightGain(weightGainCount == 0 ? BigDecimal.ZERO
				: totalWeightGain.divide(BigDecimal.valueOf(weightGainCount), 2, RoundingMode.HALF_UP));

		int totalAdmissionsForRate = admissionsInRange.size();
		row.setDeathRate(totalAdmissionsForRate == 0 ? BigDecimal.ZERO : rate(deaths, totalAdmissionsForRate));
		row.setDefaulterRate(discharges == 0 ? BigDecimal.ZERO : rate(defaulters, discharges));
		row.setRecoveryRate(discharges == 0 ? BigDecimal.ZERO : rate(recovered, discharges));

		// ===== Children Due for Followup: sum of 4 interval-buckets, old system's own definition =====
		int dueFollowup = 0;
		for (int interval : FOLLOWUP_INTERVALS) {
			dueFollowup += countDueForFollowup(hospitalId, interval, startTs, endTs);
		}
		row.setChildDueFollowup(dueFollowup);

		// ===== Bed Occupancy Rate: exact port of nr.bedoccupancyrate =====
		row.setBedOccupancyRate(computeBedOccupancyRate(hospitalId, start, end, row.getNoOfBeds()));

		return row;
	}

	/**
	 * Full Monthly Report (NrcReportController's /report/monthly/pdf) - every row the user-supplied
	 * reference PDF (February_MonthlyReport_2026.pdf) shows, each as a full gender x age-bucket
	 * breakdown. Independent of computeHospitalReportRow (the Excel report's grand-totals-only
	 * computation, unchanged) - this walks the same admission/discharge data at finer granularity.
	 */
	public MonthlyReportDetailDto computeMonthlyReportDetail(int hospitalId, LocalDate start, LocalDate end) {
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));

		MonthlyReportDetailDto dto = new MonthlyReportDetailDto();
		dto.start = start;
		dto.end = end;

		Hospital hospital = hospitalRepository.findById(hospitalId).orElse(null);
		dto.hospitalName = hospital != null ? hospital.getName() : null;
		if (hospital != null && hospital.getDistrictId() != null) {
			districtRepository.findById((long) (int) hospital.getDistrictId())
					.ifPresent(d -> dto.districtName = d.getDistrictName());
		}
		dto.monthLabel = start.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + " " + start.getYear();
		dto.beds = resolveBedCount(hospitalId);

		Map<Integer, String> casteNames = toIdNameMap(casteRepository.findAll(), Caste::getCasteId, Caste::getCaste);
		Map<Integer, String> outcomeNames = toIdNameMap(outcomeRepository.findAll(), Outcome::getOutcomeId, Outcome::getOutcome);
		Map<Integer, String> referredFromNames = toIdNameMap(referredFromRepository.findAll(), ReferredFrom::getReferredFromId, ReferredFrom::getReferredFrom);
		Map<Integer, String> admissionTypeNames = toIdNameMap(admissionTypeRepository.findAll(), AdmissionType::getAdmissionTypeId, AdmissionType::getAdmissionType);

		List<NrcAdmission> admissionsInRange = nrcAdmissionRepository.findByHIdAndAdmissionDateBetween(hospitalId, startTs, endTs);
		List<NrcDischarge> dischargesInRange = nrcDischargeRepository.findByHospitalAndDischargeDateBetween(hospitalId, startTs, endTs);

		// ===== A. Admission + A.1 Admission Criteria + A.2 Referral =====
		for (NrcAdmission a : admissionsInRange) {
			boolean below6m = a.getChildAgeInMonths() != null && a.getChildAgeInMonths() < 6;
			String gender = a.getChildGender();

			dto.numberOfAdmissions.add(gender, below6m);

			String caste = casteNames.get(a.getChildCasteId());
			if ("SC".equalsIgnoreCase(caste)) dto.sc.add(gender, below6m);
			else if ("ST".equalsIgnoreCase(caste)) dto.st.add(gender, below6m);
			else dto.othersCaste.add(gender, below6m);

			String category = a.getChildCategory();
			if ("APL".equalsIgnoreCase(category)) dto.apl.add(gender, below6m);
			else if ("BPL".equalsIgnoreCase(category)) dto.bpl.add(gender, below6m);

			if (Boolean.TRUE.equals(a.getAcWflWfhLt3z())) dto.wflWfhLt3z.add(gender, below6m);
			if (Boolean.TRUE.equals(a.getAcWflWfhLt2z())) dto.wflWfhLt2z.add(gender, below6m);
			if (Boolean.TRUE.equals(a.getAcMuacLt115())) dto.muacLt115.add(gender, below6m);
			if (Boolean.TRUE.equals(a.getAcMuac115125())) dto.muac115to125.add(gender, below6m);
			if (Boolean.TRUE.equals(a.getAcBlEdema())) dto.blEdema.add(gender, below6m);
			if (Boolean.TRUE.equals(a.getAcSevereWasting())) dto.severeWasting.add(gender, below6m);

			String referredFrom = referredFromNames.get(a.getReferredFromId());
			if (referredFrom != null) {
				if ("Self".equalsIgnoreCase(referredFrom)) dto.self.add(gender, below6m);
				else if ("Paediatric Ward / OP".equalsIgnoreCase(referredFrom)) dto.paediatricWard.add(gender, below6m);
				else if ("Others".equalsIgnoreCase(referredFrom)) dto.otherReferral.add(gender, below6m);
				else if ("AWW".equalsIgnoreCase(referredFrom) || "ASHA".equalsIgnoreCase(referredFrom)
						|| "FHW".equalsIgnoreCase(referredFrom) || "RBSK/DEIC".equalsIgnoreCase(referredFrom)) {
					dto.frontlineWorker.add(gender, below6m);
				}
				// Private Doctor / Private Person / PHC: excluded, matching the old report exactly.
			}

			String admissionType = admissionTypeNames.get(a.getAdmissionTypeId());
			if ("Relapse".equalsIgnoreCase(admissionType)) dto.relapse.add(gender, below6m);
			else if ("Readmission".equalsIgnoreCase(admissionType)) dto.readmission.add(gender, below6m);
		}

		// ===== A.3 Duration of Stay, A.5 Weight gain, B. Monthly Output (discharge-side) =====
		for (NrcDischarge d : dischargesInRange) {
			NrcAdmission a = nrcAdmissionRepository.findById(d.getCaseId()).orElse(null);
			if (a == null || a.getAdmissionDate() == null || d.getDischargeDate() == null) continue;
			boolean below6m = a.getChildAgeInMonths() != null && a.getChildAgeInMonths() < 6;
			String gender = a.getChildGender();

			dto.discharge.add(gender, below6m);

			long days = java.time.temporal.ChronoUnit.DAYS.between(
					a.getAdmissionDate().toLocalDateTime().toLocalDate(),
					d.getDischargeDate().toLocalDateTime().toLocalDate());
			if (days < 7) dto.lt7.add(gender, below6m);
			else if (days <= 15) dto.w7to15.add(gender, below6m);
			else dto.gt15.add(gender, below6m);
			dto.avgLengthStay.add(gender, below6m, BigDecimal.valueOf(days));

			if (d.getAvgWeightGainKg() != null) {
				dto.avgWeightGain.add(gender, below6m, d.getAvgWeightGainKg());
			}
			if (Boolean.TRUE.equals(a.getTargetWeightAchieved())) {
				dto.achievedTargetWeight.add(gender, below6m);
			}

			String outcome = outcomeNames.get(d.getOutcomeId());
			if (outcome != null) {
				if ("Improved".equalsIgnoreCase(outcome)) dto.recovered.add(gender, below6m);
				else if ("LAMA".equalsIgnoreCase(outcome)) dto.defaulters.add(gender, below6m);
				else if ("Non Respondent".equalsIgnoreCase(outcome)) dto.nonResponders.add(gender, below6m);
				else if ("Death".equalsIgnoreCase(outcome)) dto.deaths.add(gender, below6m);
				else if ("Referred".equalsIgnoreCase(outcome)) dto.referred.add(gender, below6m);
			}
		}

		// Achieved Partial Weight Gain = discharges - target-achieved, per gender/age cell (verified against the reference's own numbers).
		dto.achievedPartialWeight.maleBelow = dto.discharge.maleBelow - dto.achievedTargetWeight.maleBelow;
		dto.achievedPartialWeight.maleAbove = dto.discharge.maleAbove - dto.achievedTargetWeight.maleAbove;
		dto.achievedPartialWeight.femaleBelow = dto.discharge.femaleBelow - dto.achievedTargetWeight.femaleBelow;
		dto.achievedPartialWeight.femaleAbove = dto.discharge.femaleAbove - dto.achievedTargetWeight.femaleAbove;

		// Rates
		dto.recoveryRate = dto.discharge.grandTotal() == 0 ? BigDecimal.ZERO : rate(dto.recovered.grandTotal(), dto.discharge.grandTotal());
		dto.defaulterRate = dto.discharge.grandTotal() == 0 ? BigDecimal.ZERO : rate(dto.defaulters.grandTotal(), dto.discharge.grandTotal());
		dto.deathRate = dto.numberOfAdmissions.grandTotal() == 0 ? BigDecimal.ZERO : rate(dto.deaths.grandTotal(), dto.numberOfAdmissions.grandTotal());

		// Bed Occupancy
		dto.bedOccupancyRate = computeBedOccupancyRate(hospitalId, start, end, dto.beds);

		// ===== Follow-up (real SCHEDULE/VISIT rows, not date-arithmetic - see the plan) =====
		List<NrcFollowup> dueRows = nrcFollowupRepository.findByHospitalAndRecordTypeAndFollowupDateBetween(hospitalId, "SCHEDULE", start, end);
		List<NrcFollowup> visitRows = nrcFollowupRepository.findByHospitalAndRecordTypeAndFollowupDateBetween(hospitalId, "VISIT", start, end);

		Integer deathStatusId = null;
		for (MalnutritionStatus ms : malnutritionStatusRepository.findAll()) {
			if ("Death".equalsIgnoreCase(ms.getMalnutrition())) {
				deathStatusId = ms.getStatusId();
				break;
			}
		}

		for (NrcFollowup f : dueRows) {
			addFollowupToBreakdown(dto.childrenDueFollowup, f);
		}
		for (NrcFollowup f : visitRows) {
			addFollowupToBreakdown(dto.childrenFollowedUp, f);
			if (deathStatusId != null && deathStatusId.equals(f.getStatusId())) {
				addFollowupToBreakdown(dto.deathsDuringFollowup, f);
			}
		}

		// ===== Human Resource in place (ResourceService already computes exactly this) =====
		nrcUserRepository.findFirstByHospitalId(hospitalId).ifPresent(user -> {
			ResourceRequestModel req = new ResourceRequestModel();
			req.setLoginUserId(user.getuId());
			AbstractResponse resp = resourceService.getResources(req);
			if (resp.getResponseObject() instanceof ResourceResponseModel) {
				ResourceResponseModel rm = (ResourceResponseModel) resp.getResponseObject();
				dto.officers = firstOrEmpty(rm.getOfficerList());
				dto.nurses = firstOrEmpty(rm.getNurseList());
				dto.dieticians = firstOrEmpty(rm.getDieticianList());
				dto.cooks = firstOrEmpty(rm.getCookList());
				dto.attenders = firstOrEmpty(rm.getTenderList());
			}
		});

		return dto;
	}

	private static HrTypeModel firstOrEmpty(List<HrTypeModel> list) {
		return (list != null && !list.isEmpty()) ? list.get(0) : new HrTypeModel();
	}

	/** NrcFollowup carries no gender/age directly - resolved via its parent NrcAdmission (caseId). */
	private void addFollowupToBreakdown(GenderAgeBreakdown breakdown, NrcFollowup f) {
		nrcAdmissionRepository.findById(f.getCaseId()).ifPresent(a -> {
			boolean below6m = a.getChildAgeInMonths() != null && a.getChildAgeInMonths() < 6;
			breakdown.add(a.getChildGender(), below6m);
		});
	}

	private <T> Map<Integer, String> toIdNameMap(List<T> entities, java.util.function.Function<T, Integer> idFn,
			java.util.function.Function<T, String> nameFn) {
		Map<Integer, String> map = new HashMap<>();
		for (T e : entities) {
			map.put(idFn.apply(e), nameFn.apply(e));
		}
		return map;
	}

	private BigDecimal rate(int numerator, int denominator) {
		return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private Integer resolveBedCount(int hospitalId) {
		List<HospitalDetail> details = hospitalDetailRepository.findLatestForHospital(hospitalId);
		for (HospitalDetail hd : details) {
			if (hd.getHrTypeId() == 25 && hd.getValue() != null) {
				return hd.getValue();
			}
		}
		return 0;
	}

	/**
	 * "Due for followup" (nr.r2_followupcount, one interval at a time): a discharge whose
	 * dischargeDate + interval days falls inside [start,end].
	 */
	private int countDueForFollowup(int hospitalId, int interval, Timestamp start, Timestamp end) {
		LocalDate startDate = start.toLocalDateTime().toLocalDate();
		LocalDate endDate = end.toLocalDateTime().toLocalDate();
		// A discharge is "due" at this interval if dischargeDate is in
		// [startDate - interval, endDate - interval] - equivalent to the old
		// SQL's `start <= dischargeDate+interval <= end`, solved for dischargeDate.
		Timestamp dueRangeStart = Timestamp.valueOf(startDate.minusDays(interval).atStartOfDay());
		Timestamp dueRangeEnd = Timestamp.valueOf(endDate.minusDays(interval).atTime(23, 59, 59));
		return nrcDischargeRepository.findByHospitalAndDischargeDateBetween(hospitalId, dueRangeStart, dueRangeEnd).size();
	}

	/** Direct port of old nrcapp's nr.bedoccupancyrate stored procedure. */
	private BigDecimal computeBedOccupancyRate(int hospitalId, LocalDate start, LocalDate end, Integer beds) {
		if (beds == null || beds == 0) {
			return BigDecimal.ZERO;
		}
		Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(end.atTime(23, 59, 59));
		List<NrcAdmission> active = nrcAdmissionRepository.findActiveDuringPeriod(hospitalId, startTs, endTs);

		long patientDays = 0;
		for (NrcAdmission a : active) {
			if (a.getAdmissionDate() == null) continue;
			LocalDate admStart = a.getAdmissionDate().toLocalDateTime().toLocalDate();
			LocalDate effectiveStart = admStart.isBefore(start) ? start : admStart;

			LocalDate admEnd;
			if (a.getLastDischargeDate() != null) {
				LocalDate dischargeDate = a.getLastDischargeDate().toLocalDateTime().toLocalDate();
				admEnd = dischargeDate.isAfter(end) ? end : dischargeDate;
			} else {
				admEnd = end;
			}
			long days = java.time.temporal.ChronoUnit.DAYS.between(effectiveStart, admEnd) + 1;
			if (days > 0) {
				patientDays += days;
			}
		}

		long periodDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
		BigDecimal denominator = BigDecimal.valueOf(beds).multiply(BigDecimal.valueOf(periodDays));
		if (denominator.signum() == 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(patientDays).multiply(BigDecimal.valueOf(100))
				.divide(denominator, 2, RoundingMode.HALF_UP);
	}
}
