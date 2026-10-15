package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.ResourceResponseModel - request body for /saveresourceslist, response payload for /getresourceslist. */
public class ResourceResponseModel implements AbstractModel {

	private Integer month;
	private Integer year;
	private Integer loginUserId;
	private Integer numberOfBeds;

	private List<HrTypeModel> officerList = new ArrayList<>();
	private List<HrTypeModel> nurseList = new ArrayList<>();
	private List<HrTypeModel> dieticianList = new ArrayList<>();
	private List<HrTypeModel> cookList = new ArrayList<>();
	private List<HrTypeModel> tenderList = new ArrayList<>();
	private List<HrTypeModel> mswList = new ArrayList<>();

	public Integer getMonth() { return month; }
	public void setMonth(Integer month) { this.month = month; }

	public Integer getYear() { return year; }
	public void setYear(Integer year) { this.year = year; }

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }

	public Integer getNumberOfBeds() { return numberOfBeds; }
	public void setNumberOfBeds(Integer numberOfBeds) { this.numberOfBeds = numberOfBeds; }

	public List<HrTypeModel> getOfficerList() { return officerList; }
	public void setOfficerList(List<HrTypeModel> officerList) { this.officerList = officerList; }

	public List<HrTypeModel> getNurseList() { return nurseList; }
	public void setNurseList(List<HrTypeModel> nurseList) { this.nurseList = nurseList; }

	public List<HrTypeModel> getDieticianList() { return dieticianList; }
	public void setDieticianList(List<HrTypeModel> dieticianList) { this.dieticianList = dieticianList; }

	public List<HrTypeModel> getCookList() { return cookList; }
	public void setCookList(List<HrTypeModel> cookList) { this.cookList = cookList; }

	public List<HrTypeModel> getTenderList() { return tenderList; }
	public void setTenderList(List<HrTypeModel> tenderList) { this.tenderList = tenderList; }

	public List<HrTypeModel> getMswList() { return mswList; }
	public void setMswList(List<HrTypeModel> mswList) { this.mswList = mswList; }
}
