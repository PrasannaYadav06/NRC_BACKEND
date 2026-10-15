package com.hfw.nrc.dto;

/**
 * Ported from nrcadmin.common.CommonDto - the composite /hospital/save
 * request body (hospital + user + hospitalDetails sub-objects), matching
 * the merged frontend's addhospital.component.ts payload shape exactly.
 */
public class AddHospitalRequestModel {

	private NewHospitalUserDto user;
	private HospitalAdminDto hospital;
	private HospitalDetailsAdminDto hospitalDetails;

	public NewHospitalUserDto getUser() { return user; }
	public void setUser(NewHospitalUserDto user) { this.user = user; }

	public HospitalAdminDto getHospital() { return hospital; }
	public void setHospital(HospitalAdminDto hospital) { this.hospital = hospital; }

	public HospitalDetailsAdminDto getHospitalDetails() { return hospitalDetails; }
	public void setHospitalDetails(HospitalDetailsAdminDto hospitalDetails) { this.hospitalDetails = hospitalDetails; }
}
