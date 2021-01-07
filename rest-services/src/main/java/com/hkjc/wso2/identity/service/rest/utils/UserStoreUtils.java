package com.hkjc.wso2.identity.service.rest.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.Claim;

import com.hkjc.wso2.identity.service.rest.internal.RESTServiceComponent;

/**
 * Collection of utils for working with user storage
 */
public class UserStoreUtils extends AbstractAdmin {

    public static class UserStoreConnectionException extends Exception {
        UserStoreConnectionException(String message) {
            super(message);
        }
    }

    ;


    /**
     * Obtaining of UserStoreManager instance by static realm service
     *
     * @return the UserStoreManager instance which provide access to user management functionality
     * @throws UserStoreConnectionException if there are some errors with UserStoreManager
     */
    private static UserStoreManager getUserStoreManager() throws UserStoreConnectionException {
        try {
            return (UserStoreManager) RESTServiceComponent.getRealmService().getTenantUserRealm(-1234).getUserStoreManager();
        } catch (UserStoreException e) {
            throw new UserStoreConnectionException(e.getMessage());
        }
    }


    /**
     * Add user to main user storage
     *
     * @param userName   identifier of user
     * @param credential should contain a password
     * @throws UserStoreException           if there are problem with user creation
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void addUser(String userName, String credential)
            throws UserStoreConnectionException, UserStoreException {
        getUserStoreManager().addUser(userName, credential,
                null, null, "default", true);
    }


    /**
     * Add user to main user storage
     *
     * @param userName identifier of user
     * @throws UserStoreException           if there are problem with user removing
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void deleteUser(String userName) throws UserStoreConnectionException, UserStoreException {
        getUserStoreManager().deleteUser(userName);
    }

    /**
     * Add user to main user storage
     *
     * @param userName identifier of user
     * @throws UserStoreException           if there are problem with user removing
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void resetPassword(String userName, String newPassword) throws UserStoreConnectionException, UserStoreException {
        getUserStoreManager().updateCredentialByAdmin(userName,newPassword);
    }

    /**
     * Add user to main user storage
     *
     * @param userName identifier of user
     * @throws UserStoreException           if there are problem with user removing
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static boolean doAuthenticate(String userName, String password) throws UserStoreConnectionException, UserStoreException {
        return getUserStoreManager().authenticate(userName,password);
    }

    /**
     * Add user to main user storage
     *
     * @param userName identifier of user
     * @throws UserStoreException           if there are problem with user removing
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void changePassword(String userName, String newPassword, String oldPassword) throws UserStoreConnectionException, UserStoreException {
        getUserStoreManager().updateCredential(userName, newPassword, oldPassword);
    }


    /**
     * Search of user by any attribute
     *
     * @param claim is ID of attribute
     * @param value is searched value
     * @return list of usernames
     * @throws UserStoreException           if there are problem user search
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static String[] findUsers(String claim, String value)
            throws UserStoreException, UserStoreConnectionException {
        return getUserStoreManager().getUserList(claim, value, "default");
    }


    /**
     * Obtaining of all user information by username
     *
     * @param useranme should contain identifier of user
     * @return list of claims
     * @throws UserStoreException           if there are problem user search
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static Claim[] getUser(String useranme)
            throws UserStoreException, UserStoreConnectionException {
        return getUserStoreManager().getUserClaimValues(useranme, "default");
    }

    /**
     * Obtaining of all user information by username
     *
     * @param useranme should contain identifier of user
     * @return list of claims
     * @throws UserStoreException           if there are problem user search
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static Map<String, String> getUserClaims(String useranme)
            throws UserStoreException, UserStoreConnectionException {
        Claim[] claims = getUserStoreManager().getUserClaimValues(useranme, "default");
        Map<String, String> res = new HashMap<>();
        for (Claim claim : claims) {
            res.put(claim.getClaimUri(), claim.getValue());
        }
        return res;
    }

    /**
     * Obtaining of all user information by username
     *
     * @param useranme should contain identifier of user
     * @return list of claims
     * @throws UserStoreException           if there are problem user search
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static boolean authenticate(String useranme, String password)
            throws UserStoreException, UserStoreConnectionException {
        return getUserStoreManager().authenticate(useranme, password);
    }


    /**
     * Obtaining of free user identifier
     *
     * @return free user identifier
     * @throws UserStoreException           if there is impossible to find free id
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static String getFreeID()
            throws UserStoreException, UserStoreConnectionException {
        UserStoreManager usm = getUserStoreManager();
        for (int i = 0; i<3; i++){
            UUID uuid = UUID.randomUUID();
            String newName = uuid.toString().replaceAll("-", "");
            if(!usm.isExistingUser(newName)){
                return newName;
            }
        }
        throw new UserStoreException("Store is near to overload or this man just is a lucker");
    }


    /**
     * Getting of attribute value by attribute ID.
     *
     * @param userName identifier of user
     * @param claim    is identifier of user's attribute
     * @return value of requested attribute
     * @throws UserStoreException           if there are problems with claim search
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static String getUserClaim(String userName, String claim)
            throws UserStoreException, UserStoreConnectionException {
        return getUserStoreManager().getUserClaimValue(userName, claim, null);
    }

    /**
     * Getting of attributes value by attribute ID array.
     *
     * @param userName identifier of user
     * @param claims   is array with identifiers of user's attribute
     * @return map vith {ClaimUri,ClaimValue}
     * @throws UserStoreException           if there are problems with claim search
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static Map<String, String> getUserClaims(String userName, String[] claims)
            throws UserStoreException, UserStoreConnectionException {
        return getUserStoreManager().getUserClaimValues(userName, claims, null);
    }


    /**
     * Attribute setup (update)
     *
     * @param userName identifier of user
     * @param claim    is identifier of user's attribute
     * @param value    is value to fill
     * @throws UserStoreException           if there are problems with claim updating
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void setUserClaim(String userName, String claim, String value)
            throws UserStoreException, UserStoreConnectionException {
        try {
            getUserStoreManager().setUserClaimValue(userName, claim, value, null);
        } catch (UserStoreException e) {
            throw new UserStoreException("Follow value is incompatible for accorded attribute: " + value, e);
        }
    }

    /**
     * Attributes setup (update)
     *
     * @param userName identifier of user
     * @param claims   is map vith {ClaimUri,ClaimValue}
     * @throws UserStoreException           if there are problems with claim updating
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void setUserClaims(String userName, Map<String, String> claims)
            throws UserStoreException, UserStoreConnectionException {
        getUserStoreManager().setUserClaimValues(userName, claims, null);
    }


    /**
     * Changing of attribute value by attribute ID.
     *
     * @param userName identifier of user
     * @param claim    is identifier of user's attribute
     * @param value    is value to fill
     * @return previous value if it was changed
     * @throws UserStoreException           if there are problems with claim updating
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static String changeUserClaim(String userName, String claim, String value)
            throws UserStoreException, UserStoreConnectionException {
        String prevValue = getUserStoreManager().getUserClaimValue(userName, claim, null);
        if (!value.equals(prevValue)) {
            getUserStoreManager().setUserClaimValue(userName, claim, value, null);
            if (prevValue == null) {
                prevValue = "null";
            }
            return prevValue;
        }
        return null;
    }

    /**
     * Changing of user attribute value by attribute ID.
     *
     * @param userName identifier of user
     * @param claims   is array with identifiers of user's attribute
     * @return array of previous values if them were changed
     * @throws UserStoreException           if there are problems with claim updating
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static Map<String, String> changeUserClaims(String userName, Map<String, String> claims)
            throws UserStoreException, UserStoreConnectionException {
        Object[] claimsToGetObj = claims.keySet().toArray();
        String[] claimsToGet = Arrays.copyOf(claimsToGetObj, claimsToGetObj.length, String[].class);
        Map<String, String> currentValues = getUserStoreManager().
                getUserClaimValues(userName, claimsToGet, null);
        Map<String, String> changeMap = new HashMap<String, String>();
        Map<String, String> historyMap = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : claims.entrySet()) {
            String claim = entry.getKey();
            String newValue = entry.getValue();
            String oldValue = currentValues.get(claim);
            if (oldValue == null) {
                oldValue = "";
            }
            if (!newValue.equals(oldValue)) {
                changeMap.put(claim, newValue);
                historyMap.put(claim, oldValue);
            }
        }
        if (changeMap.size() > 0) {
            getUserStoreManager().setUserClaimValues(userName, changeMap, null);
        }
        return historyMap;
    }

    /**
     * Cicking of existing of role
     *
     * @param role Name of role
     * @return result of checking
     * @throws UserStoreException           if there are problems with role reading
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static boolean isExistingRole(String role)
            throws UserStoreException, UserStoreConnectionException {
        return getUserStoreManager().isExistingRole(role);
    }

    /**
     * Update of user's roles
     *
     * @param userName    identifier of user
     * @param deleteRoles list of roles for deleting (can be null)
     * @param newRoles    list of roles for adding (can be null)
     * @throws UserStoreException           if there are problems with role reading
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void updateRoles(String userName, List<String> deleteRoles, List<String> newRoles)
            throws UserStoreException, UserStoreConnectionException {
        List<String> currentRoles = Arrays.asList(getUserStoreManager().getRoleListOfUser(userName));
        Iterator<String> i = newRoles.iterator();
        while (i.hasNext()) {
            if (currentRoles.contains(i.next())) {
                i.remove();
            }
        }
        i = deleteRoles.iterator();
        while (i.hasNext()) {
            if (!currentRoles.contains(i.next())) {
                i.remove();
            }
        }
        getUserStoreManager().updateRoleListOfUser(userName, deleteRoles.toArray(new String[0]), newRoles.toArray(new String[0]));
    }

    /**
     * Greate Role for User
     *
     * @param role roleName
     * @throws UserStoreException           if there are problems with role creating
     * @throws UserStoreConnectionException if there are problem with storage connection
     */
    public static void addRole(String role) throws UserStoreException, UserStoreConnectionException {
        getUserStoreManager().addRole(role, new String[]{"admin"}, null);
    }

    /**
     * Checking that groups are existing
     *
     * @param groupList contains names without suffix
     * @param suffix    a group suffix (can be null)
     * @return names of groups if they aren't exist
     * @throws UserStoreException                          if there are some problems with group search
     * @throws UserStoreUtils.UserStoreConnectionException if there are problem with storage connection
     */
    public static List<String> areExistingRoles(List<String> groupList, String suffix) throws UserStoreException, UserStoreUtils.UserStoreConnectionException {
        List<String> result = new ArrayList<>();
        if (groupList != null) {
            for (String group : groupList) {
                String groupName = group;
                if (suffix != null) {
                    groupName += suffix;
                }
                if (!isExistingRole(groupName)) {
                    result.add(groupName);
                }
            }
        }
        return result;
    }
}
