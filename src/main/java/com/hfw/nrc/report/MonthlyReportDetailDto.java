package com.hfw.nrc.report;

import java.math.BigDecimal;

import com.hfw.nrc.dto.HrTypeModel;

/**
 * Full single-hospital Monthly Report data - every row/cell the user-supplied
 * reference PDF (February_MonthlyReport_2026.pdf) shows. Internal plumbing
 * between NrcReportService#computeMonthlyReportDetail and
 * NrcReportPdfWriter only (not serialized), so plain public fields rather
 * than getter/setter boilerplate.
 */
public class MonthlyReportDetailDto {

	// Header
	public String hospitalName;
	public String districtName;
	public String monthLabel;
	public java.time.LocalDate start;
	public java.time.LocalDate end;
	public Integer beds;

	// A. Admission
	public final GenderAgeBreakdown sc = new GenderAgeBreakdown();
	public final GenderAgeBreakdown st = new GenderAgeBreakdown();
	public final GenderAgeBreakdown othersCaste = new GenderAgeBreakdown();
	public final GenderAgeBreakdown numberOfAdmissions = new GenderAgeBreakdown();
	public final GenderAgeBreakdown apl = new GenderAgeBreakdown();
	public final GenderAgeBreakdown bpl = new GenderAgeBreakdown();

	// A.1 Admission Criteria
	public final GenderAgeBreakdown wflWfhLt3z = new GenderAgeBreakdown();
	public final GenderAgeBreakdown wflWfhLt2z = new GenderAgeBreakdown();
	public final GenderAgeBreakdown muacLt115 = new GenderAgeBreakdown();
	public final GenderAgeBreakdown muac115to125 = new GenderAgeBreakdown();
	public final GenderAgeBreakdown blEdema = new GenderAgeBreakdown();
	public final GenderAgeBreakdown severeWasting = new GenderAgeBreakdown();

	// A.2 Referral
	public final GenderAgeBreakdown frontlineWorker = new GenderAgeBreakdown();
	public final GenderAgeBreakdown self = new GenderAgeBreakdown();
	public final GenderAgeBreakdown paediatricWard = new GenderAgeBreakdown();
	public final GenderAgeBreakdown otherReferral = new GenderAgeBreakdown();

	// A.3 Duration of Stay
	public final GenderAgeBreakdown lt7 = new GenderAgeBreakdown();
	public final GenderAgeBreakdown w7to15 = new GenderAgeBreakdown();
	public final GenderAgeBreakdown gt15 = new GenderAgeBreakdown();
	public final GenderAgeAverage avgLengthStay = new GenderAgeAverage();

	// A.4 Bed Occupancy
	public BigDecimal bedOccupancyRate = BigDecimal.ZERO;

	// A.5 Weight gain
	public final GenderAgeBreakdown achievedTargetWeight = new GenderAgeBreakdown();
	public final GenderAgeBreakdown achievedPartialWeight = new GenderAgeBreakdown();
	public final GenderAgeAverage avgWeightGain = new GenderAgeAverage();

	// B. Monthly Output
	public final GenderAgeBreakdown discharge = new GenderAgeBreakdown();
	public final GenderAgeBreakdown recovered = new GenderAgeBreakdown();
	public BigDecimal recoveryRate = BigDecimal.ZERO;
	public final GenderAgeBreakdown referred = new GenderAgeBreakdown();
	public final GenderAgeBreakdown defaulters = new GenderAgeBreakdown();
	public BigDecimal defaulterRate = BigDecimal.ZERO;
	public final GenderAgeBreakdown nonResponders = new GenderAgeBreakdown();
	public final GenderAgeBreakdown deaths = new GenderAgeBreakdown();
	public BigDecimal deathRate = BigDecimal.ZERO;
	public final GenderAgeBreakdown childrenDueFollowup = new GenderAgeBreakdown();
	public final GenderAgeBreakdown childrenFollowedUp = new GenderAgeBreakdown();
	public final GenderAgeBreakdown deathsDuringFollowup = new GenderAgeBreakdown();
	public final GenderAgeBreakdown relapse = new GenderAgeBreakdown();
	public final GenderAgeBreakdown readmission = new GenderAgeBreakdown();

	// Human Resource in place
	public HrTypeModel officers;
	public HrTypeModel nurses;
	public HrTypeModel dieticians;
	public HrTypeModel cooks;
	public HrTypeModel attenders;
}
