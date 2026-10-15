package com.hfw.nrc.report;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Same 6-cell shape as GenderAgeBreakdown, for the two Monthly Report rows
 * that are averages (Average Length of Stay, Average Weight Gain) rather
 * than counts. Deliberate fix over the old system's own apparent approach
 * (its generic row-populator added already-divided per-cell averages
 * together for the Total columns, e.g. 13 + 12 = 25 - clearly not what the
 * real reference PDF shows, which has Total 6m-5y = 13 for exactly that
 * case): Total</GrandTotal here are TRUE weighted averages, derived by
 * combining the underlying sum/count pairs before dividing, not by
 * combining already-divided per-cell averages.
 */
public class GenderAgeAverage {

	private BigDecimal maleBelowSum = BigDecimal.ZERO; private int maleBelowCount;
	private BigDecimal maleAboveSum = BigDecimal.ZERO; private int maleAboveCount;
	private BigDecimal femaleBelowSum = BigDecimal.ZERO; private int femaleBelowCount;
	private BigDecimal femaleAboveSum = BigDecimal.ZERO; private int femaleAboveCount;

	public void add(String gender, boolean below6m, BigDecimal value) {
		boolean male = "M".equalsIgnoreCase(gender);
		if (male && below6m) { maleBelowSum = maleBelowSum.add(value); maleBelowCount++; }
		else if (male) { maleAboveSum = maleAboveSum.add(value); maleAboveCount++; }
		else if (below6m) { femaleBelowSum = femaleBelowSum.add(value); femaleBelowCount++; }
		else { femaleAboveSum = femaleAboveSum.add(value); femaleAboveCount++; }
	}

	private static BigDecimal avg(BigDecimal sum, int count) {
		return count == 0 ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
	}

	public BigDecimal maleBelow() { return avg(maleBelowSum, maleBelowCount); }
	public BigDecimal maleAbove() { return avg(maleAboveSum, maleAboveCount); }
	public BigDecimal femaleBelow() { return avg(femaleBelowSum, femaleBelowCount); }
	public BigDecimal femaleAbove() { return avg(femaleAboveSum, femaleAboveCount); }
	public BigDecimal totalBelow() { return avg(maleBelowSum.add(femaleBelowSum), maleBelowCount + femaleBelowCount); }
	public BigDecimal totalAbove() { return avg(maleAboveSum.add(femaleAboveSum), maleAboveCount + femaleAboveCount); }
	public BigDecimal grandTotal() {
		BigDecimal sum = maleBelowSum.add(maleAboveSum).add(femaleBelowSum).add(femaleAboveSum);
		int count = maleBelowCount + maleAboveCount + femaleBelowCount + femaleAboveCount;
		return avg(sum, count);
	}
}
