package com.hkjc.wso2.identity.service.rest.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.claim.Claim;

import com.hkjc.wso2.identity.service.rest.model.web.AuthenticationDataRq;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;
import com.hkjc.wso2.identity.service.rest.utils.PwUtils;
import com.hkjc.wso2.identity.service.rest.utils.UserStoreUtils;

public class GetAuthenticateService {
	private static Log log = LogFactory.getLog(GetAuthenticateService.class);

	// TODO - rework to object serialization approach

	/**
	 * The search of users by claim value
	 *
	 * @param request  is input servlet object
	 * @param response is blank for output servlet object
	 * @param uri      is relative URL for getting of path params
	 * @throws IOException if there are some errors with response creation
	 */
	@SuppressWarnings("unchecked")
	public static void getAuthenticate(HttpServletRequest request, HttpServletResponse response, String uri)
			throws IOException {

		AuthenticationDataRq req;
		try {
			req = (AuthenticationDataRq) MessageUtils.parseJSONreq(request, AuthenticationDataRq.class);
		} catch (Exception e) {
			log.error(e.getMessage());
			MessageUtils.setError(response, 400, "EER00404", "Error during request parsing", log);
			return;
		}

		String username = req.getUsername();
		String password = req.getPassword();

		if (!"admin".equalsIgnoreCase(username)) {
			password = PwUtils.sha1(password);
		}

		boolean auth;
		try {
			auth = UserStoreUtils.authenticate(username, password);
		} catch (UserStoreException e) {
			MessageUtils.setError(response, 401, "EER00401", "Unauthorized access for the user", log);
			log.error(e.getMessage());
			return;
		} catch (UserStoreUtils.UserStoreConnectionException e) {
			log.error("Something wrong with userstore interaction", e);
			MessageUtils.setError(response, 500, "EER00500", "Error on claims reading", log);
			return;
		}
		JSONObject outbody = new JSONObject();
		if (auth) {
			Claim[] claims;
			try {
				claims = UserStoreUtils.getUser(username);

			} catch (Exception e) {
				log.error("Something wrong with userstore interaction", e);
				MessageUtils.setError(response, 500, "EER00500", "Error on claims reading", log);
				return;
			}

			for (Claim claim : claims) {
				switch (claim.getClaimUri()) {
				case "http://wso2.org/claims/username":
					outbody.put("webAccountName", claim.getValue());
					break;
				case "http://wso2.org/claims/userid":
					outbody.put("webAccountId", claim.getValue());
					break;
				}
			}

			MessageUtils.setSuccess(response, outbody, log);
		} else {
			MessageUtils.setError(response, 401, "EER00401", "Unauthorized access for the user", log);
			return;
		}

	}
}
