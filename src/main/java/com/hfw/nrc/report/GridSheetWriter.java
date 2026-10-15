package com.hfw.nrc.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Two-row merged-header sheet writer for the District/Taluka report. Ported
 * from KABHI_BACKEND's com.hfw.kabhi.report.GridSheetWriter (this project's
 * own established precedent for a POI-based writer) - same merge/style
 * mechanics, adapted from Map&lt;String,Integer&gt; to Map&lt;String,Number&gt;
 * since this report's rate/average columns are BigDecimal, not int.
 */
public class GridSheetWriter {

	private final Sheet sheet;
	private final ExcelStyleFactory styles;
	private final String firstColumnLabel;
	private final List<ColumnSpec> dataColumns;
	private final List<ColumnSpec> leafColumns = new ArrayList<>();
	private final int headerDepth;
	private int nextRow = 0;

	public GridSheetWriter(Sheet sheet, ExcelStyleFactory styles, String firstColumnLabel,
			List<ColumnSpec> dataColumns) {
		this.sheet = sheet;
		this.styles = styles;
		this.firstColumnLabel = firstColumnLabel;
		this.dataColumns = dataColumns;

		int maxDepth = 1;
		for (ColumnSpec c : dataColumns) {
			maxDepth = Math.max(maxDepth, c.depth());
			collectLeaves(c);
		}
		this.headerDepth = maxDepth;
	}

	private void collectLeaves(ColumnSpec c) {
		if (c.isLeaf()) {
			leafColumns.add(c);
		} else {
			for (ColumnSpec child : c.children) {
				collectLeaves(child);
			}
		}
	}

	public int totalColumns() {
		return 1 + leafColumns.size();
	}

	public void writeTitle(String title) {
		Row row = sheet.createRow(nextRow);
		row.setHeightInPoints(30);
		Cell cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(styles.title());
		for (int c = 1; c < totalColumns(); c++) {
			row.createCell(c).setCellStyle(styles.title());
		}
		if (totalColumns() > 1) {
			sheet.addMergedRegion(new CellRangeAddress(nextRow, nextRow, 0, totalColumns() - 1));
		}
		nextRow++;
	}

	public int writeHeaders() {
		int headerStartRow = nextRow;
		for (int r = 0; r < headerDepth; r++) {
			Row headerRow = sheet.createRow(headerStartRow + r);
			headerRow.setHeightInPoints(32);
		}

		Row firstHeaderRow = sheet.getRow(headerStartRow);
		Cell firstColCell = firstHeaderRow.createCell(0);
		firstColCell.setCellValue(firstColumnLabel);
		firstColCell.setCellStyle(styles.groupHeader());
		if (headerDepth > 1) {
			sheet.addMergedRegion(new CellRangeAddress(headerStartRow, headerStartRow + headerDepth - 1, 0, 0));
			for (int r = 1; r < headerDepth; r++) {
				sheet.getRow(headerStartRow + r).createCell(0).setCellStyle(styles.groupHeader());
			}
		}

		int[] colCursor = { 1 };
		for (ColumnSpec c : dataColumns) {
			writeHeaderNode(c, headerStartRow, 0, colCursor);
		}

		nextRow = headerStartRow + headerDepth;
		return nextRow;
	}

	private void writeHeaderNode(ColumnSpec node, int headerStartRow, int depth, int[] colCursor) {
		int row = headerStartRow + depth;
		int startCol = colCursor[0];
		int span = node.leafCount();
		int endCol = startCol + span - 1;

		Row r = sheet.getRow(row);
		Cell cell = r.createCell(startCol);
		cell.setCellValue(node.label);
		CellStyle style = (depth == 0) ? styles.groupHeader() : styles.subHeader();
		cell.setCellStyle(style);
		for (int c = startCol + 1; c <= endCol; c++) {
			r.createCell(c).setCellStyle(style);
		}
		if (endCol > startCol) {
			sheet.addMergedRegion(new CellRangeAddress(row, row, startCol, endCol));
		}

		if (node.isLeaf()) {
			int lastHeaderRow = headerStartRow + headerDepth - 1;
			if (row < lastHeaderRow) {
				sheet.addMergedRegion(new CellRangeAddress(row, lastHeaderRow, startCol, startCol));
				for (int rr = row + 1; rr <= lastHeaderRow; rr++) {
					sheet.getRow(rr).createCell(startCol).setCellStyle(style);
				}
			}
			colCursor[0] = endCol + 1;
		} else {
			int childCol = startCol;
			for (ColumnSpec child : node.children) {
				colCursor[0] = childCol;
				writeHeaderNode(child, headerStartRow, depth + 1, colCursor);
				childCol = colCursor[0];
			}
			colCursor[0] = endCol + 1;
		}
	}

	/** One data row. Values are keyed by each leaf column's `key`; a missing key reads as 0. */
	public int writeDataRow(String rowLabel, Map<String, Number> values) {
		Row row = sheet.createRow(nextRow);
		Cell label = row.createCell(0);
		label.setCellValue(rowLabel);
		label.setCellStyle(styles.districtLabel());

		int col = 1;
		for (ColumnSpec leafCol : leafColumns) {
			Number v = values.getOrDefault(leafCol.key, 0);
			Cell cell = row.createCell(col);
			cell.setCellValue(v.doubleValue());
			cell.setCellStyle(leafCol.decimal ? styles.decimalData() : styles.data());
			col++;
		}
		int written = nextRow;
		nextRow++;
		return written;
	}

	/** Bottom "Total" row - caller supplies the already-summed/averaged totals per leaf key. */
	public void writeTotalRow(Map<String, Number> totals) {
		Row row = sheet.createRow(nextRow);
		Cell label = row.createCell(0);
		label.setCellValue("Total");
		label.setCellStyle(styles.totalRowLabel());

		int col = 1;
		for (ColumnSpec leafCol : leafColumns) {
			Number v = totals.getOrDefault(leafCol.key, 0);
			Cell cell = row.createCell(col);
			cell.setCellValue(v.doubleValue());
			cell.setCellStyle(leafCol.decimal ? styles.totalRowDecimal() : styles.totalRow());
			col++;
		}
		nextRow++;
	}

	private static final int LABEL_COLUMN_WIDTH = 26 * 256;
	private static final int DATA_COLUMN_WIDTH = 13 * 256;

	/** Same reasoning as KABHI_BACKEND's GridSheetWriter#applyUniformColumnWidths javadoc - deliberate, uniform widths instead of unreliable autoSizeColumn(). */
	public void applyUniformColumnWidths() {
		sheet.setColumnWidth(0, LABEL_COLUMN_WIDTH);
		for (int c = 1; c < totalColumns(); c++) {
			sheet.setColumnWidth(c, DATA_COLUMN_WIDTH);
		}
	}
}
