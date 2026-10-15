package com.hfw.nrc.report;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Single-hospital Monthly Report PDF - rebuilt to match the user-supplied
 * reference (February_MonthlyReport_2026.pdf) exactly: header info block,
 * a 7-column Male/Female x &lt;6m/6m-5yrs + Total + Grand Total grid across
 * every admission/criteria/referral/duration/weight/outcome/follow-up row,
 * and an HR staffing table. See MonthlyReportDetailDto /
 * NrcReportService#computeMonthlyReportDetail for the data this renders.
 */
public class NrcReportPdfWriter {

	private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	private static final Font INFO_LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
	private static final Font INFO_VALUE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);
	private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
	private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
	private static final Font ROW_LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);
	private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);
	private static final Font RATE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
	private static final BaseColor TEAL = new BaseColor(0x0B, 0x6E, 0x63);
	private static final BaseColor TEAL_TINT = new BaseColor(0xE3, 0xF1, 0xEE);

	public byte[] write(String hospitalName, java.time.LocalDate start, java.time.LocalDate end, MonthlyReportDetailDto dto) throws Exception {
		Document document = new Document(PageSize.A4.rotate(), 24, 24, 36, 24);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, out);
		document.open();

		Paragraph title = new Paragraph("Nutritional Rehabilitation Centers and Modified Nutritional Rehabilitation Centre", TITLE_FONT);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);
		Paragraph subtitle = new Paragraph("Monthly Reporting Format", TITLE_FONT);
		subtitle.setAlignment(Element.ALIGN_CENTER);
		subtitle.setSpacingAfter(8);
		document.add(subtitle);

		document.add(buildInfoTable(hospitalName, start, end, dto));
		document.add(buildMainGrid(dto));
		document.add(buildHrTable(dto));

		document.close();
		return out.toByteArray();
	}

	private PdfPTable buildInfoTable(String hospitalName, java.time.LocalDate start, java.time.LocalDate end, MonthlyReportDetailDto dto) {
		PdfPTable table = new PdfPTable(new float[] { 1f, 3f });
		table.setWidthPercentage(60);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setSpacingAfter(8);

		addInfoRow(table, "Name of health facility:", hospitalName);
		addInfoRow(table, "Block:", "Paediatric NRC");
		addInfoRow(table, "District:", dto.districtName);
		DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMM yyyy");
		boolean partialMonth = end.isBefore(java.time.YearMonth.from(start).atEndOfMonth());
		String rangeText = partialMonth
				? "(till " + end.format(df) + ")"
				: "(" + start.format(df) + " to " + end.format(df) + ")";
		addInfoRow(table, "Month:", dto.monthLabel + "  " + rangeText);
		addInfoRow(table, "Number of Beds:", dto.beds == null ? "0" : String.valueOf(dto.beds));
		return table;
	}

	private void addInfoRow(PdfPTable table, String label, String value) {
		PdfPCell labelCell = new PdfPCell(new Phrase(label, INFO_LABEL_FONT));
		labelCell.setBorder(0);
		labelCell.setPadding(2);
		PdfPCell valueCell = new PdfPCell(new Phrase(value == null ? "" : value, INFO_VALUE_FONT));
		valueCell.setBorder(0);
		valueCell.setPadding(2);
		table.addCell(labelCell);
		table.addCell(valueCell);
	}

	private PdfPTable buildMainGrid(MonthlyReportDetailDto dto) {
		PdfPTable table = new PdfPTable(new float[] { 3f, 1f, 1f, 1f, 1f, 1f, 1f, 1.2f });
		table.setWidthPercentage(100);

		// Two-row header: Male/Female/Total group + <6m/6m-5yrs sub-columns, Grand Total spanning both header rows.
		addHeaderSpan(table, "", 1, 2);
		addHeaderSpan(table, "Male", 2, 1);
		addHeaderSpan(table, "Female", 2, 1);
		addHeaderSpan(table, "Total", 2, 1);
		addHeaderSpan(table, "Grand Total", 1, 2);
		addHeaderCell(table, "< 6m");
		addHeaderCell(table, "6m to 5yrs");
		addHeaderCell(table, "< 6m");
		addHeaderCell(table, "6m to 5yrs");
		addHeaderCell(table, "< 6m");
		addHeaderCell(table, "6m to 5yrs");

		addSectionHeader(table, "A. Admission, Below 6 months / 6m to 5 years");
		addRow(table, "S/C", dto.sc);
		addRow(table, "S/T", dto.st);
		addRow(table, "Others", dto.othersCaste);
		addRow(table, "Number of Admissions", dto.numberOfAdmissions);
		addRow(table, "APL", dto.apl);
		addRow(table, "BPL", dto.bpl);

		addSectionHeader(table, "A.1 Admission Criteria");
		addRow(table, "< -3 SD WFH (Weight for Height/length)", dto.wflWfhLt3z);
		addRow(table, "< -2 SD WFH (Weight for Height/length)", dto.wflWfhLt2z);
		addRow(table, "MUAC < 11.5cm (RED)", dto.muacLt115);
		addRow(table, "MUAC 11.5cm to 12.5cm (YELLOW)", dto.muac115to125);
		addRow(table, "Bilateral pitting oedema", dto.blEdema);
		addRow(table, "Visible severe Wasting", dto.severeWasting);

		addSectionHeader(table, "A.2 Referral by");
		addRow(table, "Frontline Worker (ASHA/HW/AWW/RBSK)", dto.frontlineWorker);
		addRow(table, "Self", dto.self);
		addRow(table, "Paediatric ward/emergency/OPD", dto.paediatricWard);
		addRow(table, "Others", dto.otherReferral);

		addSectionHeader(table, "A.3 Duration of Stay");
		addRow(table, "< 7 days", dto.lt7);
		addRow(table, "7-15 days", dto.w7to15);
		addRow(table, "> 15 days", dto.gt15);
		addRow(table, "Average length of stay", dto.avgLengthStay);

		addSectionHeader(table, "A.4 Bed Occupancy");
		addRateRow(table, "Bed Occupancy rate", dto.bedOccupancyRate);

		addSectionHeader(table, "A.5 Weight gain");
		addRow(table, "Achieved Target Weight (15% weight gain)", dto.achievedTargetWeight);
		addRow(table, "Achieved Partial Weight Gain", dto.achievedPartialWeight);
		addRow(table, "Average weight gain (gm/kg/day)", dto.avgWeightGain);

		addSectionHeader(table, "B. Monthly Output");
		addRow(table, "1 Discharge from NRC", dto.discharge);
		addRow(table, "1.2 Improved(Recovered)", dto.recovered);
		addRateRow(table, "1.3 Recovery (cure) rate", dto.recoveryRate);
		addRow(table, "2 Referred", dto.referred);
		addRow(table, "3 Defaulters", dto.defaulters);
		addRateRow(table, "3.2 Defaulters rate", dto.defaulterRate);
		addRow(table, "4 Non responders", dto.nonResponders);
		addRow(table, "5 Deaths", dto.deaths);
		addRateRow(table, "5.2 Death rate", dto.deathRate);
		addRow(table, "6 Children due for follow-up (in the month)", dto.childrenDueFollowup);
		addRow(table, "7 Children followed up during the month", dto.childrenFollowedUp);
		addRow(table, "8 Deaths during follow-up period", dto.deathsDuringFollowup);
		addRow(table, "9 Relapse", dto.relapse);
		addRow(table, "10 Re-admission", dto.readmission);

		return table;
	}

	private PdfPTable buildHrTable(MonthlyReportDetailDto dto) {
		PdfPTable table = new PdfPTable(new float[] { 2.5f, 1f, 1f, 1f, 1f });
		table.setWidthPercentage(60);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setSpacingBefore(10);

		addHeaderCell(table, "Human Resource in place");
		addHeaderCell(table, "Sanctioned");
		addHeaderCell(table, "Working");
		addHeaderCell(table, "Vacant");
		addHeaderCell(table, "Training");

		addHrRow(table, "Paediatrician/Medical Officer", dto.officers);
		addHrRow(table, "Staff Nurse", dto.nurses);
		addHrRow(table, "Dietician", dto.dieticians);
		addHrRow(table, "Cook", dto.cooks);
		addHrRow(table, "Attender", dto.attenders);

		return table;
	}

	private void addHrRow(PdfPTable table, String label, com.hfw.nrc.dto.HrTypeModel hr) {
		addCell(table, label, Element.ALIGN_LEFT, ROW_LABEL_FONT);
		addCell(table, hr == null || hr.getSanctioned() == null ? "0" : String.valueOf(hr.getSanctioned()), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, hr == null || hr.getWorking() == null ? "0" : String.valueOf(hr.getWorking()), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, hr == null || hr.getVacant() == null ? "0" : String.valueOf(hr.getVacant()), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, hr == null || hr.getTraining() == null ? "0" : String.valueOf(hr.getTraining()), Element.ALIGN_CENTER, CELL_FONT);
	}

	private void addSectionHeader(PdfPTable table, String label) {
		PdfPCell cell = new PdfPCell(new Phrase(label, SECTION_FONT));
		cell.setColspan(8);
		cell.setBackgroundColor(TEAL);
		cell.setPadding(4);
		table.addCell(cell);
	}

	private void addHeaderSpan(PdfPTable table, String label, int rowspan, int colspan) {
		PdfPCell cell = new PdfPCell(new Phrase(label, HEADER_FONT));
		cell.setRowspan(rowspan);
		cell.setColspan(colspan);
		cell.setBackgroundColor(TEAL);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPadding(3);
		table.addCell(cell);
	}

	private void addHeaderCell(PdfPTable table, String text) {
		PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
		cell.setBackgroundColor(TEAL);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(3);
		table.addCell(cell);
	}

	private void addRow(PdfPTable table, String label, GenderAgeBreakdown b) {
		addCell(table, label, Element.ALIGN_LEFT, ROW_LABEL_FONT);
		addCell(table, String.valueOf(b.maleBelow), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, String.valueOf(b.maleAbove), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, String.valueOf(b.femaleBelow), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, String.valueOf(b.femaleAbove), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, String.valueOf(b.totalBelow()), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, String.valueOf(b.totalAbove()), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, String.valueOf(b.grandTotal()), Element.ALIGN_CENTER, RATE_FONT);
	}

	private void addRow(PdfPTable table, String label, GenderAgeAverage avg) {
		addCell(table, label, Element.ALIGN_LEFT, ROW_LABEL_FONT);
		addCell(table, avg.maleBelow().toPlainString(), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, avg.maleAbove().toPlainString(), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, avg.femaleBelow().toPlainString(), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, avg.femaleAbove().toPlainString(), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, avg.totalBelow().toPlainString(), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, avg.totalAbove().toPlainString(), Element.ALIGN_CENTER, CELL_FONT);
		addCell(table, avg.grandTotal().toPlainString(), Element.ALIGN_CENTER, RATE_FONT);
	}

	private void addRateRow(PdfPTable table, String label, BigDecimal rate) {
		PdfPCell labelCell = new PdfPCell(new Phrase(label, ROW_LABEL_FONT));
		labelCell.setColspan(7);
		labelCell.setPadding(3);
		table.addCell(labelCell);
		PdfPCell valueCell = new PdfPCell(new Phrase((rate == null ? BigDecimal.ZERO : rate).toPlainString() + "%", RATE_FONT));
		valueCell.setBackgroundColor(TEAL_TINT);
		valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		valueCell.setPadding(3);
		table.addCell(valueCell);
	}

	private void addCell(PdfPTable table, String text, int align, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setHorizontalAlignment(align);
		cell.setPadding(3);
		table.addCell(cell);
	}
}
