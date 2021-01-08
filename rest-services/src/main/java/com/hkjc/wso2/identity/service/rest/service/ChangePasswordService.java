package com.hkjc.wso2.identity.service.rest.service;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.user.api.UserStoreException;

import com.hkjc.wso2.identity.service.rest.model.web.ChangePasswordDataRq;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;
import com.hkjc.wso2.identity.service.rest.utils.UserStoreUtils;

public class ChangePasswordService {
	private static Log log = LogFactory.getLog(ChangePasswordService.class);

	@SuppressWarnings("unchecked")
	public static void changePassword(HttpServletRequest request, HttpServletResponse response, String uri)
			throws IOException {

		ChangePasswordDataRq req;
		try {
			req = (ChangePasswordDataRq) MessageUtils.parseJSONreq(request, ChangePasswordDataRq.class);
		} catch (Exception e) {
			log.error(e.getMessage());
			MessageUtils.setError(response, 400, "EER00404", "Error during request parsing", log);
			return;
		}

		String username = req.getUsername();
		String newPassword = req.getNewPasswordHash();
		String oldPassword = req.getOldPasswordHash();

		if ("admin".equalsIgnoreCase(username)) {
			MessageUtils.setError(response, 401, "EER00401", "Unauthorized access for the user", log);
		}

		try {
			UserStoreUtils.changePassword(username, newPassword, oldPassword);
		} catch (UserStoreException e) {
			MessageUtils.setError(response, 401, "EER00401", "Password update unsuccessful", log);
			log.error(e.getMessage());
			return;
		} catch (UserStoreUtils.UserStoreConnectionException e) {
			log.error("Something wrong with userstore interaction", e);
			MessageUtils.setError(response, 500, "EER00500", "Password update unsuccessful", log);
			return;
		}
		JSONObject outbody = new JSONObject();

		outbody.put("message", "Password updated successfully");
		MessageUtils.setSuccess(response, outbody, log);

	}

}
