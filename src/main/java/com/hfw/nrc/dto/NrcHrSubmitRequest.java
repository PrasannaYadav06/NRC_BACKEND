package com.hfw.nrc.dto;

import java.util.List;

/** Body of POST /hrpost/submit. Ported from KABHI's KabhiHrSubmitRequest, minus officialEmailId (no NRC designation is analogous to KABHI's "District Coordinator"). */
public class NrcHrSubmitRequest {

	private String designation;
	private String personName;
	private String dateOfJoining; // yyyy-MM-dd
	private String emailId;
	private String contactNo;
	private String employmentStatus;
	private List<String> daysOfConsultation; // only meaningful when employmentStatus = "On-Consultation"

	public String getDesignation() { return designation; }
	public void setDesignation(String designation) { this.designation = designation; }
	public String getPersonName() { return personName; }
	public void setPersonName(String personName) { this.personName = personName; }
	public String getDateOfJoining() { return dateOfJoining; }
	public void setDateOfJoining(String dateOfJoining) { this.dateOfJoining = dateOfJoining; }
	public String getEmailId() { return emailId; }
	public void setEmailId(String emailId) { this.emailId = emailId; }
	public String getContactNo() { return contactNo; }
	public void setContactNo(String contactNo) { this.contactNo = contactNo; }
	public String getEmploymentStatus() { return employmentStatus; }
	public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
	public List<String> getDaysOfConsultation() { return daysOfConsultation; }
	public void setDaysOfConsultation(List<String> daysOfConsultation) { this.daysOfConsultation = daysOfConsultation; }
}
