package com.hkjc.wso2.identity.service.rest.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.user.api.UserStoreException;

import com.hkjc.wso2.identity.service.rest.model.web.Mappings;
import com.hkjc.wso2.identity.service.rest.utils.CommonUtils;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;
import com.hkjc.wso2.identity.service.rest.utils.UserStoreUtils;

public class ChangeAttributeService {
    private static Log log = LogFactory.getLog(ChangeAttributeService.class);

    //TODO - rework to object serialization approach

    /**
     * The user attribute changing
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void changeAttribute(HttpServletRequest request, HttpServletResponse response,
                                 String uri) throws IOException {

        //get userName
        String userName;
        String uriUserName = CommonUtils.getParamFromString(uri, "/attribute/", "/");
        if (uriUserName != null && uriUserName.length() > 0) {
            userName = URLDecoder.decode(uriUserName, "UTF-8");
        } else {
            userName = request.getHeader("IDN-User");
        }

        //get request body
        JSONObject inbody;
        try {
            inbody = MessageUtils.parseJSONRequest(request);
        } catch (Exception e) {
            log.error(e.getMessage());
            MessageUtils.setError(request, response,
                    400, "Error during request parsing", log);
            return;
        }

        //business checks
        if (userName == null || userName.length() < 1) {
            MessageUtils.setError(request, response,
                    422, "Incorrect userName", log);
            return;
        }

        //extract business data
        Map<String, String> claimsToChange = MessageUtils.mapToClaimsToStringMap(inbody, Mappings.changeableClaimsMap);

        //business checks 2
        if (claimsToChange == null || claimsToChange.size() < 1) {
            MessageUtils.setError(request, response,
                    422, "Empty request", log);
            return;
        }


        //setup attributes
        Map<String, String> changedClaims;
        try {
            changedClaims = UserStoreUtils.changeUserClaims(userName, claimsToChange);
            log.info("User " + userName + " has been changed in "
                    + StringUtils.join(claimsToChange));
        } catch (UserStoreException e) {
            //impossible to setup claims
            MessageUtils.setError(request, response,
                    422, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        } catch (UserStoreUtils.UserStoreConnectionException e) {
            //impossible to setup claims
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        //create response
        JSONObject outbody = MessageUtils.mapFromClaimsToJSONStringsObj(changedClaims, Mappings.previousValueOfChangeableClaims);
        MessageUtils.setSuccess(response, outbody, log);
    }

}
