package com.hfw.nrc.report;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Summary Report PDF - matches the old nrcapp system's real output exactly
 * (user-supplied reference: "NRC {hospital} / Admission Records Report /
 * {ordinal date range}", 4-column table, bold Grand Total row). See
 * NrcReportService#generateSummaryReport for the data.
 */
public class NrcSummaryReportPdfWriter {

	private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
	private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
	private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
	private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
	private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	private static final BaseColor TEAL = new BaseColor(0x0B, 0x6E, 0x63);
	private static final BaseColor TEAL_TINT = new BaseColor(0xE3, 0xF1, 0xEE);

	public byte[] write(String hospitalName, LocalDate fyStart, LocalDate endDate, List<SummaryReportRowDto> rows) throws Exception {
		Document document = new Document(PageSize.A4, 36, 36, 54, 36);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, out);
		document.open();

		Paragraph title = new Paragraph("NRC " + hospitalName, TITLE_FONT);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);

		Paragraph subtitle = new Paragraph("Admission Records Report", SUBTITLE_FONT);
		subtitle.setAlignment(Element.ALIGN_CENTER);
		document.add(subtitle);

		Paragraph range = new Paragraph(ordinalDate(fyStart) + " to " + ordinalDate(endDate), CELL_FONT);
		range.setAlignment(Element.ALIGN_CENTER);
		range.setSpacingAfter(12);
		document.add(range);

		PdfPTable table = new PdfPTable(new float[] { 0.6f, 1.6f, 1.2f, 1.2f, 1.2f });
		table.setWidthPercentage(100);

		addHeaderCell(table, "Sl No.");
		addHeaderCell(table, "Month");
		addHeaderCell(table, "Total No. of Admissions");
		addHeaderCell(table, "Total No. of Discharge");
		addHeaderCell(table, "Total No. of Death");

		int sl = 1;
		int totalAdmissions = 0, totalDischarges = 0, totalDeaths = 0;
		for (SummaryReportRowDto row : rows) {
			addCell(table, String.valueOf(sl++), Element.ALIGN_CENTER);
			addCell(table, row.getMonthLabel(), Element.ALIGN_LEFT);
			addCell(table, String.valueOf(row.getAdmissions()), Element.ALIGN_CENTER);
			addCell(table, String.valueOf(row.getDischarges()), Element.ALIGN_CENTER);
			addCell(table, String.valueOf(row.getDeaths()), Element.ALIGN_CENTER);
			totalAdmissions += row.getAdmissions();
			totalDischarges += row.getDischarges();
			totalDeaths += row.getDeaths();
		}

		PdfPCell grandLabel = new PdfPCell(new Paragraph("Grand Total", TOTAL_FONT));
		grandLabel.setColspan(2);
		grandLabel.setBackgroundColor(TEAL_TINT);
		grandLabel.setPadding(5);
		table.addCell(grandLabel);
		addTotalCell(table, String.valueOf(totalAdmissions));
		addTotalCell(table, String.valueOf(totalDischarges));
		addTotalCell(table, String.valueOf(totalDeaths));

		document.add(table);
		document.close();
		return out.toByteArray();
	}

	private void addHeaderCell(PdfPTable table, String text) {
		PdfPCell cell = new PdfPCell(new Paragraph(text, HEADER_FONT));
		cell.setBackgroundColor(TEAL);
		cell.setPadding(5);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
	}

	private void addCell(PdfPTable table, String text, int align) {
		PdfPCell cell = new PdfPCell(new Paragraph(text, CELL_FONT));
		cell.setPadding(5);
		cell.setHorizontalAlignment(align);
		table.addCell(cell);
	}

	private void addTotalCell(PdfPTable table, String text) {
		PdfPCell cell = new PdfPCell(new Paragraph(text, TOTAL_FONT));
		cell.setBackgroundColor(TEAL_TINT);
		cell.setPadding(5);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell);
	}

	private String ordinalDate(LocalDate date) {
		int day = date.getDayOfMonth();
		String suffix;
		if (day >= 11 && day <= 13) {
			suffix = "th";
		} else {
			switch (day % 10) {
				case 1: suffix = "st"; break;
				case 2: suffix = "nd"; break;
				case 3: suffix = "rd"; break;
				default: suffix = "th";
			}
		}
		String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
		return day + suffix + " " + month + " " + date.getYear();
	}
}
