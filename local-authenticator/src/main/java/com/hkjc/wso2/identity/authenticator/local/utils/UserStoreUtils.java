package com.hkjc.wso2.identity.authenticator.local.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.ChallengeQuestionProcessor;
import org.wso2.carbon.identity.mgt.constants.IdentityMgtConstants;
import org.wso2.carbon.identity.mgt.dto.UserChallengesDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import com.hkjc.wso2.identity.authenticator.local.internal.AuthenticatorServiceComponent;

public class UserStoreUtils {

    public static String getUserClaim(User user, String claim) throws UserStoreException {
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(user.getUserName());
        return getUserClaim(tenantAwareUsername, claim);
    }

    public static String getUserClaim(String username, String claim) throws UserStoreException {
        String value;
        try {
            value = getUserStoreManager().getUserClaimValue(username, claim, null);
        } catch (UserStoreException e) {
            throw new UserStoreException( "Error occurred while loading user claim", e);
        }
        return value;
    }

    public static void setUserClaim(User user, String claim, String value) throws UserStoreException {
        try {
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(user.getUserName());
            getUserStoreManager().setUserClaimValue(tenantAwareUsername, claim, value, null);
        } catch (UserStoreException e) {
            throw new UserStoreException( "Error occurred while setup user claim", e);
        }
    }

    public static void updateCredentials(User user, String currentPassword, String newPassword) throws UserStoreException {
        try {
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(user.getUserName());
            UserStoreManager userStoreManager =  getUserStoreManager();
            String regularExpression = userStoreManager.getRealmConfiguration().getUserStoreProperty("PasswordJavaRegEx");
            if(StringUtils.isNotEmpty(regularExpression)) {
                if(!isFormatCorrect(regularExpression, newPassword)){
                   throw new UserStoreException(
                            "New password doesn't meet the policy requirement. It must be in the following format, "
                                    + regularExpression);
                }
            }
            userStoreManager.updateCredential(tenantAwareUsername, newPassword, currentPassword);
        } catch (UserStoreException e) {
            throw new UserStoreException(e.getMessage());
        }
    }

    public static void updateChallenge(User user, String question, String answer) throws UserStoreException {
        ChallengeQuestionProcessor processor = AuthenticatorServiceComponent.
                SetupSecretQuestionsConfiguration.getRecoveryProcessor().getQuestionProcessor();
        UserChallengesDTO[] questionSetDTOs = {new UserChallengesDTO()};
        questionSetDTOs[0].setId(IdentityMgtConstants.DEFAULT_CHALLENGE_QUESTION_URI01);
        questionSetDTOs[0].setQuestion(question);
        String modifiedAnswer = answer.toLowerCase().replaceAll("[^a-z0-9]","");
        if (modifiedAnswer.length()<1){
            throw new UserStoreException("Bad challenge answer");
        }
        questionSetDTOs[0].setAnswer(modifiedAnswer);
        questionSetDTOs[0].setOrder(0);
        questionSetDTOs[0].setPrimary(true);
        try {
            processor.setChallengesOfUser(user.getUserName(), -1234, questionSetDTOs);
        } catch (IdentityException e) {
            throw new UserStoreException(e.getMessage());
        }
    }

    public static boolean checkChallenge(User user) throws UserStoreException {
        ChallengeQuestionProcessor processor = AuthenticatorServiceComponent.
                SetupSecretQuestionsConfiguration.getRecoveryProcessor().getQuestionProcessor();
        UserChallengesDTO[] questionSetDTOs = null;
        questionSetDTOs = processor.getChallengeQuestionsOfUser( user.getUserName(), -1234, true);
        if (questionSetDTOs == null || questionSetDTOs.length < 1){
            return false;
        }
        return true;
    }

    /**
     * Obtaining of all user information by username
     *
     * @param useranme should contain identifier of user
     * @return list of claims
     * @throws UserStoreException           if there are problem user search
     */
    public static boolean authenticate(String useranme, String password)
            throws UserStoreException {
        return getUserStoreManager().authenticate(useranme, password);
    }

    private static boolean isFormatCorrect(String regularExpression, String password) {
        Pattern p2 = Pattern.compile(regularExpression);
        Matcher m2 = p2.matcher(password);
        return m2.matches();
    }


    public static boolean checkDevice(User user, String deviceID, String app)
            throws UserStoreException {
        if (StringUtils.isNotEmpty(deviceID)) {
            //check devices for current app were registered before
            String registeredDevices = getUserClaim(user, AuthenticatorServiceComponent.deviceOTPOptions.getDeviceClaim());
            if (StringUtils.isNotEmpty(registeredDevices)) {
                String appDevices = CommonUtils.getParamFromString(registeredDevices, app + ":", "!");
                if (StringUtils.isNotEmpty(appDevices)) {
                    //check list of registered devices contain current device
                    List<String> appDevicesList = Arrays.asList(appDevices.split(";"));
                    if (appDevicesList.contains(deviceID)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void registerDevice(User user, String deviceID, String app)
            throws UserStoreException {
        if (StringUtils.isNotEmpty(deviceID)) {
            //check devices for current app were registered before
            String registeredDevices = getUserClaim(user, AuthenticatorServiceComponent.deviceOTPOptions.getDeviceClaim());
            if (StringUtils.isNotEmpty(registeredDevices)) {
                String appDevices = CommonUtils.getParamFromString(registeredDevices, app + ":", "!");
                if (StringUtils.isNotEmpty(appDevices)) {
                    //check list of registered devices contain current device
                    List<String> appDevicesList = new LinkedList<>(Arrays.asList(appDevices.split(";")));
                    appDevicesList.add(deviceID);
                    if (appDevicesList.size() > AuthenticatorServiceComponent.deviceOTPOptions.getCapacity()){
                        appDevicesList.remove(0);
                    }
                    registeredDevices = registeredDevices.replace(
                            app + ":" + appDevices,
                            app + ":" + CommonUtils.listToString(appDevicesList,";"));
                } else {
                    registeredDevices += "!" + app + ":" + deviceID;
                }
            } else {
                registeredDevices = app + ":" + deviceID;
            }
            setUserClaim(user, AuthenticatorServiceComponent.deviceOTPOptions.getDeviceClaim(), registeredDevices);
        }
    }

    /**
     * Obtaining of UserStoreManager instance by static realm service
     *
     * @return the UserStoreManager instance which provide access to user management functionality
     * @throws UserStoreException if there are some errors with UserStoreManager
     */
    private static UserStoreManager getUserStoreManager() throws UserStoreException {
        return (UserStoreManager) AuthenticatorServiceComponent.getRealmService().getTenantUserRealm(-1234).getUserStoreManager();

    }
}
