package com.hfw.nrc.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * .xlsx writer for the statewide District/Taluka report - one row per
 * hospital plus a TOTAL row, built on GridSheetWriter (this project's own
 * port of KABHI_BACKEND's POI-writer precedent). Column set matches old
 * nrcapp's ExcelDashBoardView/ExcelReportModel; see the approved plan's
 * "Column list" section for the authoritative order and the Formulas
 * section for how each value is computed (NrcReportService).
 *
 * GridSheetWriter's one text column is the row label - NRC User Id and NRC
 * Name (both strings in the old report) are folded into that single label
 * ("hospitalName (nrcUserId)", district-prefixed for taluka sheets) rather
 * than extending the writer for a second text column; every other old
 * column is a real number and becomes its own leaf.
 */
public class NrcReportExcelWriter {

	private static final List<ColumnSpec> COLUMNS = Arrays.asList(
			ColumnSpec.leaf("Beds", "beds"),
			ColumnSpec.group("Admissions by Caste",
					ColumnSpec.leaf("SC", "sc"),
					ColumnSpec.leaf("ST", "st"),
					ColumnSpec.leaf("GEN", "gen")),
			ColumnSpec.group("Admissions by Category",
					ColumnSpec.leaf("APL", "apl"),
					ColumnSpec.leaf("BPL", "bpl"),
					ColumnSpec.leaf("Others", "othersCat")),
			ColumnSpec.group("Admissions by Age/Gender",
					ColumnSpec.leaf("M<6m", "maleBelow"),
					ColumnSpec.leaf("M>=6m", "maleAbove"),
					ColumnSpec.leaf("F<6m", "femaleBelow"),
					ColumnSpec.leaf("F>=6m", "femaleAbove")),
			ColumnSpec.leaf("Total Admissions", "admissions"),
			ColumnSpec.group("Admission Criteria",
					ColumnSpec.leaf("WFH(<-3Z)", "wfh"),
					ColumnSpec.leaf("MUAC(<115mm)", "muac"),
					ColumnSpec.leaf("Oedema", "oedema"),
					ColumnSpec.leaf("Severe Wasting", "wasting")),
			ColumnSpec.group("Referral",
					ColumnSpec.leaf("Self", "self"),
					ColumnSpec.leaf("Paediatric Ward/OP", "ward"),
					ColumnSpec.leaf("FLW", "flw")),
			ColumnSpec.group("Duration of Stay",
					ColumnSpec.leaf("<7 days", "lt7"),
					ColumnSpec.leaf("7-15 days", "w7to15"),
					ColumnSpec.leaf(">15 days", "gt15")),
			ColumnSpec.leafDecimal("Avg Length of Stay", "avgStay"),
			ColumnSpec.leafDecimal("Bed Occupancy Rate %", "bedOccupancy"),
			ColumnSpec.leaf("Achieved Target Weight", "achievedTarget"),
			ColumnSpec.leaf("Achieved Partial Weight", "achievedPartial"),
			ColumnSpec.leafDecimal("Avg Weight Gain", "avgWeightGain"),
			ColumnSpec.leaf("Discharges", "discharges"),
			ColumnSpec.leaf("Recovered", "recovered"),
			ColumnSpec.leafDecimal("Recovery Rate %", "recoveryRate"),
			ColumnSpec.leaf("Defaulters", "defaulters"),
			ColumnSpec.leafDecimal("Defaulter Rate %", "defaulterRate"),
			ColumnSpec.leaf("Non-Responders", "nonResponders"),
			ColumnSpec.leaf("Deaths", "deaths"),
			ColumnSpec.leafDecimal("Death Rate %", "deathRate"),
			ColumnSpec.leaf("Children Due Followup", "dueFollowup"),
			ColumnSpec.leaf("Relapse", "relapse"),
			ColumnSpec.leaf("Readmission", "readmission"));

	private static final List<String> DECIMAL_KEYS = Arrays.asList(
			"avgStay", "bedOccupancy", "avgWeightGain", "recoveryRate", "defaulterRate", "deathRate");

	public byte[] write(String title, List<ReportRowDto> rows, boolean includeDistrictColumn) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("Dashboard data");
			ExcelStyleFactory styles = new ExcelStyleFactory(workbook);
			GridSheetWriter writer = new GridSheetWriter(sheet, styles, "NRC Hospital", COLUMNS);

			writer.writeTitle(title);
			writer.writeHeaders();

			Map<String, Number> runningSum = new HashMap<>();
			Map<String, java.util.List<BigDecimal>> decimalValues = new HashMap<>();
			for (String key : DECIMAL_KEYS) {
				decimalValues.put(key, new java.util.ArrayList<>());
			}

			for (ReportRowDto row : rows) {
				String label = rowLabel(row, includeDistrictColumn);
				Map<String, Number> values = toValueMap(row);
				writer.writeDataRow(label, values);

				for (Map.Entry<String, Number> e : values.entrySet()) {
					if (DECIMAL_KEYS.contains(e.getKey())) {
						decimalValues.get(e.getKey()).add(BigDecimal.valueOf(e.getValue().doubleValue()));
					} else {
						runningSum.merge(e.getKey(), e.getValue().intValue(), (a, b) -> a.intValue() + b.intValue());
					}
				}
			}

			Map<String, Number> totals = new HashMap<>(runningSum);
			for (String key : DECIMAL_KEYS) {
				java.util.List<BigDecimal> vals = decimalValues.get(key);
				if (vals.isEmpty()) {
					totals.put(key, BigDecimal.ZERO);
				} else {
					BigDecimal sum = vals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
					totals.put(key, sum.divide(BigDecimal.valueOf(vals.size()), 2, java.math.RoundingMode.HALF_UP));
				}
			}
			writer.writeTotalRow(totals);

			writer.applyUniformColumnWidths();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return out.toByteArray();
		}
	}

	private String rowLabel(ReportRowDto row, boolean includeDistrictColumn) {
		String base = (row.getNrcName() == null ? "Unknown" : row.getNrcName())
				+ (row.getNrcUserId() != null ? " (" + row.getNrcUserId() + ")" : "");
		if (includeDistrictColumn && row.getDistrictName() != null) {
			return row.getDistrictName() + " - " + base;
		}
		return base;
	}

	private Map<String, Number> toValueMap(ReportRowDto row) {
		Map<String, Number> m = new HashMap<>();
		m.put("beds", nz(row.getNoOfBeds()));
		m.put("sc", nz(row.getScCount()));
		m.put("st", nz(row.getStCount()));
		m.put("gen", nz(row.getGenCount()));
		m.put("apl", nz(row.getAplCount()));
		m.put("bpl", nz(row.getBplCount()));
		m.put("othersCat", nz(row.getOthersCount()));
		m.put("maleBelow", nz(row.getAdmissionMaleBelow()));
		m.put("maleAbove", nz(row.getAdmissionMaleAbove()));
		m.put("femaleBelow", nz(row.getAdmissionFemaleBelow()));
		m.put("femaleAbove", nz(row.getAdmissionFemaleAbove()));
		m.put("admissions", nz(row.getAdmissions()));
		m.put("wfh", nz(row.getWfhCount()));
		m.put("muac", nz(row.getMuacCount()));
		m.put("oedema", nz(row.getOedemaCount()));
		m.put("wasting", nz(row.getWastingCount()));
		m.put("self", nz(row.getSelfCount()));
		m.put("ward", nz(row.getWardCount()));
		m.put("flw", nz(row.getFlwCount()));
		m.put("lt7", nz(row.getLessThanOneWkCount()));
		m.put("w7to15", nz(row.getOneToTwoWkCount()));
		m.put("gt15", nz(row.getMoreThanTwoWkCount()));
		m.put("avgStay", nzd(row.getAverageLengthStay()));
		m.put("bedOccupancy", nzd(row.getBedOccupancyRate()));
		m.put("achievedTarget", nz(row.getAchievedTargetWeight()));
		m.put("achievedPartial", nz(row.getAchievedPartialWeight()));
		m.put("avgWeightGain", nzd(row.getAverageWeightGain()));
		m.put("discharges", nz(row.getDischarges()));
		m.put("recovered", nz(row.getRecovered()));
		m.put("recoveryRate", nzd(row.getRecoveryRate()));
		m.put("defaulters", nz(row.getDefaulters()));
		m.put("defaulterRate", nzd(row.getDefaulterRate()));
		m.put("nonResponders", nz(row.getNonResponders()));
		m.put("deaths", nz(row.getDeaths()));
		m.put("deathRate", nzd(row.getDeathRate()));
		m.put("dueFollowup", nz(row.getChildDueFollowup()));
		m.put("relapse", nz(row.getRelapse()));
		m.put("readmission", nz(row.getReadmission()));
		return m;
	}

	private static int nz(Integer i) {
		return i == null ? 0 : i;
	}

	private static BigDecimal nzd(BigDecimal b) {
		return b == null ? BigDecimal.ZERO : b;
	}
}
