package com.hkjc.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangePasswordDataRq {

	@JsonProperty("username")
	private String username;
	@JsonProperty("newPasswordHash")
	private String newPasswordHash;
	@JsonProperty("oldPasswordHash")
	private String oldPasswordHash;

	public ChangePasswordDataRq() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNewPasswordHash() {
		return newPasswordHash;
	}

	public void setNewPasswordHash(String newPasswordHash) {
		this.newPasswordHash = newPasswordHash;
	}

	public String getOldPasswordHash() {
		return oldPasswordHash;
	}

	public void setOldPasswordHash(String oldPasswordHash) {
		this.oldPasswordHash = oldPasswordHash;
	}


}
