package vn.vpbank.wso2.identity.service.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.ChallengeQuestionProcessor;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.user.api.UserStoreException;
import vn.vpbank.wso2.identity.service.rest.internal.RESTServiceComponent;
import vn.vpbank.wso2.identity.service.rest.model.backend.*;
import vn.vpbank.wso2.identity.service.rest.model.web.*;
import vn.vpbank.wso2.identity.service.rest.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class ResetPasswordService {
    private static Log log = LogFactory.getLog(ChangePasswordService.class);


    @SuppressWarnings("unchecked")
    public static void initResetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String lang = request.getHeader("IDN-Lang");
        String appId = request.getHeader("IDN-App");
        String sessionDataKey = UUID.randomUUID().toString();

        JSONObject outbody = new JSONObject();
        outbody.put("sessionDataKey", sessionDataKey);
        outbody.put("appID", appId);
        outbody.put("lang", lang);

        //put initial information into IDN_USER_PASS_CHANGES
        try {
            IdentityWorkStoreUtils.initUserChangePassword(sessionDataKey, "");
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        MessageUtils.setSuccess(response, outbody, log);
    }

    @SuppressWarnings("unchecked")
    public static void updateResetPassword(HttpServletRequest request, HttpServletResponse response,
                                     String uri) throws IOException {
        String lang = request.getHeader("IDN-Lang");
        String appId = request.getHeader("IDN-App");
        String sessionDataKey = CommonUtils.getParamFromString(uri, "update/", "/");

        if (StringUtils.isEmpty(sessionDataKey)) {
            MessageUtils.setError(request, response,
                    422, "RequestID cannot be empty", log);
            return;
        }

        String securedId;
        Boolean status;

        //get current status of requestID from IDN_USER_PASS_CHANGES
        try {
            status = IdentityWorkStoreUtils.checkChangePassword(sessionDataKey);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        if (status == null) {
            MessageUtils.setError(request, response,
                    422, String.format("There is no request with id [%s]", sessionDataKey), log);
            return;
        }

        //get request body
        Reset_CustomersDataRq req;
        try {
            req = (Reset_CustomersDataRq) MessageUtils.parseJSONreq(request, Reset_CustomersDataRq.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            MessageUtils.setError(request, response,
                    400, "Error during request parsing", log);
            return;
        }

        //generate securedId
        securedId = Integer.toHexString(sessionDataKey.hashCode()) + Integer.toHexString((req.getPhoneNumber() + appId).hashCode()) + Integer.toHexString((req.getAccountNumber() + "vgf367ESwhb23Q").hashCode());

        //get UserId and CIF
        Map<String, String> attrs = null;
        Reset_LDAP_userInfo userInfo = null;
        String userId;
        String cif;
        try {
            attrs = UserStoreUtils.getUserClaims(req.getPhoneNumber());
        } catch (UserStoreException e) {
            MessageUtils.setError(request, response,
                    404, "User not exist", log);
            log.error(e.getMessage());
            return;
        } catch (UserStoreUtils.UserStoreConnectionException e) {
            log.error("Something wrong with userstore interaction", e);
            MessageUtils.setError(request, response,
                    500, "Error during claims reading", log);
            return;
        }

        userInfo = new Reset_LDAP_userInfo(attrs);
        userId = userInfo.getUserName();
        cif = userInfo.getCIF();

        Boolean answerIsTrue = false;
        FindVPBPlusChannelRs findVPBPlusChannelRs = null;
        CustomerInfoRs customerInfoRs = null;
        String secretQuestion = "";

        //TODO - add if app is not VPBPlus

        if (appId.toLowerCase().contains("vpbplus")) {
            //find User in MSA
            try {
                Map<String, List<String>> vpbplusCallQuery = new HashMap<>();
                vpbplusCallQuery.put("phoneNumber", Collections.singletonList(req.getPhoneNumber()));
                HTTPCall vpbplusCall = new HTTPCall(
                        RESTServiceComponent.getRESTOptions().getVpbplusFindURL(),
                        "{}",
                        null,
                        vpbplusCallQuery);
                vpbplusCall.executeOperation("GET");
                findVPBPlusChannelRs = (FindVPBPlusChannelRs) MessageUtils.parseJSON(vpbplusCall.getResponse().replaceAll("\r", ""), FindVPBPlusChannelRs.class);
            } catch (HTTPCall.HTTPCallException e) {
                //impossible to call backends
                MessageUtils.setError(request, response,
                        e.getResult().getStatus(), e.getMessage(), log);
                log.error("Status: " + String.valueOf(e.getResult().getStatus())
                        + " reason: " + e.getMessage());
            }

            //get legalId from T24
            try {
                Map<String, List<String>> customerInfoCallHeaders = new HashMap<>();
                customerInfoCallHeaders.put("CIF", Collections.singletonList(cif));

                HTTPCall customerCall = new HTTPCall(
                        RESTServiceComponent.getRESTOptions().getCustomerInfoURL(),
                        "{}",
                        null,
                        customerInfoCallHeaders);
                customerCall.executeOperation("GET");
                customerInfoRs = (CustomerInfoRs) MessageUtils.parseJSON(customerCall.getResponse().replaceAll("\r", ""), CustomerInfoRs.class);
            } catch (HTTPCall.HTTPCallException e) {
                //impossible to call backends
                MessageUtils.setError(request, response,
                        e.getResult().getStatus(), e.getMessage(), log);
                log.error("Status: " + String.valueOf(e.getResult().getStatus())
                        + " reason: " + e.getMessage());
            }

            //get secretQuestions/secretAnswers
            try {
                String st = UserStoreUtils.getUserClaim(req.getPhoneNumber(), "http://wso2.org/claims/stateorprovince");

                String secretClaim = "";
                if (st.contains("!")) {
                    int posDelim = st.indexOf("!");
                    if (posDelim != -1) {
                        secretClaim = st.substring(0, posDelim);
                    }
                } else {
                    secretClaim = st;
                }

                String challenge = UserStoreUtils.getUserClaim(req.getPhoneNumber(), secretClaim);
                int posDelim = challenge.indexOf("!");
                if (posDelim != -1) {
                    secretQuestion = challenge.substring(0, posDelim);
                }

                ChallengeQuestionProcessor processor = new ChallengeQuestionProcessor();

                UserChallengesDTO answerDTO = new UserChallengesDTO();
                answerDTO.setId(IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI01);
                answerDTO.setQuestion(req.getSecretQuestion());
                answerDTO.setAnswer(req.getSecretAnswer());

                answerIsTrue = processor.verifyUserChallengeAnswer(userId, -1234, answerDTO);
            } catch (UserStoreException e) {
                MessageUtils.setError(request, response,
                        404, "User not exist", log);
                log.error(e.getMessage());
                return;
            } catch (UserStoreUtils.UserStoreConnectionException e) {
                log.error("Something wrong with userstore interaction", e);
                MessageUtils.setError(request, response,
                        500, "Error during claims reading", log);
                return;
            }
        }

        //Create consent record
        try {
            Map<String, List<String>> consentCallQuery = new HashMap<>();
            consentCallQuery.put("phoneNumber", Collections.singletonList(req.getPhoneNumber()));
            consentCallQuery.put("operation", Collections.singletonList("reset_password"));
            Timestamp initDateTime = new Timestamp(System.currentTimeMillis());
            //check data
            if (req.getCardId().equals(customerInfoRs.getLegalid()) &&
                    req.getAccountNumber().equals(findVPBPlusChannelRs.getDefaultAccountNumber()) &&
                    req.getSecretQuestion().equals(secretQuestion) && answerIsTrue) {
                req.setCorrectData(true);
                req.setSecuredId(securedId);
                req.setInitDateTime(initDateTime);
                req.setUserId(userId);
            } else {
                req.setCorrectData(false);
            }

            req.setPhoneNumber(null);
            req.setSecretAnswer(null);
            req.setSecretQuestion(null);

            HTTPCall consentCall = new HTTPCall(
                    RESTServiceComponent.getRESTOptions().getconsentCreateURL() + "/" + sessionDataKey,
                    MessageUtils.generateJSON(req),
                    null,
                    consentCallQuery);
            consentCall.executeOperation("POST");
        } catch (HTTPCall.HTTPCallException e) {
            processConsentError(request, response, sessionDataKey, e);
            return;
        }

        JSONObject outbody = new JSONObject();
        outbody.put("sessionDataKey", sessionDataKey);
        outbody.put("appID", appId);
        outbody.put("lang", lang);
        response.setHeader("securedId", securedId);
        MessageUtils.setSuccess(response, outbody, log);
    }


    public static void checkResetPassword(HttpServletRequest request, HttpServletResponse response,
                                    String uri) throws IOException {
        String appId = request.getHeader("IDN-App");
        String lang = request.getHeader("IDN-Lang");
        String sessionDataKey = CommonUtils.getParamFromString(uri, "check/", "/");

        if (StringUtils.isEmpty(sessionDataKey)) {
            MessageUtils.setError(request, response,
                    422, "RequestID cannot be empty", log);
            return;
        }

        Boolean status;

        //get current status of requestID from IDN_USER_PASS_CHANGES
        try {
            status = IdentityWorkStoreUtils.checkChangePassword(sessionDataKey);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        if (status == null) {
            MessageUtils.setError(request, response,
                    422, String.format("There is no request with id [%s]", sessionDataKey), log);
            return;
        }

        ConsentCheckRs consentCheckRs = null;
        consentCheckRs = consentCheck(consentCheckRs, sessionDataKey, request, response);

        //check OTP verification
        if (!Integer.valueOf(1).equals(consentCheckRs.getVerified())) {
            MessageUtils.setError(request, response,
                    422, "OTP hasn't been verified yet", log);
        } else {
            //check data
            if (!consentCheckRs.getMessage().get("correctData").equals("true")) {
                MessageUtils.setError(request, response,
                        422, "Entered data is incorrect", log);
            } else {
                //update IDN_USER_PASS_CHANGES (set IS_CHANGED = 1)
                try {
                    IdentityWorkStoreUtils.submitUserChangePassword(sessionDataKey);
                } catch (SQLException | IdentityException e) {
                    MessageUtils.setError(request, response,
                            500, e.getMessage(), log);
                    log.error(e.getMessage());
                    return;
                }

                //create response
                response.setContentType("application/xml");
                MessageUtils.setRedirect(response, log,
                        RESTServiceComponent.getRESTOptions().getReset_passURL() + "?sessionDataKey=" + sessionDataKey,
                        lang, appId);
            }
        }
    }

    public static void submitResetPassword(HttpServletRequest request, HttpServletResponse response,
                                     String uri) throws IOException {
        String sessionDataKey = CommonUtils.getParamFromString(uri, "submit/", "/");

        if (StringUtils.isEmpty(sessionDataKey)) {
            MessageUtils.setError(request, response,
                    422, "RequestID cannot be empty", log);
            return;
        }

        Boolean status;

        //get current status of requestID from IDN_USER_PASS_CHANGES
        try {
            status = IdentityWorkStoreUtils.checkChangePassword(sessionDataKey);
        } catch (SQLException | IdentityException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        if (status == null) {
            MessageUtils.setError(request, response,
                    422, String.format("There is no request with id [%s]", sessionDataKey), log);
            return;
        }

        //check requestID for verification
        if (Boolean.FALSE.equals(status)) {
            MessageUtils.setError(request, response,
                    422, "The entered data was not confirmed", log);
            return;
        } else {

            //get request body
            Reset_NewPasswordRq req;
            try {
                req = (Reset_NewPasswordRq) MessageUtils.parseJSONreq(request, Reset_NewPasswordRq.class);
            } catch (Exception e) {
                log.error(e.getMessage());
                MessageUtils.setError(request, response,
                        400, "Error during request parsing", log);
                return;
            }

            String securedId = request.getHeader("securedId");
            String password = req.getPassword();

            ConsentCheckRs consentCheckRs = null;
            consentCheckRs = consentCheck(consentCheckRs, sessionDataKey, request, response);


            String userId = consentCheckRs.getMessage().get("userId");

            if (securedId.equals(consentCheckRs.getMessage().get("securedId"))) {
                //check expiration of token
                int sec = 600;
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(consentCheckRs.getMessage().get("initDateTime")));
                cal.add(Calendar.SECOND, sec);
                Timestamp dbTime = new Timestamp(cal.getTime().getTime());

                if (new Timestamp(System.currentTimeMillis()).after(dbTime)) {
                    MessageUtils.setError(request, response,
                            422, "Your securedId is out of date", log);
                } else {
                    Boolean passwordsIsMatch;
                    try {
                        passwordsIsMatch = UserStoreUtils.doAuthenticate(userId, password);
                    } catch (UserStoreException e) {
                        MessageUtils.setError(request, response,
                                500, e.getMessage(), log);
                        log.error(e.getMessage());
                        return;
                    } catch (UserStoreUtils.UserStoreConnectionException e) {
                        log.error("Something wrong with userstore interaction", e);
                        MessageUtils.setError(request, response,
                                500, "Error during claims reading", log);
                        return;
                    }

                    if (passwordsIsMatch) {
                        MessageUtils.setError(request, response,
                                422, "New password can't be same as previous one", log);
                    } else {
                        try {
                            //change password in LDAP
                            UserStoreUtils.resetPassword(userId, password);

                            //create response
                            MessageUtils.setSuccess(response, "", log);
                        } catch (UserStoreUtils.UserStoreConnectionException e) {
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
                                    422, "Impossible to reset password for user", log);
                            return;
                        }
                    }
                }
            } else {
                MessageUtils.setError(request, response,
                        422, "Access denied", log);
            }
        }
    }

    public static ConsentCheckRs consentCheck(ConsentCheckRs consentCheckRs, String requestId,
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
