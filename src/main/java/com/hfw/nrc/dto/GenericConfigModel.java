package com.hfw.nrc.dto;

/** Ported verbatim from nrcapp.model.GenericConfigModel. */
public class GenericConfigModel {

	private Integer configId;
	private String configCode;
	private String configDesc;

	/** PHC/CHC dropdowns only (see GeneralConfigService#getAllPhcs/getAllChcs) - true when an NRC hospital is already registered under this facility, so the frontend can grey it out. Unused (false) by every other lookup. */
	private boolean alreadyRegistered;

	/**
	 * Registration form's District/Taluka dropdowns only (see
	 * GeneralConfigService#getAllDistricts/getAllTaluks) - true when this
	 * EXACT level already has an NRC (a District row: a DISTRICT-level
	 * hospital already exists for it; a Taluka row: a plain TALUKA-level
	 * hospital with no PHC/CHC link already exists for it), so the
	 * Registration form can block a true duplicate. Deliberately separate
	 * from alreadyRegistered above, whose "has ANY hospital at all" meaning
	 * is intentionally broader and already relied on elsewhere (filter
	 * dropdowns) - a district/taluka having a taluka-level or PHC/CHC-linked
	 * hospital does NOT make this true. Unused (false) by every other lookup.
	 */
	private boolean hasExactLevelNrc;

	public boolean isAlreadyRegistered() {
		return alreadyRegistered;
	}

	public void setAlreadyRegistered(boolean alreadyRegistered) {
		this.alreadyRegistered = alreadyRegistered;
	}

	public boolean isHasExactLevelNrc() {
		return hasExactLevelNrc;
	}

	public void setHasExactLevelNrc(boolean hasExactLevelNrc) {
		this.hasExactLevelNrc = hasExactLevelNrc;
	}

	public Integer getConfigId() {
		return configId;
	}

	public void setConfigId(Integer configId) {
		this.configId = configId;
	}

	public String getConfigCode() {
		return configCode;
	}

	public void setConfigCode(String configCode) {
		this.configCode = configCode;
	}

	public String getConfigDesc() {
		return configDesc;
	}

	public void setConfigDesc(String configDesc) {
		this.configDesc = configDesc;
	}
}
