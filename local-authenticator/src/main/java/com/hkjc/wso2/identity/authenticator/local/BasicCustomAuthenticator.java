package com.hkjc.wso2.identity.authenticator.local;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AbstractApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.user.core.UserStoreException;

import com.hkjc.wso2.identity.authenticator.local.utils.PasswordUtils;
import com.hkjc.wso2.identity.authenticator.local.utils.UserStoreUtils;

/**
 * Username Password based custom Authenticator
 */
public class BasicCustomAuthenticator extends AbstractApplicationAuthenticator
		implements LocalApplicationAuthenticator {

	private static final long serialVersionUID = 4345354156955223654L;
	private static final Log log = LogFactory.getLog(BasicCustomAuthenticator.class);

	@Override
	protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException {

		String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

		String queryParams = FrameworkUtils.getQueryStringWithFrameworkContextId(context.getQueryParams(),
				context.getCallerSessionKey(), context.getContextIdentifier());

		try {
			String retryParam = "";

			if (context.isRetrying()) {
				retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
			}

			response.sendRedirect(response.encodeRedirectURL(loginPage + ("?" + queryParams))
					+ "&authenticators=BasicAuthenticator:" + "LOCAL" + retryParam);
		} catch (IOException e) {
			throw new AuthenticationFailedException(e.getMessage(), e);
		}
	}


	@Override
	protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
			AuthenticationContext context) throws AuthenticationFailedException {

		String usernameWithDomain = request.getParameter(BasicCustomAuthenticatorConstants.USER_NAME);

		String username = usernameWithDomain.split("@")[0];

		String password = request.getParameter(BasicCustomAuthenticatorConstants.PASSWORD);
		String sha1password = PasswordUtils.sha1(password);

		boolean isAuthenticated = true;
		context.setSubject(AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(username));
		boolean authorization = false;

		if (isAuthenticated) {
			if ("oidc".equalsIgnoreCase(context.getRequestType())) {

				try {
					if ("admin".equalsIgnoreCase(username)) {
						authorization = UserStoreUtils.authenticate(username, password);
					} else {
						authorization = UserStoreUtils.authenticate(username, sha1password);
					}

				} catch (UserStoreException e) {
					log.error(e);
				} catch (org.wso2.carbon.user.api.UserStoreException e) {
					log.error(e);
				}
			} else {
				// others scenarios are not verified.
				authorization = false;
			}

			if (!authorization) {
				log.error("user authorization is failed.");

				throw new InvalidCredentialsException("User authentication failed due to invalid credentials",
						User.getUserFromUserName(username));

			}
			else {
				log.info(username + " authorization is success.");
			}
		}
	}

	@Override
	protected boolean retryAuthenticationEnabled() {
		return true;
	}

	@Override
	public String getFriendlyName() {
		// Set the name to be displayed in local authenticator drop down lsit
		return BasicCustomAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
	}

	@Override
	public boolean canHandle(HttpServletRequest httpServletRequest) {
		String userName = httpServletRequest.getParameter(BasicCustomAuthenticatorConstants.USER_NAME);
		String password = httpServletRequest.getParameter(BasicCustomAuthenticatorConstants.PASSWORD);
		if (userName != null && password != null) {
			return true;
		}
		return false;
	}

	@Override
	public String getContextIdentifier(HttpServletRequest httpServletRequest) {
		return httpServletRequest.getParameter("sessionDataKey");
	}

	@Override
	public String getName() {
		return BasicCustomAuthenticatorConstants.AUTHENTICATOR_NAME;
	}
}
