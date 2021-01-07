package com.hkjc.wso2.identity.service.rest.model.backend;

public class FindVPBPlusChannelRs {
    private String channelStatus;
    private String customerFullName;
    private String defaultAccountNumber;
    private String packageCode;
    private String prepaidCardNumber;

    public FindVPBPlusChannelRs(){}

    public String getDefaultAccountNumber() {
        return defaultAccountNumber;
    }

    public String getPrepaidCardNumber() {
        return prepaidCardNumber;
    }
}
