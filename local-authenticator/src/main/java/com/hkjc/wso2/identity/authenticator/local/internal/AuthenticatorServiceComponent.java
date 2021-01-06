package com.hkjc.wso2.identity.authenticator.local.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.IdentityMgtConfig;
import org.wso2.carbon.identity.mgt.RecoveryProcessor;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.ChallengeQuestionDTO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import com.hkjc.wso2.identity.authenticator.local.ExtendBasicAuthenticator;

/**
 * @scr.component name="vn.vpbank.wso2.identity.authenticator.local" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class AuthenticatorServiceComponent {

    public static class DeviceOTPConfiguration {
        private int capacity;
        private int maxAttempts;
        private String mobileClaim;
        private String deviceClaim;
        private String createConsentURL;
        private String checkConsentURL;
        private String sendSMSURL;
        private boolean isOptionsInitiated = false;

        public int getCapacity(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return capacity;
        }

        public int getMaxAttempts(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return maxAttempts;
        }

        public String getMobileClaim(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return mobileClaim;
        }

        public String getDeviceClaim(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return deviceClaim;
        }

        public String getCreateConsentURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return createConsentURL;
        }

        public String getCheckConsentURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return checkConsentURL;
        }

        public String getSendSMSURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return sendSMSURL;
        }

        private void initiateOptions(){
            APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) {
                AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
                AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                        authenticatorsConfiguration.getAuthenticatorConfig("DeviceOTP");
                capacity = Integer.parseInt(authenticatorConfig.getParameters().get("capacity"));
                mobileClaim = authenticatorConfig.getParameters().get("mobileClaim");
                deviceClaim = authenticatorConfig.getParameters().get("deviceClaim");
                createConsentURL = authenticatorConfig.getParameters().get("createConsentURL");
                checkConsentURL = authenticatorConfig.getParameters().get("checkConsentURL");
                sendSMSURL = authenticatorConfig.getParameters().get("sendSMSURL");
                maxAttempts = Integer.parseInt(authenticatorConfig.getParameters().get("maxAttempts"));
                isOptionsInitiated = true;
            } else {
                log.debug("Configuration of DeviceOTP authenticator couldn't be read successfully.");
            }
        }

    }
    public static class ChangePasswordCheckConfiguration {
        private String url;
        private String chpassClaim;
        private int maxAttempts;
        private boolean isOptionsInitiated = false;

        public String getURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return url;
        }

        public int getMaxAttempts(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return maxAttempts;
        }

        public String getChpassClaim(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return chpassClaim;
        }

        private void initiateOptions(){
            APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) {
                AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
                AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                        authenticatorsConfiguration.getAuthenticatorConfig("ChangePasswordCheck");
                url = authenticatorConfig.getParameters().get("url");
                chpassClaim = authenticatorConfig.getParameters().get("chpassClaim");
                maxAttempts = Integer.parseInt(authenticatorConfig.getParameters().get("maxAttempts"));
                isOptionsInitiated = true;
            } else {
                log.debug("Configuration of ChangePasswordCheck authenticator couldn't be read successfully.");
            }
        }

    }
    public static class SetupSecretQuestionsConfiguration {
        private String url;
        private int maxAttempts;
        private boolean isOptionsInitiated = false;
        private static RecoveryProcessor recoveryProcessor;

        public String getURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return url;
        }

        public int getMaxAttempts(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return maxAttempts;
        }

        public static RecoveryProcessor getRecoveryProcessor() {
            return recoveryProcessor;
        }

        private void initiateOptions(){
            APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) {
                AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
                AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                        authenticatorsConfiguration.getAuthenticatorConfig("SetupSecretQuestions");
                url = authenticatorConfig.getParameters().get("url");
                maxAttempts = Integer.parseInt(authenticatorConfig.getParameters().get("maxAttempts"));
                isOptionsInitiated = true;
            } else {
                log.debug("Configuration of SetupSecretQuestions authenticator couldn't be read successfully.");
            }
        }

        private static void initRecovery() {

            Registry registry;
            IdentityMgtConfig.getInstance(realmService.getBootstrapRealmConfiguration());
            recoveryProcessor = new RecoveryProcessor();
            try {
                registry = AuthenticatorServiceComponent.getRegistryService()
                        .getConfigSystemRegistry();
                if (!registry
                        .resourceExists(IdentityMgtConstants.IDENTITY_MANAGEMENT_PATH)) {
                    Collection questionCollection = registry.newCollection();
                    registry.put(IdentityMgtConstants.IDENTITY_MANAGEMENT_PATH,
                            questionCollection);
                    loadDefaultChallenges();
                }
            } catch (RegistryException e) {
                log.error("Error while creating registry collection for org.wso2.carbon.identity.mgt component", e);
            }

        }

        private static void loadDefaultChallenges() {

            List<ChallengeQuestionDTO> questionSetDTOs = new ArrayList<ChallengeQuestionDTO>();

            for (String challenge : IdentityMgtConstants.getSecretQuestionsSet01()) {
                ChallengeQuestionDTO dto = new ChallengeQuestionDTO();
                dto.setQuestion(challenge);
                dto.setPromoteQuestion(true);
                dto.setQuestionSetId(IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI01);
                questionSetDTOs.add(dto);
            }

            for (String challenge : IdentityMgtConstants.getSecretQuestionsSet02()) {
                ChallengeQuestionDTO dto = new ChallengeQuestionDTO();
                dto.setQuestion(challenge);
                dto.setPromoteQuestion(true);
                dto.setQuestionSetId(IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI02);
                questionSetDTOs.add(dto);
            }

            try {
                recoveryProcessor.getQuestionProcessor().setChallengeQuestions(questionSetDTOs.
                        toArray(new ChallengeQuestionDTO[questionSetDTOs.size()]));
            } catch (IdentityException e) {
                log.error("Error while promoting default challenge questions", e);
            }

        }

    }
    public static class TermAndConditionsConfiguration {
        private String url;
        private int maxAttempts;
        private String termsClaim;
        private boolean isOptionsInitiated = false;

        public String getTermsClaim(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return termsClaim;
        }

        public String getURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return url;
        }

        public int getMaxAttempts(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return maxAttempts;
        }

        private void initiateOptions(){
            APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) {
                AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
                AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                        authenticatorsConfiguration.getAuthenticatorConfig("TermAndConditions");
                url = authenticatorConfig.getParameters().get("url");
                maxAttempts = Integer.parseInt(authenticatorConfig.getParameters().get("maxAttempts"));
                termsClaim = authenticatorConfig.getParameters().get("termClaims");
                isOptionsInitiated = true;
            } else {
                log.debug("Configuration of SetupSecretQuestions authenticator couldn't be read successfully.");
            }
        }

    }
    public static class ExtendedBasicConfiguration {
        private String url;
        private String userNameClaim;
        private int maxAttempts;
        private boolean isOptionsInitiated = false;

        public String getUserNameClaim(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return userNameClaim;
        }

        public String getURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return url;
        }

        public int getMaxAttempts(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return maxAttempts;
        }

        private void initiateOptions(){
            APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) {
                AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
                AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                        authenticatorsConfiguration.getAuthenticatorConfig("ExtendedBasic");
                url = authenticatorConfig.getParameters().get("url");
                maxAttempts = Integer.parseInt(authenticatorConfig.getParameters().get("maxAttempts"));
                userNameClaim = authenticatorConfig.getParameters().get("userNameClaim");
                isOptionsInitiated = true;
            } else {
                log.debug("Configuration of ExtendedBasic authenticator couldn't be read successfully.");
            }
        }

    }
    public static class TokenConfiguration {
        private String pincodeUrl;
        private String userNameClaim;
        private int maxAttempts;
        private String tokenServiceEndpoint;
        private boolean isOptionsInitiated = false;

        public String getUserNameClaim(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return userNameClaim;
        }

        public String getPincodeURL(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return pincodeUrl;
        }

        public int getMaxAttempts(){
            if (!isOptionsInitiated){
                initiateOptions();
            }
            return maxAttempts;
        }

        public String getTokenServiceEndpoint(){
            if(!isOptionsInitiated){
                initiateOptions();
            }
            return tokenServiceEndpoint;
        }
        private void initiateOptions(){
            APIManagerConfigurationService configurationService = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
            if (configurationService != null) { //TODO check if it is required
                AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
                AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                        authenticatorsConfiguration.getAuthenticatorConfig("TokenAuthenticator");
                  pincodeUrl = authenticatorConfig.getParameters().get("pincodeUrl");
                  maxAttempts = Integer.parseInt(authenticatorConfig.getParameters().get("maxAttempts"));
                  userNameClaim = authenticatorConfig.getParameters().get("userNameClaim");
                  tokenServiceEndpoint=authenticatorConfig.getParameters().get("tokenServiceEndpoint");
                  isOptionsInitiated = true;
            } else {
                log.debug("Configuration of TokenLocalAuthenticator authenticator couldn't be read successfully.");
            }
        }

    }

    private static Log log = LogFactory.getLog(AuthenticatorServiceComponent.class);
    private static RealmService realmService;
    private static RegistryService registryService;
    public static DeviceOTPConfiguration deviceOTPOptions = new DeviceOTPConfiguration();
    public static ChangePasswordCheckConfiguration changePasswordCheckOptions = new ChangePasswordCheckConfiguration();
    public static SetupSecretQuestionsConfiguration setupSecretQuestionsOptions = new SetupSecretQuestionsConfiguration();
    public static TermAndConditionsConfiguration termsOptions = new TermAndConditionsConfiguration();
    public static ExtendedBasicConfiguration basicOptions = new ExtendedBasicConfiguration();
    public static TokenConfiguration tokenOption = new TokenConfiguration();

    protected void activate(ComponentContext ctxt) {
        SetupSecretQuestionsConfiguration.initRecovery();

        try {
            ExtendBasicAuthenticator basicAuth = new ExtendBasicAuthenticator();
            ctxt.getBundleContext().registerService(ApplicationAuthenticator.class.getName(), basicAuth, null);
            log.info("ExtendBasicAuthenticator are activated");
        } catch (Throwable e) {
            log.error("ExtendBasicAuthenticator activation Failed", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Authenticators bundle is deactivated");
    }

    protected void unsetRealmService(RealmService realmService) {
        log.debug("UnSetting the Realm Service");
        AuthenticatorServiceComponent.realmService = null;
    }

    protected void setRealmService(RealmService realmService) {
        log.debug("Setting the Realm Service");
        AuthenticatorServiceComponent.realmService = realmService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    protected void setRegistryService(RegistryService registryService) {
        log.debug("Setting the Registry Service");
        AuthenticatorServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        log.debug("UnSetting the Registry Service");
        AuthenticatorServiceComponent.registryService = null;
    }

}
