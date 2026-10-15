package com.hfw.nrc.dto;

import java.util.List;

/** Stage 5 - paginated Follow-up Records list, same shape convention as AdmissionPageResponse/HospitalPageResponse. */
public class FollowupPageResponse implements AbstractModel {

	private List<FollowupListDto> records;
	private long totalElements;
	private int totalPages;
	private int page;

	public List<FollowupListDto> getRecords() { return records; }
	public void setRecords(List<FollowupListDto> records) { this.records = records; }
	public long getTotalElements() { return totalElements; }
	public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
}
