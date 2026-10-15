package com.hfw.nrc.report;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Shared, workbook-scoped POI style set for the District/Taluka report.
 * Ported from KABHI_BACKEND's com.hfw.kabhi.report.ExcelStyleFactory (this
 * project's own established precedent) - same teal palette, since
 * nrc_admin_frontend_ehealth's own CSS (--teal / --teal-tint / --ink) already
 * matches KABHI's design system.
 */
public class ExcelStyleFactory {

	private static final byte[] TEAL = new byte[] { (byte) 0x0B, (byte) 0x6E, (byte) 0x63 };
	private static final byte[] TEAL_STRONG = new byte[] { (byte) 0x08, (byte) 0x52, (byte) 0x49 };
	private static final byte[] TEAL_TINT = new byte[] { (byte) 0xE3, (byte) 0xF1, (byte) 0xEE };
	private static final byte[] INK = new byte[] { (byte) 0x14, (byte) 0x21, (byte) 0x1D };

	private final XSSFWorkbook workbook;

	private CellStyle titleStyle;
	private CellStyle groupHeaderStyle;
	private CellStyle subHeaderStyle;
	private CellStyle dataStyle;
	private CellStyle decimalDataStyle;
	private CellStyle districtLabelStyle;
	private CellStyle totalRowStyle;
	private CellStyle totalRowDecimalStyle;
	private CellStyle totalRowLabelStyle;
	private final short integerFormat;
	private final short decimalFormat;

	public ExcelStyleFactory(XSSFWorkbook workbook) {
		this.workbook = workbook;
		this.integerFormat = workbook.createDataFormat().getFormat("0");
		this.decimalFormat = workbook.createDataFormat().getFormat("0.00");
	}

	/** See KABHI_BACKEND's ExcelStyleFactory#xssfColor javadoc - setRGB(byte[]) avoids the POI 3.17 one-arg-constructor gap. */
	private static XSSFColor xssfColor(byte[] rgb) {
		XSSFColor color = new XSSFColor();
		color.setRGB(rgb);
		return color;
	}

	private XSSFCellStyle base() {
		XSSFCellStyle style = workbook.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setWrapText(true);
		return style;
	}

	public CellStyle title() {
		if (titleStyle == null) {
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 14);
			font.setColor(IndexedColors.WHITE.getIndex());
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillForegroundColor(xssfColor(TEAL_STRONG));
			titleStyle = style;
		}
		return titleStyle;
	}

	public CellStyle groupHeader() {
		if (groupHeaderStyle == null) {
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 10);
			font.setColor(IndexedColors.WHITE.getIndex());
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillForegroundColor(xssfColor(TEAL));
			groupHeaderStyle = style;
		}
		return groupHeaderStyle;
	}

	public CellStyle subHeader() {
		if (subHeaderStyle == null) {
			org.apache.poi.xssf.usermodel.XSSFFont font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 9);
			font.setColor(xssfColor(INK));
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillForegroundColor(xssfColor(TEAL_TINT));
			subHeaderStyle = style;
		}
		return subHeaderStyle;
	}

	public CellStyle data() {
		if (dataStyle == null) {
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setDataFormat(integerFormat);
			dataStyle = style;
		}
		return dataStyle;
	}

	public CellStyle decimalData() {
		if (decimalDataStyle == null) {
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 10);
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setDataFormat(decimalFormat);
			decimalDataStyle = style;
		}
		return decimalDataStyle;
	}

	public CellStyle districtLabel() {
		if (districtLabelStyle == null) {
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 10);
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setAlignment(HorizontalAlignment.LEFT);
			districtLabelStyle = style;
		}
		return districtLabelStyle;
	}

	public CellStyle totalRow() {
		if (totalRowStyle == null) {
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 10);
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillForegroundColor(xssfColor(TEAL_TINT));
			style.setDataFormat(integerFormat);
			totalRowStyle = style;
		}
		return totalRowStyle;
	}

	public CellStyle totalRowDecimal() {
		if (totalRowDecimalStyle == null) {
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 10);
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillForegroundColor(xssfColor(TEAL_TINT));
			style.setDataFormat(decimalFormat);
			totalRowDecimalStyle = style;
		}
		return totalRowDecimalStyle;
	}

	public CellStyle totalRowLabel() {
		if (totalRowLabelStyle == null) {
			Font font = workbook.createFont();
			font.setBold(true);
			font.setFontHeightInPoints((short) 10);
			font.setColor(IndexedColors.WHITE.getIndex());
			XSSFCellStyle style = base();
			style.setFont(font);
			style.setAlignment(HorizontalAlignment.LEFT);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			style.setFillForegroundColor(xssfColor(TEAL_STRONG));
			totalRowLabelStyle = style;
		}
		return totalRowLabelStyle;
	}
}
