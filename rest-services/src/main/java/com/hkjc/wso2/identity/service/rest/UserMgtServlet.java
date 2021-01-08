/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.hkjc.wso2.identity.service.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import com.hkjc.wso2.identity.service.rest.service.ChangePasswordService;
import com.hkjc.wso2.identity.service.rest.service.GetAuthenticateService;
import com.hkjc.wso2.identity.service.rest.utils.MessageUtils;

/**
 * Implemantation of servlets for custom application management APIs
 */
public class UserMgtServlet extends HttpServlet {

	private static final long serialVersionUID = -7182221722709941646L;
	private static Log log = LogFactory.getLog(UserMgtServlet.class);

	/**
	 * Standard init function
	 */
	@Override
	public void init() {
		ConfigurationFacade.getInstance();
	}

	/**
	 * Get method valve. For a convenience the routing of all requests is processed
	 * at doPost
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Dlete method valve. For a convenience the routing of all requests is
	 * processed at doPost
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Put method valve. For a convenience the routing of all requests is processed
	 * at doPost
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (FrameworkUtils.getMaxInactiveInterval() == 0) {
			FrameworkUtils.setMaxInactiveInterval(request.getSession().getMaxInactiveInterval());
		}
		try {
			log.debug(request.getRequestURI());
			String relativeURI = request.getRequestURI().substring(request.getContextPath().length());

			// the main request mapping
			if (relativeURI.startsWith("/authenticate") && request.getMethod().equals("POST")) {
				GetAuthenticateService.getAuthenticate(request, response, relativeURI);
			} else if (relativeURI.startsWith("/credentials") && request.getMethod().equals("PUT")) {
				ChangePasswordService.changePassword(request, response, relativeURI);

			} else {
				MessageUtils.setError(response, 404, "EER00404", "Service not found", log);
			}

		} catch (Exception e) {
			log.error("Unknown error during user management processing", e);
			MessageUtils.setError(response, 500, "EER00500", "Internal server error", log);
		}
	}
}