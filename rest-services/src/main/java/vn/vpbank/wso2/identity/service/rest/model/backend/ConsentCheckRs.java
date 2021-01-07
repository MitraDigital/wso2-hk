package vn.vpbank.wso2.identity.service.rest.model.backend;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ConsentCheckRs {
    private String requestId;
    private String channel;
    private String operation;
    private Map<String, String> message;
    private AdditionalInfo additionalInfo;
    private Integer verified;
    private Integer count_attempts;
    private Date request_time;
    private String phoneNumber;
    private String aggregateId;
    private List<String> customerAccounts;


    public ConsentCheckRs() {

    }

    public Integer getVerified() {
        return verified;
    }

    public Map<String, String> getMessage() {
        return message;
    }

    private class AdditionalInfo {
        private String accountNumber;
    }
}
