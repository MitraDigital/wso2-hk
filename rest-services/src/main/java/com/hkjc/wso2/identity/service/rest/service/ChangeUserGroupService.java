package com.hkjc.wso2.identity.service.rest.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.user.api.UserStoreException;

import com.hkjc.wso2.identity.service.rest.utils.CommonUtils;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;
import com.hkjc.wso2.identity.service.rest.utils.UserStoreUtils;

public class ChangeUserGroupService {
    private static Log log = LogFactory.getLog(ChangeUserGroupService.class);

    //TODO - rework to object serialization approach

    /**
     * The user groups changing
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void changeUserGroup(HttpServletRequest request, HttpServletResponse response,
                                 String uri) throws IOException {
        //get userName
        String userName;
        String uriUserName = CommonUtils.getParamFromString(uri, "/group/", "/");
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
                    400, "Incorrect userName", log);
            return;
        }


        //extract business data
        JSONArray addList = (JSONArray) inbody.get("addList");
        JSONArray deleteList = (JSONArray) inbody.get("deleteList");

        JSONObject outbody = new JSONObject();
        try {

            //business checks 2
            if ((addList == null || addList.size() < 1) && (deleteList == null || deleteList.size() < 1)) {
                MessageUtils.setError(request, response,
                        400, "Nothing to do", log);
                return;
            }
            List<String> wrongAddGroups = UserStoreUtils.areExistingRoles(addList, "_USER");
            if (wrongAddGroups.size() > 0) {
                MessageUtils.setError(request, response,
                        422, "not existing groups " + StringUtils.join(wrongAddGroups), log);
                return;
            }
            List<String> wrongDeleteGroups = UserStoreUtils.areExistingRoles(deleteList, "_USER");
            if (wrongDeleteGroups.size() > 0) {
                MessageUtils.setError(request, response,
                        422, "not existing group " + StringUtils.join(wrongDeleteGroups), log);
                return;
            }

            //setup groups
            List<String> addListGroups = CommonUtils.addSuffix(addList, "_USER");
            List<String> deleteListGroups = CommonUtils.addSuffix(deleteList, "_USER");
            UserStoreUtils.updateRoles(userName, deleteListGroups, addListGroups);
            if (addListGroups.size() > 0) {
                JSONArray glist = new JSONArray();
                glist.addAll(addListGroups);
                outbody.put("addedTo", glist);
                log.info("User " + userName + " has been added to : "
                        + StringUtils.join(addListGroups));
            }
            if (deleteListGroups.size() > 0) {
                JSONArray glist = new JSONArray();
                glist.addAll(deleteListGroups);
                outbody.put("deletedFrom", glist);
                log.info("User " + userName + " has been deleted from : "
                        + StringUtils.join(deleteListGroups));
            }

        } catch (UserStoreException e) {
            MessageUtils.setError(request, response,
                    422, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        } catch (UserStoreUtils.UserStoreConnectionException e) {
            MessageUtils.setError(request, response,
                    500, e.getMessage(), log);
            log.error(e.getMessage());
            return;
        }

        //create response
        MessageUtils.setSuccess(response, outbody, log);
    }

}
