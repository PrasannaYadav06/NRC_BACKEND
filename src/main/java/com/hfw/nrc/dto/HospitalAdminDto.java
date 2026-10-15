package com.hfw.nrc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ported from nrcadmin.common.HospitalDto - the "hospital" sub-object of
 * the /hospital/save request body. Field names match the merged Angular
 * frontend's addhospital.component.ts payload exactly (hCode/hId camelCase).
 *
 * @JsonProperty is required on hCode/hId, not optional polish: Jackson's
 * default bean-property mangling follows the same java.beans.Introspector
 * .decapitalize rule that bit findByCaseIdAndHospital_HId back in Group C -
 * getHCode()/getHId() strip to "HCode"/"HId", and since the first two
 * characters are both uppercase the rule refuses to lowercase the first
 * one, so Jackson looks for JSON keys "HCode"/"HId" by default, not
 * "hCode"/"hId". Verified live: without this annotation hCode silently
 * bound as null (caught via a live test save, DB showed h_code=NULL).
 */
public class HospitalAdminDto {

	private int hId;
	private String hCode;
	private String name;
	private String nameShort;
	private int districtId;
	private Integer talukaId;
	private String hospitalLevel;
	private Integer phcId;
	private Integer chcId;
	private boolean isMedicalCollege;

	public Integer getPhcId() { return phcId; }
	public void setPhcId(Integer phcId) { this.phcId = phcId; }

	public boolean isMedicalCollege() { return isMedicalCollege; }
	public void setMedicalCollege(boolean isMedicalCollege) { this.isMedicalCollege = isMedicalCollege; }

	public Integer getChcId() { return chcId; }
	public void setChcId(Integer chcId) { this.chcId = chcId; }

	@JsonProperty("hId")
	public int getHId() { return hId; }
	@JsonProperty("hId")
	public void setHId(int hId) { this.hId = hId; }

	@JsonProperty("hCode")
	public String getHCode() { return hCode; }
	@JsonProperty("hCode")
	public void setHCode(String hCode) { this.hCode = hCode; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getNameShort() { return nameShort; }
	public void setNameShort(String nameShort) { this.nameShort = nameShort; }

	public int getDistrictId() { return districtId; }
	public void setDistrictId(int districtId) { this.districtId = districtId; }

	public Integer getTalukaId() { return talukaId; }
	public void setTalukaId(Integer talukaId) { this.talukaId = talukaId; }

	public String getHospitalLevel() { return hospitalLevel; }
	public void setHospitalLevel(String hospitalLevel) { this.hospitalLevel = hospitalLevel; }
}
