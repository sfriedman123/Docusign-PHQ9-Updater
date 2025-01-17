package org.interborough.docusign.phq9;

import java.sql.Date;

public class DemograhpicsMany {

    private Date signedDate;
    private String envelopeId = "";
    private String clientId = "";
    private Integer phqScore = null;
   

	private boolean bulk = false;


    // Default Constructor
    public DemograhpicsMany() {}

    // Parameterized Constructor
    public DemograhpicsMany(Date signedDate, String envelopeId, String clientId, int phqScore, boolean bulk) {
        this.signedDate = signedDate;
        this.envelopeId = envelopeId;
        this.clientId =  clientId;
        this.phqScore = phqScore;
        this.bulk = bulk;
    }

    // Getters and Setters
    
    public boolean isBulk() {
		return bulk;
	}

	public void setBulk(boolean bulk) {
		this.bulk = bulk;
	}
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
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
