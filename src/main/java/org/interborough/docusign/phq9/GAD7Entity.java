package org.interborough.docusign.phq9;

import java.sql.Date;

public class GAD7Entity {

    private Date signedDate;
    private String envelopeId = "";
    private String clientId = "";
    
   
	private boolean bulk = false;
	private Integer GAD7Score;

    // Default Constructor
    public GAD7Entity() {}

    // Parameterized Constructor
    public GAD7Entity(Date signedDate, String envelopeId, String clientId, int phqScore, boolean bulk) {
        this.signedDate = signedDate;
        this.envelopeId = envelopeId;
        this.clientId =  clientId;
     
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

  
    public int getGAD7Score() {
        return GAD7Score;
    }

    public void setGAD7Score(int gadScore) {
        this.GAD7Score = gadScore;
    }

    // toString Method
    @Override
    public String toString() {
        return "GAD7Entity{" +
                "signedDate=" + signedDate +
                ", envelopeId='" + envelopeId + '\'' +
                ", clientId=" + clientId +
              
                ", GAD7Score=" + GAD7Score +
                
                '}';
    }
}
