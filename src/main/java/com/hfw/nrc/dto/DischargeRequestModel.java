package com.hfw.nrc.dto;

import java.math.BigDecimal;
import java.util.List;

/** Ported from nrcapp.model.DischargeRequestModel - request body for /updatecase and /updatedischarge. */
public class DischargeRequestModel implements AbstractModel {

	private String dischargeDate;
	private BigDecimal dischargeWeight;
	private BigDecimal dischargeHeight;
	private BigDecimal dischargeSd;
	private String minWeightDate;
	private BigDecimal minWeight;
	private Integer hospitalId;
	private Integer loginUserId;
	private Integer caseId;

	private List<DischargeChildModel> dischargeChildDetails;
	private List<DischargeComorbidModel> dischargeComorbidConditions;
	private List<DischargeMotherModel> dischargeMotherDetails;
	private List<CaseMotherGrowthModel> dischargeMotherGrowths;

	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }

	public BigDecimal getDischargeWeight() { return dischargeWeight; }
	public void setDischargeWeight(BigDecimal dischargeWeight) { this.dischargeWeight = dischargeWeight; }

	public BigDecimal getDischargeHeight() { return dischargeHeight; }
	public void setDischargeHeight(BigDecimal dischargeHeight) { this.dischargeHeight = dischargeHeight; }

	public BigDecimal getDischargeSd() { return dischargeSd; }
	public void setDischargeSd(BigDecimal dischargeSd) { this.dischargeSd = dischargeSd; }

	public String getMinWeightDate() { return minWeightDate; }
	public void setMinWeightDate(String minWeightDate) { this.minWeightDate = minWeightDate; }

	public BigDecimal getMinWeight() { return minWeight; }
	public void setMinWeight(BigDecimal minWeight) { this.minWeight = minWeight; }

	public Integer getHospitalId() { return hospitalId; }
	public void setHospitalId(Integer hospitalId) { this.hospitalId = hospitalId; }

	public Integer getLoginUserId() { return loginUserId; }
	public void setLoginUserId(Integer loginUserId) { this.loginUserId = loginUserId; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public List<DischargeChildModel> getDischargeChildDetails() { return dischargeChildDetails; }
	public void setDischargeChildDetails(List<DischargeChildModel> dischargeChildDetails) { this.dischargeChildDetails = dischargeChildDetails; }

	public List<DischargeComorbidModel> getDischargeComorbidConditions() { return dischargeComorbidConditions; }
	public void setDischargeComorbidConditions(List<DischargeComorbidModel> dischargeComorbidConditions) { this.dischargeComorbidConditions = dischargeComorbidConditions; }

	public List<DischargeMotherModel> getDischargeMotherDetails() { return dischargeMotherDetails; }
	public void setDischargeMotherDetails(List<DischargeMotherModel> dischargeMotherDetails) { this.dischargeMotherDetails = dischargeMotherDetails; }

	public List<CaseMotherGrowthModel> getDischargeMotherGrowths() { return dischargeMotherGrowths; }
	public void setDischargeMotherGrowths(List<CaseMotherGrowthModel> dischargeMotherGrowths) { this.dischargeMotherGrowths = dischargeMotherGrowths; }
}
