package vn.vpbank.wso2.identity.service.rest.model.backend;

import vn.vpbank.wso2.identity.service.rest.internal.RESTServiceComponent;

public class GeneratePasswordTemplateRq {

    private String password;

    public GeneratePasswordTemplateRq(String password){
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
