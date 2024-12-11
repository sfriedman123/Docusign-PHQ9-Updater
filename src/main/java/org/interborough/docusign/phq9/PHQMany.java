package org.interborough.docusign.phq9;

import java.sql.Date;

public class PHQMany {

    private Date signedDate;
    private String envelopeId = "";
    private int clientId = 0;
    private int phqScore = 0;

    // Default Constructor
    public PHQMany() {}

    // Parameterized Constructor
    public PHQMany(Date signedDate, String envelopeId, int clientId, int phqScore) {
        this.signedDate = signedDate;
        this.envelopeId = envelopeId;
        this.clientId = clientId;
        this.phqScore = phqScore;
    }

    // Getters and Setters
    public Date getSignedDate() {
        return signedDate;
    }

    public void setSignedDate(Date signedDate) {
        this.signedDate = signedDate;
    }

    public String getEnvelopeId() {
        return envelopeId;
    }

    public void setEnvelopeId(String envelopeId) {
        this.envelopeId = envelopeId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getPhqScore() {
        return phqScore;
    }

    public void setPhqScore(int phqScore) {
        this.phqScore = phqScore;
    }

    // toString Method
    @Override
    public String toString() {
        return "PHQMany{" +
                "signedDate=" + signedDate +
                ", envelopeId='" + envelopeId + '\'' +
                ", clientId=" + clientId +
                ", phqScore=" + phqScore +
                '}';
    }
}
