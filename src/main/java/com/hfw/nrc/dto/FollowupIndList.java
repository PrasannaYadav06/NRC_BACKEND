package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.FollowupIndList - request body for /deletefollowups. */
public class FollowupIndList {

	private List<Integer> followupList = new ArrayList<>();

	public List<Integer> getFollowupList() { return followupList; }
	public void setFollowupList(List<Integer> followupList) { this.followupList = followupList; }
}
