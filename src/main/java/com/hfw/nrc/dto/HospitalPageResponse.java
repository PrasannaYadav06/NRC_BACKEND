package com.hfw.nrc.dto;

import java.util.List;

/** Paginated "Hospital Records" list response - see HospitalRecordDto. */
public class HospitalPageResponse implements AbstractModel {

	private List<HospitalRecordDto> records;
	private int totalElements;
	private int totalPages;
	private int page;

	public List<HospitalRecordDto> getRecords() { return records; }
	public void setRecords(List<HospitalRecordDto> records) { this.records = records; }

	public int getTotalElements() { return totalElements; }
	public void setTotalElements(int totalElements) { this.totalElements = totalElements; }

	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
}
