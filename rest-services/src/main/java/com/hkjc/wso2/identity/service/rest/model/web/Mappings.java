package com.hkjc.wso2.identity.service.rest.model.web;

import java.util.HashMap;
import java.util.Map;

public class Mappings {
    /**
     * Mapping claims to JSONfields
     */
    public static Map<String, String> changeableClaimsMap = new HashMap<String, String>() {
        {
            put("http://wso2.org/claims/chpass", "passwordChangeRequired");
            put("http://wso2.org/claims/passpref", "passwordPrefix");
            put("http://wso2.org/claims/mobile", "mobile");
            put("http://wso2.org/claims/fullname", "fullName");
            put("http://wso2.org/claims/emailaddress", "email");
            put("http://wso2.org/claims/externalid", "CAID");
            put("http://wso2.org/claims/CIF", "CIF");
            put("http://wso2.org/claims/userid", "loginName");
        }
    };

    public static Map<String, String> previousValueOfChangeableClaims = new HashMap<String, String>() {
        {
            put("http://wso2.org/claims/chpass", "prevPasswordChangeRequired");
            put("http://wso2.org/claims/passpref", "prevPasswordPrefix");
            put("http://wso2.org/claims/mobile", "prevMobile");
            put("http://wso2.org/claims/fullname", "prevFullName");
            put("http://wso2.org/claims/emailaddress", "prevEmail");
            put("http://wso2.org/claims/externalid", "prevCAID");
            put("http://wso2.org/claims/CIF", "prevCIF");
            put("http://wso2.org/claims/userid", "prevLoginName");
        }
    };
}
