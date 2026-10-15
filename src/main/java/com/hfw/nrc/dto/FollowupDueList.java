package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.FollowupDueList. */
public class FollowupDueList implements AbstractModel {

	private List<FollowupDueResponseModel> followupDueList = new ArrayList<>();

	public List<FollowupDueResponseModel> getFollowupDueList() { return followupDueList; }
	public void setFollowupDueList(List<FollowupDueResponseModel> followupDueList) { this.followupDueList = followupDueList; }
}
