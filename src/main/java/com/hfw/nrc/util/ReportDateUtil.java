package com.hfw.nrc.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Simplified port of nrcapp.util.ReportUtil#getReportingDatesForGivenDate.
 * The source method had a legacy branch with custom 26th-of-month cutoffs
 * for dates before Oct 2020; that branch is unreachable for any date from
 * 2020-10 onward (its own guard condition), which covers every date this
 * app will ever actually be asked about, so only the simple calendar-month
 * branch (YearMonth.atDay(1)/atEndOfMonth) is ported.
 */
public final class ReportDateUtil {

	private ReportDateUtil() {
	}

	/**
	 * @param currentMonth true = current calendar month, false = previous
	 *                     calendar month (matches the source's confusingly
	 *                     named "currFiscal" boolean, which is not a fiscal
	 *                     year at all - just current-vs-previous month).
	 * @return [startDate, endDate] as yyyy-MM-dd strings.
	 */
	public static List<String> reportingMonthRange(boolean currentMonth) {
		LocalDate today = LocalDate.now();
		YearMonth ym = currentMonth ? YearMonth.from(today) : YearMonth.from(today).minusMonths(1);
		DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
		return Arrays.asList(ym.atDay(1).format(fmt), ym.atEndOfMonth().format(fmt));
	}
}
