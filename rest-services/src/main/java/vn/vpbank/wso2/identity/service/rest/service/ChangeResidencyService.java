package vn.vpbank.wso2.identity.service.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.user.api.UserStoreException;
import vn.vpbank.wso2.identity.service.rest.utils.CommonUtils;
import vn.vpbank.wso2.identity.service.rest.utils.IdentityWorkStoreUtils;
import vn.vpbank.wso2.identity.service.rest.utils.MessageUtils;
import vn.vpbank.wso2.identity.service.rest.utils.UserStoreUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChangeResidencyService {
    private static Log log = LogFactory.getLog(ChangeResidencyService.class);

    //TODO - rework to object serialization approach

    /**
     * The user groups changing
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void changeResidency(HttpServletRequest request, HttpServletResponse response,
                                 String uri) throws IOException {
        //get userName
        String userName;
        String uriUserName = CommonUtils.getParamFromString(uri, "/residency/", "/");
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

        //business checks 2
        if ((addList == null || addList.size() < 1) && (deleteList == null || deleteList.size() < 1)) {
            MessageUtils.setError(request, response,
                    400, "Nothing to do", log);
            return;
        }

        JSONObject outbody = new JSONObject();
        try {
            //preparing groupnames
            List<String> addListGroups = CommonUtils.addSuffix(CommonUtils.cleanAppNames(addList), "_RESIDENT");
            List<String> deleteListGroups = CommonUtils.addSuffix(CommonUtils.cleanAppNames(deleteList), "_RESIDENT");

            //checking of groups for adding, checking of apps and group adding if
            // there is app but there is not group
            if (addList != null && addList.size() > 0) {
                //check that apps are active
                try {
                    List<String> wrongApps = checkApps(addList);
                    if (wrongApps.size() > 0) {
                        MessageUtils.setError(request, response,
                                422, "Not existing or innactive apps " +
                                        StringUtils.join(wrongApps), log);
                        return;
                    }
                } catch (SQLException | IdentityException e) {
                    MessageUtils.setError(request, response,
                            500, e.getMessage(), log);
                    log.error(e.getMessage());
                    return;
                }

                //checking of group existing
                List<String> wrongAddGroups = UserStoreUtils.areExistingRoles(addListGroups, null);

                //if there are missed groups then create them
                if (wrongAddGroups.size() > 0) {
                    for (String group : wrongAddGroups) {
                        UserStoreUtils.addRole(group);
                        log.info("Group " + group + " has been created");
                    }
                }
            }

            //checking of groups for deleting
            if (deleteList != null && deleteList.size() > 0) {
                //checking of group existing
                List<String> wrongDeleteGroups = UserStoreUtils.areExistingRoles(deleteListGroups, null);

                //if there are missed groups then delete them from list
                if (wrongDeleteGroups.size() > 0) {
                    deleteListGroups.removeAll(wrongDeleteGroups);
                }
            }

            //change groups
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


    /**
     * Checking of apps are existing and active
     *
     * @param appList contains full names of apps
     * @return names of applications are not exist or are innactive
     * @throws SQLException if there are problem with DB connection
     */
    private static List<String> checkApps(List<String> appList) throws SQLException, IdentityException {
        List<String> result = new ArrayList<>();
        if (appList != null) {
            for (String app : appList) {
                if (!IdentityWorkStoreUtils.isActiveApp(app)) {
                    result.add(app);
                }
            }
        }
        return result;
    }



}
