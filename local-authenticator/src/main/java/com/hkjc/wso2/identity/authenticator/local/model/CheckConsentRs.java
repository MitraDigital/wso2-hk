package com.hkjc.wso2.identity.authenticator.local.model;

public class CheckConsentRs {

    private boolean isVerified;
    private String verified;
    private String attempts_over;
    private String time_over;

    public boolean isVerified() {
        return isVerified;
    }

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
        this.isVerified = Boolean.valueOf(verified);
    }

    public String getAttempts_over() {
        return attempts_over;
    }

    public void setAttempts_over(String attempts_over) {
        this.attempts_over = attempts_over;
    }

    public String getTime_over() {
        return time_over;
    }

    public void setTime_over(String time_over) {
        this.time_over = time_over;
    }
}
