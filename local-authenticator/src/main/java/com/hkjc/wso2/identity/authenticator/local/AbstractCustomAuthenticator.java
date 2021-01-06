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


import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.User;

import com.hkjc.wso2.identity.authenticator.local.utils.ContextUtils;


abstract class AbstractCustomAuthenticator extends AbstractApplicationAuthenticator implements LocalApplicationAuthenticator {

    private static final Log log = LogFactory.getLog(AbstractCustomAuthenticator.class);

    protected String sdkPref = null;

    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationContext context)
            throws AuthenticationFailedException, LogoutFailedException {
        sdkPref = "[sdk:"+context.getContextIdentifier()+"] ";
        if (log.isDebugEnabled()) {
            log.debug(sdkPref + "Start process of authenticator");
        }
        if (!context.isLogoutRequest()) {
            // if an authentication flow
            context.setCurrentAuthenticator(getName());

//            //check that nobody stolen SassionDataKey
//            // and request goes from the same client
//            if (!ContextUtils.validateContex(context, request)){
//                throw new AuthenticationFailedException ("Unknown client tried to process auth step");
//            }

            // check data from previous steps
            // authenticator can work only when user has been authenticated
            AuthenticatedUser user = ContextUtils.getUser(context);
            String app = ContextUtils.getApp(context);

            //process cookies sequence check
            ContextUtils.linkSession(context,response);

            //detect is it request or response
            if (!canHandle(request)
                || (request.getAttribute(FrameworkConstants.REQ_ATTR_HANDLED) != null && ((Boolean) request
                .getAttribute(FrameworkConstants.REQ_ATTR_HANDLED)))) {
                //this is request
                try{
//                context.setProperty("authenticatedUser", user);
                    if (checkNecessityOfAuthenticator(context, user, app)) {
                        log.info(sdkPref+"request to ["+ context.getCurrentAuthenticator() +"]");
                        initiateAuthenticationRequest(request, response, context, user, app);
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    } else {
                        if (user==null){
                            throw new AuthenticationFailedException ("authentication can not be processed");
                        } else {
                            ContextUtils.updateAuthenticatedUserInStepConfig(context, user);
                        }
                        return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                    }
                }catch(AuthenticationFailedException e){
                    log.info(sdkPref+"["+ context.getCurrentAuthenticator() +"] aborted");
                    if (log.isDebugEnabled()) {
                        log.debug(e.getStackTrace());
                    }
                    throw e;
                }
            } else {
                //this is response
                try {
                    //process response
                    log.info(sdkPref+"response to ["+ context.getCurrentAuthenticator() +"]");
                    processAuthenticationResponse(request, response, context, user, app);
                    request.setAttribute(FrameworkConstants.REQ_ATTR_HANDLED, true);
                    //setup user if he should be defined at current step
                    if (ContextUtils.checkSubjectDefinitionStep(context)){
                        user = context.getSubject();
                        if (!context.getSequenceConfig().getApplicationConfig().isSaaSApp()) {
                            String userDomain = user.getTenantDomain();
                            String tenantDomain = context.getTenantDomain();
                            if (!StringUtils.equals(userDomain, tenantDomain)) {
                                context.setProperty("UserTenantDomainMismatch", true);
                                throw new AuthenticationFailedException("Service Provider tenant domain must be equal to user tenant domain for non-SaaS applications", context.getSubject());
                            }
                        }
                    }
                    //process context
                    ContextUtils.updateAuthenticatedUserInStepConfig(context, user);
                    ContextUtils.resetAttempts(context);
                    log.info(sdkPref+"["+ context.getCurrentAuthenticator() +"] passed");
                    return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
                } catch (AuthenticationFailedException e) {
                    if (retryAuthenticationEnabled() && checkAttempts(ContextUtils.increaseAttempts(context))) {
                        // The Authenticator will re-initiate the authentication and retry.
                        context.setRetrying(true);
                        int retryCount = context.getRetryCount();
                        retryCount++;
                        if(retryCount == 1){
                            context.setProperty("attemptStartTime", String.valueOf(new Date().getTime()));
                        }
                        context.setRetryCount(retryCount);
                        initiateAuthenticationRequest(request, response, context, user, app);
                        log.info(sdkPref+"["+ context.getCurrentAuthenticator() +"] to retry");
                        return AuthenticatorFlowStatus.INCOMPLETE;
                    } else {
                        log.info(sdkPref+"["+ context.getCurrentAuthenticator() +"] aborted");
                        if (log.isDebugEnabled()) {
                            log.debug(e.getStackTrace());
                        }
                        throw e;
                    }
                }
            }
        } else {
            // if a logout flow
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        }
    }

    protected abstract void initiateAuthenticationRequest(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context,
                                                 AuthenticatedUser user,
                                                 String app)
            throws AuthenticationFailedException;

    protected abstract void processAuthenticationResponse(HttpServletRequest request,
                                               HttpServletResponse response,
                                               AuthenticationContext context,
                                               AuthenticatedUser user,
                                               String sp)
            throws AuthenticationFailedException;

    @Override
    protected void processAuthenticationResponse(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 AuthenticationContext context)
            throws AuthenticationFailedException {
        throw new AuthenticationFailedException("Incorrect action");
    }

    protected abstract boolean checkNecessityOfAuthenticator(AuthenticationContext context, User user, String app) throws AuthenticationFailedException;

    protected abstract boolean checkAttempts(int amount);

}