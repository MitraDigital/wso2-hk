package com.hkjc.wso2.identity.service.rest.model.web;

public class CreateUserRs {
    private String userName;

    public CreateUserRs(String userName){
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
