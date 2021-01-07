package vn.vpbank.wso2.identity.service.rest.model.backend;

import com.fasterxml.jackson.annotation.JsonProperty;
import vn.vpbank.wso2.identity.service.rest.internal.RESTServiceComponent;

public class SendSMSRq {

    private String phoneNumber;
    private String smsText;
    private String requestor;
    private String sourceAppPassword;
    private String requestBranch;
    private String messageType;

    public SendSMSRq(String mobile, String text){
        this.phoneNumber = mobile;
        this.smsText = text;
        this.requestor = RESTServiceComponent.getRESTOptions().getSmsRequestor();
        this.sourceAppPassword = RESTServiceComponent.getRESTOptions().getSmsSourceAppPassword();
        this.requestBranch = RESTServiceComponent.getRESTOptions().getSmsRequestBranch();
        this.messageType = RESTServiceComponent.getRESTOptions().getSmsMessageType();
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getSourceAppPassword() {
        return sourceAppPassword;
    }

    public void setSourceAppPassword(String sourceAppPassword) {
        this.sourceAppPassword = sourceAppPassword;
    }

    public String getRequestBranch() {
        return requestBranch;
    }

    public void setRequestBranch(String requestBranch) {
        this.requestBranch = requestBranch;
    }


}
