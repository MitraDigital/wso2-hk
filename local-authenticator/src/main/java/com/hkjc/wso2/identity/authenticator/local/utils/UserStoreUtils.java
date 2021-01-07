package com.hkjc.wso2.identity.authenticator.local.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import com.hkjc.wso2.identity.authenticator.local.internal.BasicCustomAuthenticatorServiceComponent;



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

	public static int getUserId(String username) throws UserStoreException {
		int value;
		try {
			value = getUserStoreManager().getUserId(username);
		} catch (UserStoreException e) {
			throw new UserStoreException("Error occurred while loading user id", e);
		}
		return value;
	}

	public static String[] getRoleListOfUser(String role) throws UserStoreException {
		String[] value;
		try {
			value = getUserStoreManager().getRoleListOfUser(role);
		} catch (UserStoreException e) {
			throw new UserStoreException("Error occurred while loading user id", e);
		}
		return value;
	}

	public static String[] getRoleNames() throws UserStoreException {
		String[] value;
		try {
			value = getUserStoreManager().getRoleNames();
		} catch (UserStoreException e) {
			throw new UserStoreException("Error occurred while loading user id", e);
		}
		return value;
	}

	public static String[] listUsers(String filter, int maxItemLimit) throws UserStoreException {
		String[] value;
		try {
			value = getUserStoreManager().listUsers(filter, maxItemLimit);
		} catch (UserStoreException e) {
			throw new UserStoreException("Error occurred while loading user id", e);
		}
		return value;
	}

	public static boolean isExistingUser(String username) throws UserStoreException {
		boolean value;
		try {
			value = getUserStoreManager().isExistingUser(username);
		} catch (UserStoreException e) {
			throw new UserStoreException("Error occurred while loading username", e);
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






    /**
     * Obtaining of UserStoreManager instance by static realm service
     *
     * @return the UserStoreManager instance which provide access to user management functionality
     * @throws UserStoreException if there are some errors with UserStoreManager
     */
    private static UserStoreManager getUserStoreManager() throws UserStoreException {

		UserStoreManager userStoreManager = (UserStoreManager) BasicCustomAuthenticatorServiceComponent
				.getRealmService().getTenantUserRealm(-1234).getUserStoreManager();


		return userStoreManager;

    }
}
