package vn.vpbank.wso2.identity.service.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;
import vn.vpbank.wso2.identity.service.rest.internal.RESTServiceComponent;
import vn.vpbank.wso2.identity.service.rest.model.web.*;
import vn.vpbank.wso2.identity.service.rest.model.backend.*;
import vn.vpbank.wso2.identity.service.rest.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ChangePasswordService {
    private static Log log = LogFactory.getLog(ChangePasswordService.class);








    @SuppressWarnings("unchecked")
    public static void initChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String userID = request.getHeader("IDN-User");
        String lang = request.getHeader("IDN-Lang");
        String appID = request.getHeader("IDN-App");
        String requestID;

        if (StringUtils.isEmpty(userID)) {
            MessageUtils.setError(request, response,
                    422, "IDN-User cannot be empty", log);
            return;
        }

        Claim[] claims;
        try {
            claims = UserStoreUtils.getUser(userID);
        } catch (Exception e) {
            log.error("Cannot find user for change password operation", e);
            MessageUtils.setError(request, response,
                    422, "User doesn't exist", log);
            return;
        }

        //put initial information into IDN_USER_PASS_CHANGES
        try {
            requestID = UUID.randomUUID().toString();
            IdentityWorkStoreUtils.initUserChangePassword(requestID, userID);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        JSONObject outbody = new JSONObject();
        outbody.put("sessionDataKey", requestID);
        outbody.put("appID", appID);
        outbody.put("lang", lang);

        MessageUtils.setSuccess(response, outbody, log);

    }

    @SuppressWarnings("unchecked")
    public static void submitChangePassword(HttpServletRequest request, HttpServletResponse response,
                                      String uri) throws IOException {

        String requestID = CommonUtils.getParamFromString(uri, "submit/", "/");
        if (StringUtils.isEmpty(requestID)) {
            MessageUtils.setError(request, response,
                    422, "requestID cannot be empty", log);
            return;
        }
        Boolean status;
        String userId;
        //get current status of requestID from IDN_USER_PASS_CHANGES
        try {
            status = IdentityWorkStoreUtils.checkChangePassword(requestID);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        if (status == null) {
            MessageUtils.setError(request, response,
                    422, String.format("There is no request with id [%s]", requestID), log);
            return;
        }

        //check requestID for verification
        if (Boolean.TRUE.equals(status)) {
            MessageUtils.setError(request, response,
                    422, "Request has already been finished", log);
            return;
        }

        ConsentCheckRs consentCheckRs = null;
        consentCheckRs = consentCheck(consentCheckRs, requestID, request, response);
        if (consentCheckRs == null) {
            return;
        }

        //check OTP verification
        if (!Integer.valueOf(1).equals(consentCheckRs.getVerified())) {
            MessageUtils.setError(request, response,
                    422, "OTP hasn't been verified yet", log);
            return;
        }

        if (consentCheckRs.getMessage().get("oldPassword").equals(consentCheckRs.getMessage().get("newPassword"))) {
            MessageUtils.setError(request, response,
                    422, "New password should not be same with current password. Please reset new password", log);
            return;
        }

        try {
            userId = IdentityWorkStoreUtils.getUserChangePassword(requestID);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        boolean isAuthenticated = false;
        try {
            isAuthenticated = UserStoreUtils.doAuthenticate(userId, consentCheckRs.getMessage().get("oldPassword"));
        } catch (UserStoreUtils.UserStoreConnectionException | UserStoreException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }
        if (!isAuthenticated) {
            MessageUtils.setError(request, response,
                    422, "Current password incorrect, please check and retry", log);
            return;
        }

        try {
            //change password in LDAP
            UserStoreUtils.changePassword(userId,
                    consentCheckRs.getMessage().get("newPassword"),
                    consentCheckRs.getMessage().get("oldPassword"));

            //update IDN_USER_PASS_CHANGES
            IdentityWorkStoreUtils.submitUserChangePassword(requestID);
        } catch (SQLException | IdentityException | UserStoreUtils.UserStoreConnectionException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        } catch (UserStoreException e) {
            log.error(e.getMessage());
            if ("This password has been used in recent history. Please choose a different password".equalsIgnoreCase(e.getMessage())) {
                MessageUtils.setError(request, response,
                        422, "New password should not be same with three last password", log);
                return;
            }

            MessageUtils.setError(request, response,
                    422, "Impossible to change password for user", log);
            return;
        }

        JSONObject outbody = new JSONObject();
        outbody.put("passwordChanged", consentCheckRs.getMessage().get("oldPassword"));
        //create response
        MessageUtils.setSuccess(response, outbody, log);
    }

    @SuppressWarnings("unchecked")
    public static void updateChangePassword(HttpServletRequest request, HttpServletResponse response,
                                      String uri) throws IOException {
        String userId;
        String requestID = CommonUtils.getParamFromString(uri, "update/", "/");

        if (StringUtils.isEmpty(requestID)) {
            MessageUtils.setError(request, response,
                    422, "requestID cannot be empty", log);
            return;
        }

        Boolean status;

        //get current status of requestID from IDN_USER_PASS_CHANGES
        try {
            status = IdentityWorkStoreUtils.checkChangePassword(requestID);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        if (status == null) {
            MessageUtils.setError(request, response,
                    422, String.format("There is no request with id [%s]", requestID), log);
            return;
        }

        //check requestID for verification
        if (Boolean.TRUE.equals(status)) {
            MessageUtils.setError(request, response,
                    422, "Request has already been finished", log);
            return;
        }

        //get request body
        ChangePasswordRq req;
        try {
            req = (ChangePasswordRq) MessageUtils.parseJSONreq(request, ChangePasswordRq.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            MessageUtils.setError(request, response,
                    422, "Error during request parsing", log);
            return;
        }

        //business check
        if (StringUtils.equals(req.getNewPassword(), req.getOldPassword())) {
            MessageUtils.setError(request, response,
                    400, "CHANGE_PASS_EQUAL_PASSWORDS", log);
            return;
        }

        //get information from user
        String mobile = null;
        Claim[] claims;
        try {
            userId = IdentityWorkStoreUtils.getUserChangePassword(requestID);
            if (userId == null) {
                MessageUtils.setError(request, response,
                        422, String.format("There is no user for request id [%s]", requestID), log);
                return;
            }
            claims = UserStoreUtils.getUser(userId);
        } catch (SQLException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        } catch (Exception e) {
            log.error("Something wrong with userstore interaction", e);
            MessageUtils.setError(request, response,
                    500, "Error during claims reading", log);
            return;
        }
        for (Claim claim : claims) {
            switch (claim.getClaimUri()) {
                case "http://wso2.org/claims/mobile":
                    mobile = claim.getValue();
                    break;
            }
        }

        try {
            //create Consent record
            Map<String, List<String>> consentCallQuery = new HashMap<>();
            consentCallQuery.put("phoneNumber", Collections.singletonList(mobile));
            consentCallQuery.put("operation", Collections.singletonList("change_password"));
            HTTPCall consentCall = new HTTPCall(
                    RESTServiceComponent.getRESTOptions().getconsentCreateURL() + "/" + requestID,
                    MessageUtils.generateJSON(req),
                    null,
                    consentCallQuery);
            consentCall.executeOperation("POST");
        } catch (HTTPCall.HTTPCallException e) {
            //impossible to call backends
            processConsentError(request, response, requestID, e);
            return;
        }

        JSONObject outbody = new JSONObject();
        outbody.put("OTP status", "initialized");

        MessageUtils.setSuccess(response, outbody, log);
    }

    private static ConsentCheckRs consentCheck(ConsentCheckRs consentCheckRs, String requestId,
                                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            //get Consent record
            Map<String, List<String>> consentCallQuery = new HashMap<>();
            consentCallQuery.put("requestId", Collections.singletonList(requestId));
            HTTPCall consentCall = new HTTPCall(
                    RESTServiceComponent.getRESTOptions().getconsentCheckURL(),
                    "{}",
                    null,
                    consentCallQuery);
            consentCall.executeOperation("GET");
            consentCheckRs = (ConsentCheckRs) MessageUtils.parseJSON(consentCall.getResponse(), ConsentCheckRs.class);
        } catch (HTTPCall.HTTPCallException e) {
            //impossible to call backends
            if (Integer.valueOf(404).equals(e.getResult().getStatus())) {
                MessageUtils.setError(request, response,
                        e.getResult().getStatus(), String.format("There is no information for requestId [%s] in Consent MSA", requestId), log);
            } else {
                MessageUtils.setError(request, response,
                        e.getResult().getStatus(), e.getMessage(), log);
            }
            log.error("Status: " + String.valueOf(e.getResult().getStatus())
                    + " reason: " + e.getMessage());
        }
        return consentCheckRs;
    }

    public static void processConsentError(HttpServletRequest request, HttpServletResponse response, String sessionDataKey, HTTPCall.HTTPCallException e) throws IOException {
        //impossible to call backends
        log.error("Status: " + String.valueOf(e.getResult().getStatus())
                + " reason: " + e.getMessage());
        if (Integer.valueOf(500).equals(e.getResult().getStatus())) {
            MessageUtils.setError(request, response,
                    e.getResult().getStatus(), String.format("Cannot create record in Consent service for requestId [%s], use another requestId", sessionDataKey), log);
            return;
        }
        MessageUtils.setError(request, response,
                e.getResult().getStatus(), e.getMessage(), log);
        log.error("Status: " + String.valueOf(e.getResult().getStatus())
                + " reason: " + e.getMessage());
    }

}
