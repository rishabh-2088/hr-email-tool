package com.rishabh.hrtool.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CandidateRequest {

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @NotBlank(message = "Candidate email is required")
    @Email(message = "Invalid email format")
    private String candidateEmail;

    @NotBlank(message = "Position is required")
    private String position;

    @NotBlank(message = "Status is required (Selected / Rejected)")
    private String status;

    // Optional: If HR wants to edit the previewed email body before sending
    private String overrideBody;

    // Optional: Allow custom sender address (else fallback to app default)
    private String fromEmail;

    public CandidateRequest() {}

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOverrideBody() {
        return overrideBody;
    }

    public void setOverrideBody(String overrideBody) {
        this.overrideBody = overrideBody;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
