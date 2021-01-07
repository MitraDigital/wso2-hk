package com.hkjc.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationDataRq {

	@JsonProperty("username")
	private String username;
	@JsonProperty("password")
	private String password;
	@JsonProperty("acctType")
	private String acctType;

	public AuthenticationDataRq() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAcctType() {
		return acctType;
	}

	public void setAcctType(String acctType) {
		this.acctType = acctType;
	}

}
