package vn.vpbank.wso2.identity.service.rest.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import vn.vpbank.wso2.identity.service.rest.internal.RESTServiceComponent;
import vn.vpbank.wso2.identity.service.rest.utils.CommonUtils;
import vn.vpbank.wso2.identity.service.rest.utils.MessageUtils;
import vn.vpbank.wso2.identity.service.rest.utils.UserStoreUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

public class GetAuthenticateService {
    private static Log log = LogFactory.getLog(GetAuthenticateService.class);

    //TODO - rework to object serialization approach

    /**
     * The search of users by claim value
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @param uri      is relative URL for getting of path params
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void getAuthenticate(HttpServletRequest request, HttpServletResponse response,
                                 String uri) throws IOException {
        String userName = URLDecoder.decode(CommonUtils.getParamFromString(uri, "/authenticate/", "/"), "UTF-8");
        String password;
        try {
            password = CommonUtils.decrypt(request.getHeader("passwordAES"),
                    RESTServiceComponent.getRESTOptions().getKey());
        } catch (Exception e) {
            MessageUtils.setError(request, response,
                    400, "Error during password decription", log);
            log.error(e.getMessage());
            return;
        }
        boolean auth;
        try {
            auth = UserStoreUtils.authenticate(userName, password);
        } catch (UserStoreException e) {
            MessageUtils.setError(request, response,
                    401, "User can't be authenticated", log);
            log.error(e.getMessage());
            return;
        } catch (UserStoreUtils.UserStoreConnectionException e) {
            log.error("Something wrong with userstore interaction", e);
            MessageUtils.setError(request, response,
                    500, "Error during claims reading", log);
            return;
        }
        JSONObject outbody = new JSONObject();
        if (auth) {
            Claim[] claims;
            try {
                claims = UserStoreUtils.getUser(userName);
            } catch (Exception e) {
                log.error("Something wrong with userstore interaction", e);
                MessageUtils.setError(request, response,
                        500, "Error during claims reading", log);
                //log.info("User " + userName + " failed authentication");
                return;
            }
            //log.info("User " + userName + " is authenticated");
            boolean chPassFound = false;
            boolean agreementsFound = false;
            for (Claim claim : claims) {
                switch (claim.getClaimUri()) {
                    case "http://wso2.org/claims/username":
                        outbody.put("userName", claim.getValue());
                        break;
                    case "http://wso2.org/claims/mobile":
                        outbody.put("mobile", claim.getValue());
                        break;
                    case "http://wso2.org/claims/emailaddress":
                        outbody.put("email", claim.getValue());
                        break;
                    case "http://wso2.org/claims/fullname":
                        outbody.put("fullName", claim.getValue());
                        break;
                    case "http://wso2.org/claims/CIF":
                        outbody.put("CIF", claim.getValue());
                        break;
                    case "http://wso2.org/claims/userid":
                        outbody.put("loginName", claim.getValue());
                        break;
                    case "http://wso2.org/claims/externalid":
                        outbody.put("CAID", claim.getValue());
                        break;
                    case "http://wso2.org/claims/chpass":
                        chPassFound = true;
                        outbody.put("passwordChangeRequired", !("false".equals(claim.getValue())));
                        break;
                    case "http://wso2.org/claims/im":
                        agreementsFound = true;
                        outbody.put("agreements",
                                (claim.getValue() != null && claim.getValue().length() > 0));
                        break;
                }
            }
            if (!chPassFound) {
                outbody.put("passwordChangeRequired", true);
            }
            if (!agreementsFound) {
                outbody.put("agreements", false);
            }
            MessageUtils.setSuccess(response, outbody, log);
        } else {
            MessageUtils.setError(request, response,
                    401, "User can't be authenticated", log);
            return;
        }

    }
}
