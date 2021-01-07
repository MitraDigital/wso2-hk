# Description

Custom RESTfull services which extend basic IS functionality

# How to deploy

### Jar file location
${IS_HOME}/repository/components/dropins/vn.vpbank.wso2.identity.service.rest-1.0.0.jar

### Required libs 
${IS_HOME}/repository/components/lib/passay-1.3.1.jar  
${IS_HOME}/repository/components/lib/jackson-annotations-2.9.4.jar  
${IS_HOME}/repository/components/lib/jackson-databind-2.9.4.jar  
${IS_HOME}/repository/components/lib/jackson-core-2.9.4.jar  

### Configuration
File: ${IS_HOME}/repository/conf/api-manager.xml  
Path: /APIManager/  
```xml
<RESTServices>
	<AES>
	    <!-- Key is used for en/decripting of password 
	    during sending between systems -->
		<Key>16bytesSecretKey</Key>
	</AES>
	<SMS>
	    <!-- SMSGateway login -->
		<Requestor>new_iib</Requestor>
	    <!-- SMSGateway password -->
		<SourceAppPassword>IIB@Vpbank</SourceAppPassword>
	    <!-- SMSGateway branch (required for input) -->
		<RequestBranch>VN0010005</RequestBranch>
		<!-- SMSGateway required propertie -->
		<MessageType>NotifyClient</MessageType>
	    <!-- InternalAPI endpoint for service -->
		<URL>http://10.37.24.28:8280/iapi/notifications/v1/sms</URL>
		<!-- ID of template for SMS sending -->
		<TemplateID>110</TemplateID>
	</SMS>
	<Template>
	    <!-- MSA endpoint for service -->
		<URL>http://dev-templates-engine.msa-apps.vpbank.com/templates/generate</URL>
	</Template>
	<OTP>
		<Send>
	        <!-- MSA endpoint for service -->
			<URL>http://dev-otp.msa-apps.vpbank.com/security/otp/send</URL>
		</Send>
	</OTP>
	<Consent>
		<Create>
	        <!-- MSA endpoint for service -->
			<URL>http://dev-vpb-consent-service.msa-apps.vpbank.com/consent/create</URL>
		</Create>
		<Check>
	        <!-- MSA endpoint for service -->
			<URL>http://dev-vpb-consent-service.msa-apps.vpbank.com/consent/payload</URL>
		</Check>
	</Consent>
	<VPBPlus>
		<Find>
	        <!-- MSA endpoint for service -->
			<URL>http://dev-channel-vpbplus.msa-apps.vpbank.com/find</URL>
		</Find>
	</VPBPlus>
	<Customers>
		<Info>
		    <!-- InternalAPI endpoint for service -->
			<URL>http://10.37.24.151:8150/iapi/customers/v1/info</URL>
		</Info>
	</Customers>
	<ResetPass>
	    <!-- Page location. There should be link to OpenAPI Endpoint -->
		<URL>http://42.112.212.36:8280/openui/reset-pass.do</URL>
	</ResetPass>
</RESTServices>
```

# Repository structure

RESTful interfaces are located: [src/main/java/vn/vpbank/wso2/identity/service/rest/model](src/main/java/vn/vpbank/wso2/identity/service/rest/model)  
Event handlers are located: -not used-  
Aggregate commands are located: -not used-  
Microservice business logic are located: [/src/main/java/vn/vpbank/wso2/identity/service/rest/UserMgtServlet.java](/src/main/java/vn/vpbank/wso2/identity/service/rest/UserMgtServlet.java)  
Backend interaction logic are located: [src/main/java/vn/vpbank/wso2/identity/service/rest/utils](src/main/java/vn/vpbank/wso2/identity/service/rest/utils)  
Configuration logic are located: [src/main/java/vn/vpbank/wso2/identity/service/rest/internal/RESTServiceComponent.java](src/main/java/vn/vpbank/wso2/identity/service/rest/internal/RESTServiceComponent.java)  
