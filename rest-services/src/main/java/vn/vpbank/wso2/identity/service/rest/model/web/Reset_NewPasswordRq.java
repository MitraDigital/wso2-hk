package vn.vpbank.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reset_NewPasswordRq {
    @JsonProperty("newPassword")
    private String newPassword;

    public Reset_NewPasswordRq(){}

    public Reset_NewPasswordRq(String newPassword){
        this.newPassword = newPassword;
    }

    public String getPassword() {
        return newPassword;
    }
}
