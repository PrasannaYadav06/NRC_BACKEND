package com.hfw.nrc.dto;

import java.util.ArrayList;
import java.util.List;

/** Ported from nrcapp.model.SearchQueryResultList. */
public class SearchQueryResultList implements AbstractModel {

	private List<SearchQueryResponseModel> searchResultList = new ArrayList<>();

	public List<SearchQueryResponseModel> getSearchResultList() { return searchResultList; }
	public void setSearchResultList(List<SearchQueryResponseModel> searchResultList) { this.searchResultList = searchResultList; }
}
