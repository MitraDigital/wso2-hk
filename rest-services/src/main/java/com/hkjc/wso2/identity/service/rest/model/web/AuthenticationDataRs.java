package com.hkjc.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationDataRs {

	@JsonProperty("webAccountName")
	private String webAccountName;
	@JsonProperty("webAccountId")
	private String webAccountId;

	public AuthenticationDataRs() {

	}

	public String getWebAccountName() {
		return webAccountName;
	}

	public void setWebAccountName(String webAccountName) {
		this.webAccountName = webAccountName;
	}

	public String getWebAccountId() {
		return webAccountId;
	}

	public void setWebAccountId(String webAccountId) {
		this.webAccountId = webAccountId;
	}

}
