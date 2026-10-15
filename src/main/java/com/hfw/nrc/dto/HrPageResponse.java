package com.hfw.nrc.dto;

import java.util.List;

/** Stage 6 - "Search HR Records" paginated response, same shape as FollowupPageResponse/AdmissionPageResponse. */
public class HrPageResponse implements AbstractModel {

	private List<HrListDto> records;
	private long totalElements;
	private int totalPages;
	private int page;

	public List<HrListDto> getRecords() { return records; }
	public void setRecords(List<HrListDto> records) { this.records = records; }
	public long getTotalElements() { return totalElements; }
	public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
	public int getPage() { return page; }
	public void setPage(int page) { this.page = page; }
}
