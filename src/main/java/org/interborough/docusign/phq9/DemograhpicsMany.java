package org.interborough.docusign.phq9;

import java.sql.Date;

public class DemograhpicsMany {

    private Date signedDate;
    private String envelopeId = "";
    private String clientId = "";
    private Integer phqScore = null;
    
	private Integer question9Score = null;
   

	private boolean bulk = false;


    // Default Constructor
    public DemograhpicsMany() {}

    // Parameterized Constructor
    public DemograhpicsMany(Date signedDate, String envelopeId, String clientId, int phqScore, boolean bulk, int question9Score) {
        this.signedDate = signedDate;
        this.envelopeId = envelopeId;
        this.clientId =  clientId;
        this.phqScore = phqScore;
        this.bulk = bulk;
        this.question9Score = question9Score;
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
    
    public Integer getQuestion9Score() {
		return question9Score;
	}

	public void setQuestion9Score(Integer question9Score) {
		this.question9Score = question9Score;
	}


    // toString Method
    @Override
    public String toString() {
        return "PHQMany{" +
                "signedDate=" + signedDate +
                ", envelopeId='" + envelopeId + '\'' +
                ", clientId=" + clientId +
                ", phqScore=" + phqScore +
                ", question9Score=" + this.question9Score +
                
               
                
                '}';
    }
}
