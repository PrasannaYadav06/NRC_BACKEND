package com.hfw.nrc.dto;

/** Stage 6 - full per-designation breakdown: View popup on the Search HR Records list, and the self-service form's own prefill (GET /hr/latest). */
public class HrDetailDto implements AbstractModel {

	private Integer hrRecId;
	private Integer hId;
	private String hospitalName;
	private Integer month;
	private Integer year;
	private Integer numberOfBeds;

	private Integer officerSanctioned, officerWorking, officerVacant, officerTraining;
	private Integer nurseSanctioned, nurseWorking, nurseVacant, nurseTraining;
	private Integer dieticianSanctioned, dieticianWorking, dieticianVacant, dieticianTraining;
	private Integer cookSanctioned, cookWorking, cookVacant, cookTraining;
	private Integer tenderSanctioned, tenderWorking, tenderVacant, tenderTraining;
	private Integer mswSanctioned, mswWorking, mswVacant, mswTraining;

	public Integer getHrRecId() { return hrRecId; }
	public void setHrRecId(Integer hrRecId) { this.hrRecId = hrRecId; }
	public Integer getHId() { return hId; }
	public void setHId(Integer hId) { this.hId = hId; }
	public String getHospitalName() { return hospitalName; }
	public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
	public Integer getMonth() { return month; }
	public void setMonth(Integer month) { this.month = month; }
	public Integer getYear() { return year; }
	public void setYear(Integer year) { this.year = year; }
	public Integer getNumberOfBeds() { return numberOfBeds; }
	public void setNumberOfBeds(Integer numberOfBeds) { this.numberOfBeds = numberOfBeds; }

	public Integer getOfficerSanctioned() { return officerSanctioned; }
	public void setOfficerSanctioned(Integer v) { this.officerSanctioned = v; }
	public Integer getOfficerWorking() { return officerWorking; }
	public void setOfficerWorking(Integer v) { this.officerWorking = v; }
	public Integer getOfficerVacant() { return officerVacant; }
	public void setOfficerVacant(Integer v) { this.officerVacant = v; }
	public Integer getOfficerTraining() { return officerTraining; }
	public void setOfficerTraining(Integer v) { this.officerTraining = v; }

	public Integer getNurseSanctioned() { return nurseSanctioned; }
	public void setNurseSanctioned(Integer v) { this.nurseSanctioned = v; }
	public Integer getNurseWorking() { return nurseWorking; }
	public void setNurseWorking(Integer v) { this.nurseWorking = v; }
	public Integer getNurseVacant() { return nurseVacant; }
	public void setNurseVacant(Integer v) { this.nurseVacant = v; }
	public Integer getNurseTraining() { return nurseTraining; }
	public void setNurseTraining(Integer v) { this.nurseTraining = v; }

	public Integer getDieticianSanctioned() { return dieticianSanctioned; }
	public void setDieticianSanctioned(Integer v) { this.dieticianSanctioned = v; }
	public Integer getDieticianWorking() { return dieticianWorking; }
	public void setDieticianWorking(Integer v) { this.dieticianWorking = v; }
	public Integer getDieticianVacant() { return dieticianVacant; }
	public void setDieticianVacant(Integer v) { this.dieticianVacant = v; }
	public Integer getDieticianTraining() { return dieticianTraining; }
	public void setDieticianTraining(Integer v) { this.dieticianTraining = v; }

	public Integer getCookSanctioned() { return cookSanctioned; }
	public void setCookSanctioned(Integer v) { this.cookSanctioned = v; }
	public Integer getCookWorking() { return cookWorking; }
	public void setCookWorking(Integer v) { this.cookWorking = v; }
	public Integer getCookVacant() { return cookVacant; }
	public void setCookVacant(Integer v) { this.cookVacant = v; }
	public Integer getCookTraining() { return cookTraining; }
	public void setCookTraining(Integer v) { this.cookTraining = v; }

	public Integer getTenderSanctioned() { return tenderSanctioned; }
	public void setTenderSanctioned(Integer v) { this.tenderSanctioned = v; }
	public Integer getTenderWorking() { return tenderWorking; }
	public void setTenderWorking(Integer v) { this.tenderWorking = v; }
	public Integer getTenderVacant() { return tenderVacant; }
	public void setTenderVacant(Integer v) { this.tenderVacant = v; }
	public Integer getTenderTraining() { return tenderTraining; }
	public void setTenderTraining(Integer v) { this.tenderTraining = v; }

	public Integer getMswSanctioned() { return mswSanctioned; }
	public void setMswSanctioned(Integer v) { this.mswSanctioned = v; }
	public Integer getMswWorking() { return mswWorking; }
	public void setMswWorking(Integer v) { this.mswWorking = v; }
	public Integer getMswVacant() { return mswVacant; }
	public void setMswVacant(Integer v) { this.mswVacant = v; }
	public Integer getMswTraining() { return mswTraining; }
	public void setMswTraining(Integer v) { this.mswTraining = v; }
}
