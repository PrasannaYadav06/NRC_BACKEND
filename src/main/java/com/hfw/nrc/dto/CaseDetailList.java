package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.CaseDetailList. */
public class CaseDetailList implements AbstractModel {

	private List<CaseDetailModel> caseDetailList = new ArrayList<>();

	public List<CaseDetailModel> getCaseDetailList() {
		return caseDetailList;
	}

	public void setCaseDetailList(List<CaseDetailModel> caseDetailList) {
		this.caseDetailList = caseDetailList;
	}
}
