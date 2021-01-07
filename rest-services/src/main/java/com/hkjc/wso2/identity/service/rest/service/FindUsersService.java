package com.hkjc.wso2.identity.service.rest.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.user.core.claim.Claim;

import com.hkjc.wso2.identity.service.rest.model.ClaimEnum;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;
import com.hkjc.wso2.identity.service.rest.utils.UserStoreUtils;

public class FindUsersService {
    private static Log log = LogFactory.getLog(FindUsersService.class);

    /**
     * The search of users by claim value
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void findUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //Claims for search
        final Set<ClaimEnum> claimsForSearch = new HashSet<>(Arrays.asList(
                ClaimEnum.MOBILE,
                ClaimEnum.EMAIL,
                ClaimEnum.CAID,
                ClaimEnum.CIF,
                ClaimEnum.LOGIN,
                ClaimEnum.USER_NAME,
                ClaimEnum.FULL_NAME));

        //Claims for output
        final Set<ClaimEnum> claimsForOutput = new HashSet<>(Arrays.asList(
                ClaimEnum.MOBILE,
                ClaimEnum.EMAIL,
                ClaimEnum.CAID,
                ClaimEnum.CIF,
                ClaimEnum.LOGIN,
                ClaimEnum.USER_NAME,
                ClaimEnum.FULL_NAME));

        Map<String, String[]> requestParametersMap = request.getParameterMap();
        TreeMap<ClaimEnum, String> filterClaims = new TreeMap<>();

        for (String paramName : requestParametersMap.keySet()) {
            ClaimEnum claimEnum = ClaimEnum.getClaimEnumByParamName(paramName);

            if ((claimEnum != null) && (claimsForSearch.contains(claimEnum))) {
                filterClaims.put(claimEnum, requestParametersMap.get(paramName)[0]);
            } else {
                MessageUtils.setError(request, response,
                        500, "Impossible to find users by parameter " + paramName, log);
                return;
            }
        }

        if (filterClaims.isEmpty()) {
            MessageUtils.setError(request, response,
                    500, "Impossible to find users without available parameter for search", log);
            return;
        }

        String[] users;
        try {
            Map.Entry<ClaimEnum, String> firstEntry = filterClaims.firstEntry();

            //Search with escape '*'

            users = UserStoreUtils.findUsers(firstEntry.getKey().getClaimURI(), firstEntry.getValue().replace("*", "\\*"));
            filterClaims.remove(firstEntry.getKey());
        } catch (Exception e) {
            log.error("Something wrong with userstore interaction", e);
            MessageUtils.setError(request, response,
                    500, "Error during connection to userstore", log);
            return;
        }

        JSONArray userList = new JSONArray();
        for (String user : users) {
            Claim[] claims;
            JSONObject userobj = new JSONObject();
            try {
                claims = UserStoreUtils.getUser(user);
            } catch (Exception e) {
                log.error("Something wrong with userstore interaction!", e);
                MessageUtils.setError(request, response,
                        500, "Error during claims reading", log);
                return;
            }

            boolean isAllFilterClaims = true;

            for (Claim claim : claims) {

                ClaimEnum claimEnum = ClaimEnum.getClaimEnumByClaimURI(claim.getClaimUri());
                if (claimEnum != null) {
                    if (filterClaims.containsKey(claimEnum) && (!filterClaims.get(claimEnum).equals(claim.getValue()))) {
                        isAllFilterClaims = false;
                        break;
                    }

                    if (claimsForOutput.contains(claimEnum)) {
                        userobj.put(claimEnum.getParamName(), claim.getValue());
                    }
                }
            }
            if (isAllFilterClaims) {
                userList.add(userobj);
            }
        }

        if (userList.size() <= 0) {
            MessageUtils.setError(request, response,
                    404, "Not found", log);
            return;
        }

        JSONObject outbody = new JSONObject();
        outbody.put("userList", userList);
        MessageUtils.setSuccess(response, outbody, log);
    }

}
