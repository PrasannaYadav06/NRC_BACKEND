package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.ChildGrowthList. */
public class ChildGrowthList implements AbstractModel {

	private String childName;
	private Integer samNumber;
	private List<ChildGrowthResponseModel> growthList = new ArrayList<>();

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public List<ChildGrowthResponseModel> getGrowthList() { return growthList; }
	public void setGrowthList(List<ChildGrowthResponseModel> growthList) { this.growthList = growthList; }
}
