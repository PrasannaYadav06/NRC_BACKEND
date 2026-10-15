package com.hfw.nrc.dto;

/** Ported from nrcapp.model.HrTypeModel. */
public class HrTypeModel {

	private Integer sanctioned = 0;
	private Integer working = 0;
	private Integer vacant = 0;
	private Integer training = 0;

	public Integer getSanctioned() { return sanctioned; }
	public void setSanctioned(Integer sanctioned) { this.sanctioned = sanctioned; }

	public Integer getWorking() { return working; }
	public void setWorking(Integer working) { this.working = working; }

	public Integer getVacant() { return vacant; }
	public void setVacant(Integer vacant) { this.vacant = vacant; }

	public Integer getTraining() { return training; }
	public void setTraining(Integer training) { this.training = training; }
}
