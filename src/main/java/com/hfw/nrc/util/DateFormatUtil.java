package com.hfw.nrc.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Ported from nrcapp.util.DateFormatUtil - only the methods actually used
 * by the ported endpoints (the source class has ~25 methods, most unused
 * by any active - non-dead-commented - endpoint).
 */
public final class DateFormatUtil {

	private DateFormatUtil() {
	}

	public static String getDateInddMMyyyyString(Date dateToFormat) {
		return new SimpleDateFormat("dd/MM/yyyy").format(dateToFormat);
	}

	public static String getDateInyyyyMMddString(Date dateToFormat) {
		return new SimpleDateFormat("yyyy-MM-dd").format(dateToFormat);
	}

	/**
	 * Despite the source method's name (...hhMMssStringFromTimestamp), it only
	 * ever formatted dd/MM/yyyy - no time component. Kept identical on purpose.
	 */
	public static String getDateInddMMyyyyhhMMssStringFromTimestamp(Timestamp timestamp) {
		return new SimpleDateFormat("dd/MM/yyyy").format(timestamp);
	}

	public static Date getDateFromyyyyMMddDashedString(String dateString) throws ParseException {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		fmt.setLenient(false);
		return fmt.parse(dateString);
	}

	public static Timestamp getTimestampFromDashedDateString(String dateString) throws ParseException {
		return new Timestamp(getDateFromyyyyMMddDashedString(dateString).getTime());
	}

	public static String getDateInyyyyMMddStringFromTimestamp(Timestamp timestamp) {
		return new SimpleDateFormat("yyyy-MM-dd").format(timestamp);
	}

	public static Date priorNDate(Date fromDate, int noOfDaysPrior) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(fromDate);
		cal.add(Calendar.DATE, -1 * noOfDaysPrior);
		return cal.getTime();
	}

	/**
	 * Ported verbatim from nrcapp.util.DateFormatUtil#isThisDateWithinThisMonth.
	 * fromDate == null: true if dateToValidate falls within (yesterday, today+29
	 * days) - i.e. "due this month" relative to now. fromDate != null: true if
	 * dateToValidate falls within (fromDate, fromDate+31 days).
	 */
	public static boolean isThisDateWithinThisMonth(String dateToValidate, String fromDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		try {
			Date date = sdf.parse(dateToValidate);
			Calendar cFromDate = null;
			Calendar cAfterMonthsFromDate = null;
			Calendar currentDateToday = null;
			Calendar currentDateAfterMonth = null;

			if (fromDate != null) {
				Date dt = sdf.parse(fromDate);
				cFromDate = Calendar.getInstance();
				cFromDate.setTime(dt);
				cAfterMonthsFromDate = Calendar.getInstance();
				cAfterMonthsFromDate.setTime(dt);
				cAfterMonthsFromDate.add(Calendar.DATE, 31);
			} else {
				currentDateToday = Calendar.getInstance();
				currentDateToday.add(Calendar.DATE, -1);
				currentDateAfterMonth = Calendar.getInstance();
				currentDateAfterMonth.add(Calendar.DATE, 29);
			}

			if (currentDateAfterMonth != null && currentDateToday != null
					&& date.before(currentDateAfterMonth.getTime()) && date.after(currentDateToday.getTime())) {
				return true;
			}
			return cFromDate != null && cAfterMonthsFromDate != null
					&& date.after(cFromDate.getTime()) && date.before(cAfterMonthsFromDate.getTime());
		} catch (ParseException e) {
			return false;
		}
	}
}
