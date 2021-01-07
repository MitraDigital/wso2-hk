package com.hkjc.wso2.identity.service.rest.model.backend;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerInfoRs {
    @JsonProperty
    private String customerNumber;
    @JsonProperty
    private String customerName;
    @JsonProperty
    private String shortName;
    @JsonProperty
    private ArrayList<DocumentList> documentList;
    @JsonProperty
    private ArrayList<ContactList> contactList;
    @JsonProperty
    private String dateOfBirth;
    @JsonProperty
    private String permanentAddress;
    @JsonProperty
    private String currentAddress;
    @JsonProperty
    private String customerType;
    @JsonProperty
    private String segment;
    @JsonProperty
    private String country;
    @JsonProperty
    private String residence;
    @JsonProperty
    private String companyBook;
    @JsonProperty
    private String gender;
    @JsonProperty
    private String DAO;
    @JsonProperty
    private String DAOPB;
    @JsonProperty
    private String VIPType;
    @JsonProperty
    private String jobTitle;
    @JsonProperty
    private String legalId;
    @JsonProperty
    private String sectorCode;
    @JsonProperty
    private String SBVSector;
    @JsonProperty
    private String nationalID;
    @JsonProperty
    private String channel;
    @JsonProperty
    private String maritalStatus;
    @JsonProperty
    private String education;
    @JsonProperty
    private String occupation;
    @JsonProperty
    private String officeName;
    @JsonProperty
    private String officeAddress;
    @JsonProperty
    private String noticeList;
    @JsonProperty
    private String priorityBranch;
    @JsonProperty
    private String priorityIdentificationDate;
    @JsonProperty
    private String DAORm;
    @JsonProperty
    private String nationality;
    @JsonProperty
    private String provinceCity;
    @JsonProperty
    private String taxCode;
    @JsonProperty
    private String taxCoIssDate;
    @JsonProperty
    private String corpCustType;
    @JsonProperty
    private String buIssuedDate;
    @JsonProperty
    private String buIssuedPlace;
    @JsonProperty
    private String bOMMemID;
    @JsonProperty
    private String bOMMemName;
    @JsonProperty
    private String bOMMemAddr;
    @JsonProperty
    private String legalRepId;
    @JsonProperty
    private String legalRepName;
    @JsonProperty
    private String legalRepTitle;

    public CustomerInfoRs(){}

    public String getLegalid() {
        return legalId;
    }
}

class DocumentList
{
    @JsonProperty
    private String type;
    @JsonProperty
    private String number;
    @JsonProperty
    private String issuePlace;
    @JsonProperty
    private String issueDate;
    @JsonProperty
    private String primary;
}

class ContactList
{
    @JsonProperty
    private String contactType;
    @JsonProperty
    private String contactInfo;
}