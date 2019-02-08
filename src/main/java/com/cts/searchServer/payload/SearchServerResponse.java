package com.cts.searchServer.payload;

public class SearchServerResponse {
	private StringBuffer searchResults;

	public SearchServerResponse(StringBuffer searchResults) {
		super();
		this.searchResults = searchResults;
	}

	public StringBuffer getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(StringBuffer searchResults) {
		this.searchResults = searchResults;
	}
	
	

}
