package vn.vpbank.wso2.identity.service.rest.utils;

import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;

import java.sql.*;

/**
 * Collection of utils for working with common storages
 * (APIMGT_DB, REGISTRY_DB, USERMGT_DB)
 */
public class IdentityWorkStoreUtils {

    private static final String GET_APPLICATION_CALLBACK_URI =
            "SELECT CALLBACK_URL FROM IDN_OAUTH_CONSUMER_APPS where APP_NAME=?";

    private static final String GET_APPLICATION_STATE =
            "SELECT APP_STATE FROM IDN_OAUTH_CONSUMER_APPS where APP_NAME=?";

    private static final String GET_IS_CHANGED_PASSWORD =
            "SELECT IS_CHANGED, USER_ID FROM IDN_USER_PASS_CHANGES where REQUEST_ID=?";

    private static final String INIT_USER_CHANGE_PASSWORD =
            "INSERT INTO IDN_USER_PASS_CHANGES (REQUEST_ID, USER_ID) values (?, ?)";

    private static final String SUBMIT_USER_CHANGE_PASSWORD =
            "UPDATE IDN_USER_PASS_CHANGES SET IS_CHANGED = 1 where REQUEST_ID=?";

    /**
     * Get a callback URL by application name. The Callback can be not installed
     *
     * @param appName is name of application
     * @return URL string or null
     * @throws SQLException if there are problems with DB interaction
     */
    public static String getCallbackURL(String appName) throws SQLException, IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement storeAppPrepStmt = null;
        ResultSet results = null;
        String callbackURL = null;
        try {
            storeAppPrepStmt = connection.prepareStatement(GET_APPLICATION_CALLBACK_URI);
            storeAppPrepStmt.setString(1, appName);
            results = storeAppPrepStmt.executeQuery();
            if (results.next()) {
                callbackURL = results.getString(1);
            }
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(results);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
        }
        return callbackURL;
    }

    /**
     * Checking of Application is existing and in active state
     *
     * @param appName is name of application
     * @return result of checking
     * @throws SQLException if there are problems with DB interaction
     */
    public static boolean isActiveApp(String appName) throws SQLException, IdentityException {
        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement storeAppPrepStmt = null;
        ResultSet results = null;
        boolean result = false;
        try {
            storeAppPrepStmt = connection.prepareStatement(GET_APPLICATION_STATE);
            storeAppPrepStmt.setString(1, appName);
            results = storeAppPrepStmt.executeQuery();
            while (results.next()) {
                if ("ACTIVE".equals(results.getString(1))) {
                    result = true;
                    break;
                }
            }
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(results);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
        }
        return result;
    }

    public static Boolean checkChangePassword(String requestId) throws SQLException, IdentityException {
        boolean result = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            PreparedStatement storeAppPrepStmt = connection.prepareStatement(GET_IS_CHANGED_PASSWORD);
            storeAppPrepStmt.setString(1, requestId);
            ResultSet results = storeAppPrepStmt.executeQuery();
            if (results == null) {
                return null;
            }
            while (results.next()) {
                if ("1".equals(results.getString(1))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static String getUserChangePassword(String requestId) throws SQLException, IdentityException {
        String userId = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            PreparedStatement storeAppPrepStmt = connection.prepareStatement(GET_IS_CHANGED_PASSWORD);
            storeAppPrepStmt.setString(1, requestId);
            ResultSet results = storeAppPrepStmt.executeQuery();
            while (results.next()) {
                userId = results.getString(2);
            }
        }
        return userId;
    }

    public static void initUserChangePassword(String requestId, String userId) throws SQLException, IdentityException {
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            PreparedStatement storeAppPrepStmt = connection.prepareStatement(INIT_USER_CHANGE_PASSWORD);
            storeAppPrepStmt.setString(1, requestId);
            storeAppPrepStmt.setString(2, userId);
            storeAppPrepStmt.executeUpdate();
            connection.commit();
        }
    }

    public static void submitUserChangePassword(String requestId) throws SQLException, IdentityException {
        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {
            PreparedStatement storeAppPrepStmt = connection.prepareStatement(SUBMIT_USER_CHANGE_PASSWORD);
            storeAppPrepStmt.setString(1, requestId);
            storeAppPrepStmt.executeUpdate();
            connection.commit();
        }
    }
}
