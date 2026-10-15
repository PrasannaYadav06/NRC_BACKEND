package com.hfw.nrc.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ported from nrcadmin.common.CaseDetailsDto - response row for
 * GET /caseDetails/{samNum} (nrcadmin's admin SAM-number search/delete
 * tool). Returned as a raw List<CaseDetailsAdminDto>, not wrapped in
 * AbstractResponse - matches the source's own contract exactly, since the
 * merged frontend's deletesam.component.ts reads the array directly.
 */
public class CaseDetailsAdminDto {

	private int caseId;
	private String admissionDate;
	private String dischargeDate;
	private int samNum;
	private BigDecimal targetWeight;
	private BigDecimal admissionSd;
	private BigDecimal dischargeSd;
	private int childId;
	private String childName;
	private String dateOfBirth;
	private String gender;
	private String address;
	private int hId;
	private String hospitalName;

	public int getCaseId() { return caseId; }
	public void setCaseId(int caseId) { this.caseId = caseId; }

	public String getAdmissionDate() { return admissionDate; }
	public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

	public String getDischargeDate() { return dischargeDate; }
	public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }

	public int getSamNum() { return samNum; }
	public void setSamNum(int samNum) { this.samNum = samNum; }

	public BigDecimal getTargetWeight() { return targetWeight; }
	public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }

	public BigDecimal getAdmissionSd() { return admissionSd; }
	public void setAdmissionSd(BigDecimal admissionSd) { this.admissionSd = admissionSd; }

	public BigDecimal getDischargeSd() { return dischargeSd; }
	public void setDischargeSd(BigDecimal dischargeSd) { this.dischargeSd = dischargeSd; }

	public int getChildId() { return childId; }
	public void setChildId(int childId) { this.childId = childId; }

	public String getChildName() { return childName; }
	public void setChildName(String childName) { this.childName = childName; }

	public String getDateOfBirth() { return dateOfBirth; }
	public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

	public String getGender() { return gender; }
	public void setGender(String gender) { this.gender = gender; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	// Same getHId()/setHId() -> "HId" Jackson-mangling pitfall as
	// HospitalAdminDto (see its javadoc) - forced explicitly, this time on
	// the serialization side.
	@JsonProperty("hId")
	public int getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(int hId) { this.hId = hId; }

	public String getHospitalName() { return hospitalName; }
	public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
}
