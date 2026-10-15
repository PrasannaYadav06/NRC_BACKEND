package com.hfw.nrc.report;

/**
 * One Monthly Report row's 6-cell gender x age-bucket breakdown (Male <6m /
 * Male 6m-5y / Female <6m / Female 6m-5y / Total <6m / Total 6m-5y), plus a
 * Grand Total - matches the user-supplied reference PDF's own table shape
 * exactly. Total<6m/Total6m-5y are the TRUE combined count for that age
 * bucket across both genders (not a separate query) - count data, so simple
 * addition is correct here (see GenderAgeAverage for why rate/average rows
 * need different handling).
 */
public class GenderAgeBreakdown {

	public int maleBelow;
	public int maleAbove;
	public int femaleBelow;
	public int femaleAbove;

	public void add(String gender, boolean below6m) {
		addN(gender, below6m, 1);
	}

	public void addN(String gender, boolean below6m, int n) {
		boolean male = "M".equalsIgnoreCase(gender);
		if (male && below6m) maleBelow += n;
		else if (male) maleAbove += n;
		else if (below6m) femaleBelow += n;
		else femaleAbove += n;
	}

	public int totalBelow() { return maleBelow + femaleBelow; }
	public int totalAbove() { return maleAbove + femaleAbove; }
	public int grandTotal() { return totalBelow() + totalAbove(); }
}
