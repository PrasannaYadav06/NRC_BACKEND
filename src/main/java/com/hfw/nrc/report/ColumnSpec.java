package com.hfw.nrc.report;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A node in the report sheet's column header tree. Ported from KABHI_BACKEND's
 * com.hfw.kabhi.report.ColumnSpec (this project's own established precedent
 * for a two-row-header POI sheet) - identical shape, just copied into this
 * Maven module since the two backends don't share a library.
 */
public final class ColumnSpec {

	public final String label;
	public final String key;
	public final boolean decimal;
	public final List<ColumnSpec> children;

	private ColumnSpec(String label, String key, boolean decimal, List<ColumnSpec> children) {
		this.label = label;
		this.key = key;
		this.decimal = decimal;
		this.children = children;
	}

	/** A plain integer-count data column. */
	public static ColumnSpec leaf(String label, String key) {
		return new ColumnSpec(label, key, false, Collections.emptyList());
	}

	/** A rate/average data column - rendered with 2-decimal formatting. */
	public static ColumnSpec leafDecimal(String label, String key) {
		return new ColumnSpec(label, key, true, Collections.emptyList());
	}

	public static ColumnSpec group(String label, ColumnSpec... children) {
		return new ColumnSpec(label, null, false, Arrays.asList(children));
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public int leafCount() {
		if (isLeaf()) {
			return 1;
		}
		int total = 0;
		for (ColumnSpec c : children) {
			total += c.leafCount();
		}
		return total;
	}

	public int depth() {
		if (isLeaf()) {
			return 1;
		}
		int maxChildDepth = 0;
		for (ColumnSpec c : children) {
			maxChildDepth = Math.max(maxChildDepth, c.depth());
		}
		return 1 + maxChildDepth;
	}
}
