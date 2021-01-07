package vn.vpbank.wso2.identity.service.rest.model;

import java.util.HashMap;
import java.util.Map;

public enum ClaimEnum {
	//This order is used in UserMgtServlet.findUsers
	MOBILE("http://wso2.org/claims/mobile", "mobile"),
	EMAIL("http://wso2.org/claims/emailaddress", "email"),
	CAID("http://wso2.org/claims/externalid", "CAID"),
	CIF("http://wso2.org/claims/CIF", "CIF"),
	LOGIN("http://wso2.org/claims/userid", "loginName"),
	USER_NAME("http://wso2.org/claims/username", "userName"),
	FULL_NAME("http://wso2.org/claims/fullname", "fullName"),
	USER_PASSWORD("http://wso2.org/claims/encryptedUserPassword", "encryptedUserPassword");
	
	private static Map<String, ClaimEnum> CLAIMS_URI_MAP = new HashMap<String, ClaimEnum>();
	static{
		for (ClaimEnum claimEnum : values()) {
			CLAIMS_URI_MAP.put(claimEnum.getClaimURI(), claimEnum);
		}
	};	
	
	private static Map<String, ClaimEnum> PARAM_NAME_MAP = new HashMap<String, ClaimEnum>();
	static {
		for (ClaimEnum claimEnum : values()) {
			PARAM_NAME_MAP.put(claimEnum.getParamName(), claimEnum);
		}
	};	

	private final String claimURI;
	private final String paramName;
	
	ClaimEnum (String claimURI, String paramName) {
		this.claimURI = claimURI;
		this.paramName = paramName;
	}
	
	public String getClaimURI() {
        return claimURI;
    }
	
	public String getParamName() {
        return paramName;
    }	
	
	public static ClaimEnum getClaimEnumByClaimURI(String claimURI) {
		return CLAIMS_URI_MAP.get(claimURI);
	}
	
	public static ClaimEnum getClaimEnumByParamName(String paramName) {
		return PARAM_NAME_MAP.get(paramName);
	}
}
