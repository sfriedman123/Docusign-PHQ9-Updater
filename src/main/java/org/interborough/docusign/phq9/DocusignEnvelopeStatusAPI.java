package org.interborough.docusign.phq9;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import org.json.JSONObject;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.auth.OAuth.OAuthToken;

public class DocusignEnvelopeStatusAPI {
	
	
	public String getAccessToken(String propertiesFile) throws IOException, IllegalArgumentException, ApiException
	{
System.out.println("Step 1. get access token\n_________________\n");
    	
    	Properties prop = new Properties();
		String fileName = propertiesFile;
		FileInputStream fis = new FileInputStream(fileName);
		prop.load(fis);
    	

		// Get access token and accountId
		ApiClient apiClient = new ApiClient(prop.getProperty("apiRESTUrl"));
		apiClient.setOAuthBasePath(prop.getProperty("DocuSignAccount"));
	

		// apiClient.set
		ArrayList<String> scopes = new ArrayList<String>();
		scopes.add("signature");
		scopes.add("impersonation");
		// scopes.add("organization_read");
		
		byte[] privateKeyBytes = Files.readAllBytes(Paths.get(prop.getProperty("rsaKeyFile")));
		OAuthToken oAuthToken = apiClient.requestJWTUserToken(prop.getProperty("clientId"),
				prop.getProperty("userId"), scopes, privateKeyBytes, 3600);
		String accessToken = oAuthToken.getAccessToken();
		System.out.println("access token " + accessToken);
		return accessToken;
	}
	
	
	
	public static String getEnvelopeStatus(String accessToken, String envelopeId, String BASE_URL) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        
        System.out.println("REST call: " + BASE_URL   + "envelopes/" + envelopeId + "/recipients?include=recipients");
        System.out.println(BASE_URL + "envelopes/" + envelopeId + "?include=recipients");

        HttpRequest request = HttpRequest.newBuilder()
        		
            .uri(URI.create(BASE_URL + "/envelopes/" + envelopeId + "?include=recipients"))
            .header("Authorization", "Bearer " + accessToken)
            .header("Accept", "application/json")
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.statusCode());
        JSONObject jsonResponse = new JSONObject(response.body());
        //System.out.println("Envelope Status: " + jsonResponse.getString("status"));
        System.out.println(jsonResponse.toString());
        return jsonResponse.toString();
        
        // Retrieve recipients' status
//        JSONObject recipients = jsonResponse.getJSONObject("recipients");
//        System.out.println("Recipients Status: " + recipients.toString());
    }

}
