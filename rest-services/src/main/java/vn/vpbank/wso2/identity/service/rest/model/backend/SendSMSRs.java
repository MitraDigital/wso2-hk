package vn.vpbank.wso2.identity.service.rest.model.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendSMSRs {
    private Boolean passwordChangeRequired = true;
    private String mobile;
    private String fullName;
    private String email;
    @JsonProperty("CAID")
    private String CAID;
    @JsonProperty("CIF")
    private String CIF;
    private String password;
    private String loginName;
    private String userName;


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
