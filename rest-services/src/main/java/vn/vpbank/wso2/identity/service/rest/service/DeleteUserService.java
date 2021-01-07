package vn.vpbank.wso2.identity.service.rest.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;
import vn.vpbank.wso2.identity.service.rest.utils.CommonUtils;
import vn.vpbank.wso2.identity.service.rest.utils.MessageUtils;
import vn.vpbank.wso2.identity.service.rest.utils.UserStoreUtils;
import vn.vpbank.wso2.identity.service.rest.model.web.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

public class DeleteUserService {
    private static Log log = LogFactory.getLog(DeleteUserService.class);

    /**
     * The deleting of user
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @param uri      is relative URL for getting of path params
     * @throws IOException if there are some errors with response creation
     */
    @SuppressWarnings("unchecked")
    public static void deleteUser(HttpServletRequest request, HttpServletResponse response,
                                  String uri) throws IOException {
        String userName = URLDecoder.decode(CommonUtils.getParamFromString(uri, "/", "/"), "UTF-8");

        Map<String, String> attrs = null;
        DeleteUserRs answer = null;
        try {

            //check user exist and get parameters
            try {
                attrs = UserStoreUtils.getUserClaims(userName);
            } catch (UserStoreException e) {
                MessageUtils.setError(request, response,
                        404, "User not exist", log);
                log.error(e.getMessage());
                return;
            }

            //prepare answer
            answer = new DeleteUserRs(attrs);

            //delete user
            try {
                UserStoreUtils.deleteUser(userName);
                log.info("User " + userName + " has been deleted. Params: " + StringUtils.join(attrs));
            } catch (UserStoreException e) {
                MessageUtils.setError(request, response,
                        500, "User can't be deleted", log);
                log.error(e.getMessage());
                return;
            }

        } catch (UserStoreUtils.UserStoreConnectionException e) {
            log.error("Something wrong with userstore interaction", e);
            MessageUtils.setError(request, response,
                    500, "Error during claims reading", log);
            return;
        }

        //send response
        MessageUtils.setSuccess(response,
                MessageUtils.generateJSON(answer), log);
    }

}
