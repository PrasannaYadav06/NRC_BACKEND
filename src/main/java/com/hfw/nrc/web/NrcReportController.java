package com.hfw.nrc.web;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hfw.nrc.domain.Hospital;
import com.hfw.nrc.report.NrcReportExcelWriter;
import com.hfw.nrc.report.NrcReportPdfWriter;
import com.hfw.nrc.report.NrcReportService;
import com.hfw.nrc.report.NrcSummaryReportPdfWriter;
import com.hfw.nrc.report.ReportRowDto;
import com.hfw.nrc.report.SummaryReportRowDto;
import com.hfw.nrc.repository.HospitalRepository;
import com.hfw.nrc.util.SecurityUtils;

/**
 * District/Taluka report - port of old nrcapp's two report features (see the
 * approved plan): the admin statewide Excel download
 * (NRCGeneralController's GET /excelreport/{type}/{month}/{year} +
 * ExcelDashBoardView) and the per-hospital-login PDF
 * (NRCGeneralController's POST /getmonthlyreport/pdf + MonthlyReportPDFView).
 * Unlike the old system, both endpoints enforce real server-side scoping
 * (SecurityUtils) instead of trusting client-supplied ids - see the plan's
 * "Security fix" section.
 */
@RestController
public class NrcReportController {

	@Autowired
	private NrcReportService nrcReportService;
	@Autowired
	private HospitalRepository hospitalRepository;

	private static final NrcReportExcelWriter EXCEL_WRITER = new NrcReportExcelWriter();
	private static final NrcReportPdfWriter PDF_WRITER = new NrcReportPdfWriter();
	private static final NrcSummaryReportPdfWriter SUMMARY_PDF_WRITER = new NrcSummaryReportPdfWriter();

	/** Admin-only, statewide: every DISTRICT- or TALUKA-level hospital's row for the given month/year. */
	@GetMapping("/report/excel/{level}/{month}/{year}")
	public ResponseEntity<?> downloadStatewideExcel(@PathVariable("level") String level,
			@PathVariable("month") int month, @PathVariable("year") int year) {

		if (SecurityUtils.currentUser() == null || !SecurityUtils.currentUser().isAdmin()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Admin access required"));
		}
		String normalizedLevel = level.trim().toUpperCase();
		if (!"DISTRICT".equals(normalizedLevel) && !"TALUKA".equals(normalizedLevel)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "level must be DISTRICT or TALUKA"));
		}

		YearMonth ym;
		try {
			ym = YearMonth.of(year, month);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "Invalid month/year"));
		}
		LocalDate start = ym.atDay(1);
		LocalDate end = ym.atEndOfMonth();

		try {
			List<ReportRowDto> rows = nrcReportService.generateStatewideReport(normalizedLevel, start, end);
			boolean includeDistrictColumn = "TALUKA".equals(normalizedLevel);
			String title = normalizedLevel + " NRC Report - " + ym.getMonth() + " " + year;
			byte[] bytes = EXCEL_WRITER.write(title, rows, includeDistrictColumn);

			String filename = "NRC_" + normalizedLevel + "_Report_" + ym.getMonth() + "_" + year + ".xlsx";
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
					.body(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Failed to generate report: " + e.getMessage()));
		}
	}

	/** Any hospital-login user - scoped to their own hospital via SecurityUtils.resolveHospitalId, never the raw requested id. */
	@PostMapping("/report/monthly/pdf")
	public ResponseEntity<?> downloadMonthlyPdf(@RequestParam("hospitalId") Integer requestedHospitalId,
			@RequestParam("month") int month, @RequestParam("year") int year) {

		Integer resolvedHospitalId = SecurityUtils.resolveHospitalId(requestedHospitalId);
		if (SecurityUtils.isDenied(resolvedHospitalId) || resolvedHospitalId == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Not authenticated."));
		}

		YearMonth ym;
		try {
			ym = YearMonth.of(year, month);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "Invalid month/year"));
		}
		LocalDate start = ym.atDay(1);
		// For the current, still-in-progress month, cap the data (and the PDF's own
		// displayed range) at today rather than claiming data through a future date
		// that hasn't happened yet.
		LocalDate end = ym.equals(YearMonth.now()) ? LocalDate.now() : ym.atEndOfMonth();

		Hospital hospital = hospitalRepository.findById(resolvedHospitalId).orElse(null);
		if (hospital == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Hospital not found"));
		}

		try {
			com.hfw.nrc.report.MonthlyReportDetailDto detail = nrcReportService.computeMonthlyReportDetail(resolvedHospitalId, start, end);
			byte[] bytes = PDF_WRITER.write(hospital.getName(), start, end, detail);

			String filename = "NRC_Monthly_Report_" + ym.getMonth() + "_" + year + ".pdf";
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.APPLICATION_PDF)
					.body(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Failed to generate report: " + e.getMessage()));
		}
	}

	/** Any hospital-login user - scoped the same way as the Monthly PDF. Fiscal-year-to-date admission/discharge/death summary. */
	@PostMapping("/report/summary/pdf")
	public ResponseEntity<?> downloadSummaryPdf(@RequestParam("hospitalId") Integer requestedHospitalId,
			@RequestParam("endDate") String endDateStr) {

		Integer resolvedHospitalId = SecurityUtils.resolveHospitalId(requestedHospitalId);
		if (SecurityUtils.isDenied(resolvedHospitalId) || resolvedHospitalId == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.singletonMap("message", "Not authenticated."));
		}

		LocalDate endDate;
		try {
			endDate = LocalDate.parse(endDateStr);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "endDate must be yyyy-MM-dd"));
		}

		Hospital hospital = hospitalRepository.findById(resolvedHospitalId).orElse(null);
		if (hospital == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Hospital not found"));
		}

		try {
			List<SummaryReportRowDto> rows = nrcReportService.generateSummaryReport(resolvedHospitalId, endDate);
			LocalDate fyStart = NrcReportService.fiscalYearStart(endDate);
			byte[] bytes = SUMMARY_PDF_WRITER.write(hospital.getName(), fyStart, endDate, rows);

			String filename = "NRC_Summary_Report_" + endDate + ".pdf";
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
					.contentType(MediaType.APPLICATION_PDF)
					.body(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", "Failed to generate report: " + e.getMessage()));
		}
	}
}
