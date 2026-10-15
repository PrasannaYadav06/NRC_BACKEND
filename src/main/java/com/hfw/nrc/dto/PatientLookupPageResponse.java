package com.hfw.nrc.dto;

import java.util.List;

/** Item 13 - Search Patient page's result-picker list, same shape convention as AdmissionPageResponse. */
public class PatientLookupPageResponse implements AbstractModel {

	private List<PatientLookupDto> records;
	private long totalElements;
	private int totalPages;
	private int page;

	public List<PatientLookupDto> getRecords() { return records; }
	public void setRecords(List<PatientLookupDto> records) { this.records = records; }
	public long getTotalElements() { return totalElements; }
	public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
}
