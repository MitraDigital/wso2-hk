package com.hkjc.wso2.identity.service.rest.model.web;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reset_CustomersDataRq {
    @JsonProperty("phoneNumber")
    private String phoneNumber;
    @JsonProperty("cardId")
    private String cardId;
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("secretAnswer")
    private String secretAnswer;
    @JsonProperty("secretQuestion")
    private String secretQuestion;
    @JsonProperty("correctData")
    private Boolean correctData;
    @JsonProperty("securedId")
    private String securedId;
    @JsonProperty("initDateTime")
    private Timestamp initDateTime;
    @JsonProperty("userId")
    private String userId;

    public Reset_CustomersDataRq(){}

    public Reset_CustomersDataRq(String phoneNumber, String cardId, String accountNumber, String secretAnswer, String secretQuestion){
        this.phoneNumber = phoneNumber;
        this.cardId = cardId;
        this.accountNumber = accountNumber;
        this.secretAnswer = secretAnswer;
        this.secretQuestion = secretQuestion;
    }

    public Reset_CustomersDataRq(String phoneNumber, String cardId, String accountNumber, String secretAnswer, String secretQuestion, Boolean correctData, String securedId,
                                 Timestamp initDateTime, String userId){
        this.phoneNumber = phoneNumber;
        this.cardId = cardId;
        this.accountNumber = accountNumber;
        this.secretAnswer = secretAnswer;
        this.secretQuestion = secretQuestion;
        this.correctData = correctData;
        this.securedId = securedId;
        this.initDateTime = initDateTime;
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCardId() {
        return cardId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSecretAnswer() {
        return secretAnswer;
    }

    public String getSecretQuestion() {
        return secretQuestion;
    }

    public void setCorrectData(Boolean correctData) {
        this.correctData = correctData;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSecretAnswer(String secretAnswer) {
        this.secretAnswer = secretAnswer;
    }

    public void setSecretQuestion(String secretQuestion) {
        this.secretQuestion = secretQuestion;
    }

    public void setSecuredId(String securedId) {
        this.securedId = securedId;
    }

    public void setInitDateTime(Timestamp initDateTime) {
        this.initDateTime = initDateTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}