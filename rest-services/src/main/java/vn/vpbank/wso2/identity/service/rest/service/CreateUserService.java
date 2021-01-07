package vn.vpbank.wso2.identity.service.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;
import vn.vpbank.wso2.identity.service.rest.internal.RESTServiceComponent;
import vn.vpbank.wso2.identity.service.rest.utils.*;
import vn.vpbank.wso2.identity.service.rest.model.web.*;
import vn.vpbank.wso2.identity.service.rest.model.backend.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class CreateUserService {

    private static Log log = LogFactory.getLog(CreateUserService.class);


    /**
     * The user creation
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //get request body
        CreateUserRq req;
        try {
            req = (CreateUserRq) MessageUtils.parseJSONreq(request, CreateUserRq.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            MessageUtils.setError(request, response,
                    400, "Error during request parsing", log);
            return;
        }

        //business checks
        if (req.getMobile() == null) {
            MessageUtils.setError(request, response,
                    422, "Mobile is mandatory", log);
            return;
        }

        //properties generation
        String userName;
        try {
            userName = UserStoreUtils.getFreeID();
        } catch (UserStoreException e) {
            log.error(e.getMessage());
            MessageUtils.setError(request, response,
                    422, "Impossible to find free userName", log);
            return;
        } catch (UserStoreUtils.UserStoreConnectionException e) {
            //some errors with userstore
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }
        String friendlyUserName = userName +
                " (" + req.getMobile() +
                " / " + req.getLoginName() + ")";
        if (req.getLoginName() == null) {
            req.setLoginName(userName);
        }
        boolean passIsNew = false;
        if ((req.getPassword() == null) && (req.getPasswordAES() == null)) {
            try {
                req.setPassword(CommonUtils.generatePassword());
            } catch (Exception e) {
                MessageUtils.setError(request, response,
                        500, "Password generation error", log);
                return;
            }
            passIsNew = true;
            log.info("Password has been generated");
        } else if (req.getPasswordAES() != null) {
            try {
                req.setPassword(CommonUtils.decrypt(req.getPasswordAES(),
                        RESTServiceComponent.getRESTOptions().getKey()));
            } catch (Exception e) {
                log.error(e.getMessage());
                MessageUtils.setError(request, response,
                        400, "Error during password decription", log);
                return;
            }
        }


        //user creation
        try {
            UserStoreUtils.addUser(userName, req.getPassword());
            log.info("User " + friendlyUserName + " has been created");
        } catch (UserStoreException e) {
            //impossible to create user
            MessageUtils.setError(request, response,
                    422, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        } catch (UserStoreUtils.UserStoreConnectionException e) {
            //some errors with userstore
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        //setup optional claims
        try {
            Map<String, String> claimsMap = new HashMap();
            claimsMap.put("http://wso2.org/claims/chpass", String.valueOf(req.getPasswordChangeRequired()));
            if (req.getMobile() != null) {
                claimsMap.put("http://wso2.org/claims/mobile", req.getMobile());
            }
            if (req.getFullName() != null) {
                claimsMap.put("http://wso2.org/claims/fullname", req.getFullName());
            }
            if (req.getEmail() != null) {
                claimsMap.put("http://wso2.org/claims/emailaddress", req.getEmail());
            }
            if (req.getCAID() != null) {
                claimsMap.put("http://wso2.org/claims/externalid", req.getCAID());
            }
            if (req.getCIF() != null) {
                claimsMap.put("http://wso2.org/claims/CIF", req.getCIF());
            }
            if (req.getLoginName() != null) {
                claimsMap.put("http://wso2.org/claims/userid", req.getLoginName());
            }
            if (req.getLang() != null) {
                claimsMap.put("http://wso2.org/claims/locality", req.getLang());
            }
            log.info("User " + friendlyUserName + " try to be changed: " + StringUtils.join(claimsMap));
            UserStoreUtils.setUserClaims(userName, claimsMap);
            log.info("User " + friendlyUserName + " has been changed: ");
            if (passIsNew) {
                //get template text
                GeneratePasswordTemplateRq generatePasswordTemplateRq =
                        new GeneratePasswordTemplateRq(req.getPassword());
                Map<String, List<String>> templateCallQuery = new HashMap<>();
                templateCallQuery.put("id", Arrays.asList(RESTServiceComponent.getRESTOptions().getSmsTemplateID()));
                templateCallQuery.put("lang", Arrays.asList(req.getLang()));
                HTTPCall templateCall = new HTTPCall(
                        RESTServiceComponent.getRESTOptions().getTemplateURL(),
                        MessageUtils.generateJSON(generatePasswordTemplateRq),
                        null,
                        templateCallQuery);
                templateCall.executeOperation("POST");
                //send sms
                SendSMSRq sendSMSRq = new SendSMSRq(req.getMobile(), templateCall.getResponse());
                HTTPCall smsCall = new HTTPCall(
                        RESTServiceComponent.getRESTOptions().getSmsURL(),
                        MessageUtils.generateJSON(sendSMSRq), null, null);
                smsCall.executeOperation("POST");
                log.info("Password has been sent to " + req.getMobile());
            }
        } catch (UserStoreException e) {
            //impossible to setup claims
            MessageUtils.setError(request, response,
                    422, e.toString(), log);
            log.error(e.toString());
            deleteUserAfterCreate(userName);
            return;
        } catch (UserStoreUtils.UserStoreConnectionException | IOException e) {
            //problems with userstore or backends
            MessageUtils.setError(request, response,
                    500, e.toString(), log);
            log.error(e.toString());
            deleteUserAfterCreate(userName);
            return;
        } catch (HTTPCall.HTTPCallException e) {
            //impossible to call backends
            MessageUtils.setError(request, response,
                    e.getResult().getStatus(), e.getMessage(), log);
            deleteUserAfterCreate(userName);
            log.error("Status: " + String.valueOf(e.getResult().getStatus())
                    + " reason: " + e.getMessage());
            return;
        } catch (Exception e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            deleteUserAfterCreate(userName);
            log.error("Status: " + String.valueOf(500)
                    + " reason: " + e.getMessage());
            return;
        }
        //create response
        MessageUtils.setSuccess(response,
                MessageUtils.generateJSON(new CreateUserRs(userName)), log);
    }

    private static void deleteUserAfterCreate(String userName) {
        log.info("Trying to delete " + userName);
        try {
            UserStoreUtils.deleteUser(userName);
            log.info("User " + userName + " has been deleted");
        } catch (UserStoreUtils.UserStoreConnectionException | UserStoreException e1) {
            log.error(e1.getMessage());
        }
    }
}
