package org.interborough.docusign.phq9;

public class DemoGraphicsEntity {

    private String staffName="";
    private String credentials="";
    private String clientId="";
    private String clientName="";
    private String organization="";

    // Constructors
    public DemoGraphicsEntity() {
    }

    public DemoGraphicsEntity(String staffName, String credentials, String clientId, String clientName, String organization) {
        this.staffName = staffName;
        this.credentials = credentials;
        this.clientId = clientId;
        this.clientName = clientName;
        this.organization = organization;
    }

    // Getters and Setters
    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    // toString Method
    @Override
    public String toString() {
        return "StaffDetails{" +
                "staffName='" + staffName + '\'' +
                ", credentials='" + credentials + '\'' +
                ", clientId=" + clientId +
                ", clientName='" + clientName + '\'' +
                ", organization='" + organization + '\'' +
                '}';
    }
}
