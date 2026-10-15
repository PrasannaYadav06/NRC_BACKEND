package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported verbatim from nrcapp.model.GenericConfigList. */
public class GenericConfigList implements AbstractModel {

	private List<GenericConfigModel> genericList = new ArrayList<>();

	public List<GenericConfigModel> getGenericList() {
		return genericList;
	}

	public void setGenericList(List<GenericConfigModel> genericList) {
		this.genericList = genericList;
	}
}
