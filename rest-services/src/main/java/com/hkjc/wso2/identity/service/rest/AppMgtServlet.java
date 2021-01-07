
package com.hkjc.wso2.identity.service.rest;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import com.hkjc.wso2.identity.service.rest.utils.CommonUtils;
import com.hkjc.wso2.identity.service.rest.utils.IdentityWorkStoreUtils;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;

/**
 * Implemantation of servlets for custom application management APIs
 */
public class AppMgtServlet extends HttpServlet {

    private static final long serialVersionUID = -7182121722709941646L;
    private static Log log = LogFactory.getLog(AppMgtServlet.class);

    /**
     * Standard init function
     */
    @Override
    public void init() {
        ConfigurationFacade.getInstance();
    }

    /**
     * Get method valve.
     * For a convenience the routing of all requests is processed at doPost
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Post method valve.
     * Process all requests
     * @param request is input servlet object
     * @param response is blank for output servlet object
     * @throws ServletException if there are servlet problems
     * @throws IOException if there are problems with message reading/writing
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (FrameworkUtils.getMaxInactiveInterval() == 0) {
            FrameworkUtils.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
        }
        try {
            log.debug(request.getRequestURI());
            String relativeURI = request.getRequestURI().substring(request.getContextPath().length());

            // the main request mapping
            if (relativeURI.startsWith("/url/") && request.getMethod().equals("GET")) {
                getCallbackURL(request, response, relativeURI);
            } else {
                MessageUtils.setError(request, response,
                        HttpServletResponse.SC_NOT_FOUND,
                        "Service wasn't found", log);
            }

        } catch (Exception e){
            log.error("Unknown error during application management processing", e);
            MessageUtils.setError(request, response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unknown internal error", log);

        }
    }

    /**
     * The getting of callback uri by application name
     * @param request is input servlet object
     * @param response is blank for output servlet object
     * @param url is relative URL for getting of path params
     * @throws IOException if there are some errors with response creation
     */
    private void getCallbackURL (HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String appName = URLDecoder.decode(CommonUtils.getParamFromString(url,"/url/","/"), "UTF-8");
        String callback;
        try {
            callback = IdentityWorkStoreUtils.getCallbackURL(appName);
        } catch (Exception e) {
            log.error("Something wrong with DAO processing", e);
            MessageUtils.setError(request, response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error during DAO interaction", log);
            return;
        }
        if (callback == null || callback.length()<1){
            MessageUtils.setError(request, response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Callback URL wasn't found", log);
        } else {
            JSONObject body = new JSONObject();
            body.put("url", callback);
            MessageUtils.setSuccess(response, body, log);
        }
    }
}