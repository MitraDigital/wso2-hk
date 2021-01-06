/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.hkjc.wso2.identity.authenticator.local;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.user.api.UserStoreException;

import com.hkjc.wso2.identity.authenticator.local.internal.AuthenticatorServiceComponent;
import com.hkjc.wso2.identity.authenticator.local.utils.CommonUtils;
import com.hkjc.wso2.identity.authenticator.local.utils.UserStoreUtils;


/**
 * DeviceOTP custom Authenticator
 */
public class ExtendBasicAuthenticator extends AbstractCustomAuthenticator implements LocalApplicationAuthenticator {

    private static final long serialVersionUID = 4345384156955223654L;
    //TODO - insert debug logging
    private static final Log log = LogFactory.getLog(ExtendBasicAuthenticator.class);

    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context,
                                                 AuthenticatedUser user,
                                                 String app)
            throws AuthenticationFailedException {

        String loginPage = AuthenticatorServiceComponent.basicOptions.getURL();
        String debitCard = request.getParameter(ExtendBasicAuthenticatorConstants.LOGIN_TYPE);
        if (debitCard != null) {
            if (debitCard.equalsIgnoreCase(ExtendBasicAuthenticatorConstants.DEBIT_CARD)) {
                loginPage = loginPage.replace(ExtendBasicAuthenticatorConstants.BASIC_LOGIN, ExtendBasicAuthenticatorConstants.DEBIT_CARD_LOGIN);
            }
        }
        StringBuilder queryParams = new StringBuilder();
        String relyingParty = context.getRelyingParty();
        String contextIdentifier = context.getContextIdentifier();
        String state = CommonUtils.getParamFromString(
                context.getContextIdIncludedQueryParams(), "state=", "&");
        try {
            if (StringUtils.isNotEmpty(state)) {
                state = URLDecoder.decode(state, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new AuthenticationFailedException(e.getMessage());
        }
        String lang = request.getParameter("lang");
        if (StringUtils.isEmpty(lang)) {
            lang = CommonUtils.getParamFromString(context.getQueryParams(), "lang=", "&");
        }
        String phoneNumber = request.getParameter("phoneNumber");

        String defUserName = request.getParameter("default_username");
        if (StringUtils.isEmpty(defUserName)) {
            defUserName = request.getParameter("username");
        }

        //building of redirect
        if (StringUtils.isNotEmpty(lang)) {
            queryParams.append("&lang=").append(lang);
        }
        if (StringUtils.isNotEmpty(defUserName)) {
            queryParams.append("&default_username=").append(defUserName);
        }
        if (StringUtils.isNotEmpty(relyingParty)) {
            queryParams.append("&relyingParty=").append(relyingParty);
        }
        if (StringUtils.isNotEmpty(state)) {
            queryParams.append("&state=").append(state);
        }
        if (StringUtils.isNotEmpty(phoneNumber)) {
            queryParams.append("&phoneNumber=").append(phoneNumber);
        }
        queryParams.append("&sessionDataKey=").append(contextIdentifier);
        log.debug("Application value before submitting: " + app);
        queryParams.append("&sp=").append(app);
        log.debug("Retry Count: " + context.getRetryCount());
        if (context.isRetrying()) {
            //queryParams.append("&authFailure=true&authFailureMsg=login.fail.message");
            queryParams.append("&authFailure=true&authFailureMsg=").append((String) (context.getProperty("ExBasicLoginError")));
            queryParams.append("&authFailureAttempts=").append(context.getRetryCount());
            queryParams.append("&authFailureStartDateTime=").append((String) (context.getProperty("attemptStartTime")));
        }
        try {
            queryParams.replace(0, 1, "?");
            response.sendRedirect(response.encodeRedirectURL(loginPage + queryParams));
        } catch (IOException e) {
            throw new AuthenticationFailedException(e.getMessage());
        }
    }

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context,
                                                 AuthenticatedUser user,
                                                 String sp)
            throws AuthenticationFailedException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String openui = request.getParameter("openui");
        String encodedPassword = null;

        //replace " " with "+" in order to avoid AngularJS bug with encoding symbols
        if ("js".equals(openui)) {
            encodedPassword = password.replace(" ", "+");
        }

        log.info(sdkPref + "Try to authenticate ["
                + username + "] at [" + sp + "]");

        boolean isAuthenticated;
        try {

            isAuthenticated = UserStoreUtils.authenticate(username, StringUtils.isEmpty(encodedPassword) ? password : encodedPassword);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("BasicAuthentication failed while trying to authenticate", e);
            }
            if (e.getMessage().contains("PRE_AUTHENTICATION")) {
                log.info(sdkPref + "User authentication failed because locked");
                context.setProperty("ExBasicLoginError", "login.preconditions.fail");
            }
            if(e.getMessage().contains("Account is locked")){
                context.setProperty("ExBasicLoginError", "login.account.locked");
            }
            log.info(sdkPref + "User authentication failed by unknown reasons");
            throw new AuthenticationFailedException(e.getMessage(), User.getUserFromUserName(username), e);
        }

        if (!isAuthenticated) {
            log.info(sdkPref + "User authentication failed by incorrect data");
            context.setProperty("ExBasicLoginError", "login.fail.message");
            throw new AuthenticationFailedException("User authentication failed for " + username);
        } else {
            log.info(sdkPref + "User authentication passed");
            try {
                username = UserStoreUtils.getUserClaim(username, AuthenticatorServiceComponent.basicOptions.getUserNameClaim());
            } catch (UserStoreException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error during username receiving for authenticated user", e);
                }
                throw new AuthenticationFailedException("User authentication failed for " + username);
            }
            context.setSubject(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username));
            String rememberMe = request.getParameter("chkRemember");
            if (rememberMe != null && "on".equals(rememberMe)) {
                context.setRememberMe(true);
            }
        }

    }

    @Override
    protected boolean checkNecessityOfAuthenticator(AuthenticationContext context, User user, String app) throws AuthenticationFailedException {
        return true;
    }

    @Override
    protected boolean checkAttempts(int amount) {
        return amount < AuthenticatorServiceComponent.basicOptions.getMaxAttempts();
    }

    @Override
    protected boolean retryAuthenticationEnabled() {
        return true;
    }

    @Override
    public String getFriendlyName() {
        return ExtendBasicAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public boolean canHandle(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter("username") != null
                && httpServletRequest.getParameter("password") != null;
    }

    @Override
    public String getContextIdentifier(HttpServletRequest httpServletRequest) {
        return httpServletRequest.getParameter("sessionDataKey");
    }

    @Override
    public String getName() {
        return ExtendBasicAuthenticatorConstants.AUTHENTICATOR_NAME;
    }


}