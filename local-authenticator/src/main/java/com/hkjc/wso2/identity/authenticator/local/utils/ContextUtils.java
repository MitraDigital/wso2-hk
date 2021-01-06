package com.hkjc.wso2.identity.authenticator.local.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.ssl.Base64;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;


public class ContextUtils {
    static private final String LINK_COOKIE = "linkCookie";
    static private final String LINK_COOKIE_PARAM_NAME = "sessionCookieLink";
    static private String salt;

    static{
        Random randomGenerator = new Random();
        salt = String.valueOf(10000 + randomGenerator.nextInt(89999));
    }



    static class Attempts{
        int amount = 0;
    }

    public static AuthenticatedUser getUser(AuthenticationContext context) {
        AuthenticatedUser authenticatedUser = null;
        if (!checkSubjectDefinitionStep(context)) {
            Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
            Integer current = context.getCurrentStep();
            for (Map.Entry<Integer, StepConfig> stp:stepMap.entrySet()) {
                if (stp.getKey() < current){
                    if (stp.getValue().isSubjectIdentifierStep()) {
                        authenticatedUser = stp.getValue().getAuthenticatedUser();
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return authenticatedUser;
    }

    public static String getApp(AuthenticationContext context) {
        return context.getServiceProviderName();
    }

    public static boolean checkSubjectDefinitionStep(AuthenticationContext context) {
        Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
        Integer current = context.getCurrentStep();
        if (stepMap.size() > 1) {
            StepConfig stepConfig = stepMap.get(current);
            if (!stepConfig.isSubjectIdentifierStep()) {
                return false;
            }
        }

        return true;
    }

    public static void updateAuthenticatedUserInStepConfig(AuthenticationContext context,
                                                     AuthenticatedUser authenticatedUser) {
        for (int i = 1; i <= context.getSequenceConfig().getStepMap().size(); i++) {
            StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(i);
            stepConfig.setAuthenticatedUser(authenticatedUser);
        }
        context.setSubject(authenticatedUser);
    }

    public static String getDeviceID (AuthenticationContext context) throws UnsupportedEncodingException {
        String scope[] = context.getAuthenticationRequest().getRequestQueryParam("scope");
        String device = null;
        if (scope!=null && scope.length > 0){
            device = CommonUtils.getParamFromString(scope[0],"device_", " ");
        }
        return StringUtils.isEmpty(device) ? null : URLEncoder.encode(device,"UTF-8");
    }

    public static int increaseAttempts (AuthenticationContext context){
        Attempts attempts = (Attempts)context.getProperty("attempts");
        if (attempts == null){
            attempts = new Attempts();
            context.setProperty("attempts",attempts);
        }
        attempts.amount++;
        return attempts.amount;
    }

    public static void resetAttempts (AuthenticationContext context){
        Attempts attempts = (Attempts)context.getProperty("attempts");
        if (attempts != null) {
            attempts.amount = 0;
        }
    }

    public static void linkSession(AuthenticationContext context, HttpServletResponse response) {
        String key = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            key = new String(Base64.encodeBase64(
                    digest.digest((salt + context.getContextIdentifier()).getBytes("UTF-8"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.setProperty(LINK_COOKIE_PARAM_NAME, key);
        Cookie link = new Cookie(LINK_COOKIE, key);
        link.setPath("/");
        response.addCookie(link);
    }

    public static boolean checkSession(AuthenticationContext context, HttpServletRequest request) {
        String key = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (LINK_COOKIE.equals(cookie.getName())) {
                key = cookie.getValue();
                break;
            }
        }
        return key != null && key.equals((String)context.getProperty(LINK_COOKIE_PARAM_NAME));
    }

    public static void declineSession(AuthenticationContext context, HttpServletResponse response) throws AuthenticationFailedException {
        context.setContextIdentifier(null);
        try {
            response.sendError(401);
        } catch (IOException e) {
            throw new AuthenticationFailedException("Error occurs during error answer");
        }
    }

}
