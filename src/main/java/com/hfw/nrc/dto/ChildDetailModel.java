package com.hfw.nrc.dto;

import java.math.BigDecimal;

/** Ported from nrcapp.model.ChildDetailModel - the actual response payload for /getchilddetails. */
public class ChildDetailModel implements AbstractModel {

	private String childName;
	private String gender;
	private Long ipNumber;
	private Long rchId;
	private Long uhId;
	private Integer samNumber;
	private String mobileNumber;
	private Integer caseId;
	private String admissionDate;
	private BigDecimal admissionWeight;
	private BigDecimal admissionHeight;
	private BigDecimal dischargeHeight;
	private BigDecimal dischargeWeight;
	private BigDecimal admissionSd;
	private BigDecimal minWeight;
	private String minWeightDate;

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public Long getIpNumber() { return ipNumber; }
	public void setIpNumber(Long ipNumber) { this.ipNumber = ipNumber; }

	public Long getRchId() { return rchId; }
	public void setRchId(Long rchId) { this.rchId = rchId; }

	public Long getUhId() { return uhId; }
	public void setUhId(Long uhId) { this.uhId = uhId; }

	public Integer getSamNumber() { return samNumber; }
	public void setSamNumber(Integer samNumber) { this.samNumber = samNumber; }

	public String getMobileNumber() { return mobileNumber; }
	public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

	public Integer getCaseId() { return caseId; }
	public void setCaseId(Integer caseId) { this.caseId = caseId; }

	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

	public BigDecimal getAdmissionWeight() { return admissionWeight; }
	public void setAdmissionWeight(BigDecimal admissionWeight) { this.admissionWeight = admissionWeight; }

	public BigDecimal getAdmissionHeight() { return admissionHeight; }
	public void setAdmissionHeight(BigDecimal admissionHeight) { this.admissionHeight = admissionHeight; }

	public BigDecimal getDischargeHeight() { return dischargeHeight; }
	public void setDischargeHeight(BigDecimal dischargeHeight) { this.dischargeHeight = dischargeHeight; }

	public BigDecimal getDischargeWeight() { return dischargeWeight; }
	public void setDischargeWeight(BigDecimal dischargeWeight) { this.dischargeWeight = dischargeWeight; }

	public BigDecimal getAdmissionSd() { return admissionSd; }
	public void setAdmissionSd(BigDecimal admissionSd) { this.admissionSd = admissionSd; }

	public BigDecimal getMinWeight() { return minWeight; }
	public void setMinWeight(BigDecimal minWeight) { this.minWeight = minWeight; }

	public String getMinWeightDate() { return minWeightDate; }
	public void setMinWeightDate(String minWeightDate) { this.minWeightDate = minWeightDate; }
}
