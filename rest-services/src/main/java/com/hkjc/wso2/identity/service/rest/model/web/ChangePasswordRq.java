package com.hkjc.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangePasswordRq {

    @JsonProperty("oldPassword")
    private String oldPassword;
    @JsonProperty("newPassword")
    private String newPassword;

    public ChangePasswordRq(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public ChangePasswordRq() {
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
