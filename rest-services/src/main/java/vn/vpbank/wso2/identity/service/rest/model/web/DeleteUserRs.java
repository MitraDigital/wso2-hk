package vn.vpbank.wso2.identity.service.rest.model.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.user.core.claim.Claim;

import java.util.Map;

public class DeleteUserRs {
    private String userName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean passwordChangeRequired;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mobile;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fullName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    @JsonIgnore
    private String CAID;
    @JsonIgnore
    private String CIF;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String loginName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lang;

    public DeleteUserRs(String userName){
        this.userName = userName;
    }

    public DeleteUserRs(Map<String, String> claims){
        for (Map.Entry<String, String> claim: claims.entrySet()) {
            switch (claim.getKey()) {
                case "http://wso2.org/claims/username":
                    this.userName = claim.getValue();
                    break;
                case "http://wso2.org/claims/chpass":
                    this.passwordChangeRequired = Boolean.valueOf(claim.getValue());
                    break;
                case "http://wso2.org/claims/mobile":
                    this.mobile = claim.getValue();
                    break;
                case "http://wso2.org/claims/fullname":
                    this.fullName = claim.getValue();
                    break;
                case "http://wso2.org/claims/emailaddress":
                    this.email = claim.getValue();
                    break;
                case "http://wso2.org/claims/externalid":
                    this.CAID = claim.getValue();
                    break;
                case "http://wso2.org/claims/CIF":
                    this.CIF = claim.getValue();
                    break;
                case "http://wso2.org/claims/userid":
                    this.loginName = claim.getValue();
                    break;
                case "http://wso2.org/claims/locality":
                    this.lang = claim.getValue();
                    break;
            }
        }
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

    @JsonProperty("CAID")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getCAID() {
        return CAID;
    }

    public void setCAID(String CAID) {
        this.CAID = CAID;
    }

    @JsonProperty("CIF")
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
