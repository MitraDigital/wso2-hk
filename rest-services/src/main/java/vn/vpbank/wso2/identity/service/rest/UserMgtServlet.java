/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package vn.vpbank.wso2.identity.service.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import vn.vpbank.wso2.identity.service.rest.utils.*;
import vn.vpbank.wso2.identity.service.rest.service.*;
import vn.vpbank.wso2.identity.service.rest.model.web.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Implemantation of servlets for custom application management APIs
 */
public class UserMgtServlet extends HttpServlet {

    private static final long serialVersionUID = -7182221722709941646L;
    private static Log log = LogFactory.getLog(UserMgtServlet.class);

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
     * Dlete method valve.
     * For a convenience the routing of all requests is processed at doPost
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Put method valve.
     * For a convenience the routing of all requests is processed at doPost
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Post method valve.
     * Process all requests
     *
     * @param request  is input servlet object
     * @param response is blank for output servlet object
     * @throws ServletException if there are servlet problems
     * @throws IOException      if there are problems with message reading/writing
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
            if (relativeURI.startsWith("/new") && request.getMethod().equals("POST")) {
                CreateUserService.createUser(request, response);
            } else if (relativeURI.startsWith("/find") && request.getMethod().equals("GET")) {
                FindUsersService.findUsers(request, response);
            } else if (relativeURI.startsWith("/attribute") && request.getMethod().equals("PUT")) {
                ChangeAttributeService.changeAttribute(request, response, relativeURI);
            } else if (relativeURI.startsWith("/authenticate/") && request.getMethod().equals("GET")) {
                GetAuthenticateService.getAuthenticate(request, response, relativeURI);
            } else if (relativeURI.startsWith("/group") && request.getMethod().equals("PUT")) {
                ChangeUserGroupService.changeUserGroup(request, response, relativeURI);
            } else if (relativeURI.startsWith("/residency") && request.getMethod().equals("PUT")) {
                ChangeResidencyService.changeResidency(request, response, relativeURI);
            } else if (relativeURI.startsWith("/pass/change/init") && request.getMethod().equals("PUT")) {
                ChangePasswordService.initChangePassword(request, response);
            } else if (relativeURI.startsWith("/pass/change/update") && request.getMethod().equals("POST")) {
                ChangePasswordService.updateChangePassword(request, response, relativeURI);
            } else if (relativeURI.startsWith("/pass/change/submit") && request.getMethod().equals("PUT")) {
                ChangePasswordService.submitChangePassword(request, response, relativeURI);
            } else if (relativeURI.startsWith("/pass/reset/init") && request.getMethod().equals("PUT")) {
                ResetPasswordService.initResetPassword(request, response);
            } else if (relativeURI.startsWith("/pass/reset/update") && request.getMethod().equals("POST")) {
                ResetPasswordService.updateResetPassword(request, response, relativeURI);
            } else if (relativeURI.startsWith("/pass/reset/check") && request.getMethod().equals("PUT")) {
                ResetPasswordService.checkResetPassword(request, response, relativeURI);
            } else if (relativeURI.startsWith("/pass/reset/submit") && request.getMethod().equals("PUT")) {
                ResetPasswordService.submitResetPassword(request, response, relativeURI);
            } else if(relativeURI.startsWith("/pass/find") && request.getMethod().equals("GET")){
                FindPasswordService.findUserPassword(request, response);
            } else if (relativeURI.startsWith("/") && request.getMethod().equals("DELETE")) {
                DeleteUserService.deleteUser(request, response, relativeURI);
            } else {
                MessageUtils.setError(request, response,
                        404, "Service wasn't found", log);
            }

        } catch (Exception e) {
            log.error("Unknown error during user management processing", e);
            MessageUtils.setError(request, response,
                    500, "Unknown internal error", log);
        }
    }
}