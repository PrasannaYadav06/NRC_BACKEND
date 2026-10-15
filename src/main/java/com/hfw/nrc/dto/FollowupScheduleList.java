package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

import com.hfw.nrc.domain.FollowupSchedule;

/** Thin AbstractModel wrapper so NRCFollowupService#getAllFollowupsDue/getAllFollowupsDuebyDate can go through setResponseObject, same convention as CaseDetailList. */
public class FollowupScheduleList implements AbstractModel {

	private List<FollowupSchedule> followupScheduleList = new ArrayList<>();

	public List<FollowupSchedule> getFollowupScheduleList() { return followupScheduleList; }
	public void setFollowupScheduleList(List<FollowupSchedule> followupScheduleList) { this.followupScheduleList = followupScheduleList; }
}
