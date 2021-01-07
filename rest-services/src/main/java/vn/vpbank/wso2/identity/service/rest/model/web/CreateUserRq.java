package vn.vpbank.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateUserRq {
    private Boolean passwordChangeRequired = true;
    private String mobile;
    private String fullName;
    private String email;
    @JsonProperty("CAID")
    private String CAID;
    @JsonProperty("CIF")
    private String CIF;
    private String password;
    private String passwordAES;
    private String loginName;
    private String lang = "VN";

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getPasswordAES() {
        return passwordAES;
    }

    public void setPasswordAES(String passwordAES) {
        this.passwordAES = passwordAES;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public void setPasswordChangeRequired(Boolean passwordChangeRequired) {
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCAID() {
        return CAID;
    }

    public void setCAID(String CAID) {
        this.CAID = CAID;
    }

    public String getCIF() {
        return CIF;
    }

    public void setCIF(String CIF) {
        this.CIF = CIF;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
