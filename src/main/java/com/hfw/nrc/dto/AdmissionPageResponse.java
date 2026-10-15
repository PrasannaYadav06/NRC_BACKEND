package com.hfw.nrc.dto;

import java.util.List;

/** Stage 3 - paginated In-Patient Care Records list, same shape convention as HospitalPageResponse (Stage 2). */
public class AdmissionPageResponse implements AbstractModel {

	private List<AdmissionListDto> records;
	private long totalElements;
	private int totalPages;
	private int page;

	public List<AdmissionListDto> getRecords() { return records; }
	public void setRecords(List<AdmissionListDto> records) { this.records = records; }
	public long getTotalElements() { return totalElements; }
	public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
}
