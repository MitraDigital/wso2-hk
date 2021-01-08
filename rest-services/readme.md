# Description

Custom RESTfull services which extend basic IS functionality

# How to deploy

### Jar file location
${IS_HOME}/repository/components/dropins/com.hkjc.wso2.identity.service.rest-2.0.0.jar

### Required libs 
${IS_HOME}/repository/components/lib/passay-1.3.1.jar  
${IS_HOME}/repository/components/lib/jackson-annotations-2.9.4.jar  
${IS_HOME}/repository/components/lib/jackson-databind-2.9.4.jar  
${IS_HOME}/repository/components/lib/jackson-core-2.9.4.jar  

# add below to repository/conf/deployment.toml (IS 5.9.0 and above)

[resource_access_control]
default_access = "allow"
