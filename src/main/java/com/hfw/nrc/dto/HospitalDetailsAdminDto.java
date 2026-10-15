package com.hfw.nrc.dto;

/**
 * Ported from nrcadmin.common.HospitalDetailsDto - the "hospitalDetails"
 * sub-object of the /hospital/save request body. Only ever used to seed
 * the initial bed count (hr_type_id=25) for a newly-created hospital.
 */
public class HospitalDetailsAdminDto {

	private int detailsId;
	private int value;

	public int getDetailsId() { return detailsId; }
	public void setDetailsId(int detailsId) { this.detailsId = detailsId; }

	public int getValue() { return value; }
	public void setValue(int value) { this.value = value; }
}
