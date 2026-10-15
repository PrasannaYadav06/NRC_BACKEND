package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

import com.hfw.nrc.domain.Followup;

/** Thin AbstractModel wrapper so NRCFollowupService#getAllFollowups can go through setResponseObject, same convention as CaseDetailList. */
public class FollowupList implements AbstractModel {

	private List<Followup> followupList = new ArrayList<>();

	public List<Followup> getFollowupList() { return followupList; }
	public void setFollowupList(List<Followup> followupList) { this.followupList = followupList; }
}
