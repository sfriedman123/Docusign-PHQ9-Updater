package org.interborough.docusign.phq9;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocusignDocuments {
	
	
	
	
	public boolean processPHQEDocument(String JsonData, DatabaseHelper dbHelper, DemoGraphicsEntity phqe, String accessToken, String envelopeId, String BASE_URL, DemograhpicsMany childRecord) throws IOException, InterruptedException
	{
		
		
		String documentID = findDocumentIdByName(JsonData, "PHQ9");
		Boolean PHQ9Found=false;

		if (documentID != null)
		{
			System.out.println("FOUND PHQ");
			PHQ9Found = true;
			
			System.out.println(documentID);
			

			if (! dbHelper.doesClientIdExist(phqe.getClientId()))
				dbHelper.insertIntoDocusignPhq9Master(phqe);
			else
				System.out.println("Already in master");
			String jsonData = getDocumentData(accessToken, BASE_URL, envelopeId, documentID  );

			DemographicsScores phqScore = new DemographicsScores();
			childRecord.setPhqScore(phqScore.  computeScore(jsonData));
			childRecord.setBulk(false);
			System.out.println("Computed PHQ9 score " + childRecord.getPhqScore());

		}
		else
			

			System.out.println("NO PHQ");
		
		return PHQ9Found;
	
	
	}
	
	public Boolean processGAD7Document(String JsonData, DatabaseHelper dbHelper, DemoGraphicsEntity phqe, String accessToken, String envelopeId, String BASE_URL, GAD7Entity childRecord) throws IOException, InterruptedException
	{
		
		String documentID = findDocumentIdByName(JsonData, "GAD7");
		Boolean GAD7Found=false;

		if (documentID != null)
		{
			System.out.println("FOUND GAD");
			GAD7Found = true;
			System.out.println(documentID);

			if (! dbHelper.doesClientIdExist(phqe.getClientId()))
				dbHelper.insertIntoDocusignPhq9Master(phqe);
			else
				System.out.println("Already in master");
			String jsonData = getDocumentData(accessToken, BASE_URL, envelopeId, documentID  );

			DemographicsScores GADScore = new DemographicsScores();
			childRecord.setGAD7Score(GADScore.computeScore(jsonData));
			childRecord.setBulk(false);
			System.out.println("Computed GAD7 score " + childRecord.getGAD7Score());

//			if (! dbHelper.doesEnvelopeIdExist(childRecord.getEnvelopeId(), phqe.getClientId()))
//			{
//				dbHelper.insertIntoPhqResults(childRecord);
//			}
//			{
//				System.out.println("this envelope exists.  Not writing it to the database");
//			}

		}
		else

			System.out.println("NO GAD");
		return GAD7Found;
	
	
	}
	
	
	public static String findDocumentIdByName(String jsonString, String documentName) {
		try {
			// Create an ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			// Parse the JSON string into a JsonNode
			JsonNode rootNode = objectMapper.readTree(jsonString);

			// Get the "envelopeDocuments" array
			JsonNode envelopeDocuments = rootNode.get("envelopeDocuments");

			// Loop through the documents
			if (envelopeDocuments != null && envelopeDocuments.isArray()) {
				for (JsonNode document : envelopeDocuments) {
					// Check if the name matches
					if (document.has("name") && document.get("name").asText().contains(documentName)) {
						// Return the documentId
						return document.get("documentId").asText();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error parsing JSON: " + e.getMessage());
		}
		return null; // Return null if no match is found
	}
	
	public static String getDocumentData(String accessToken, String BASE_URL, String envelopeId, String documentID)
			throws IOException, InterruptedException {
		System.out.println("Document ID: " + documentID);

		HttpClient client = HttpClient.newHttpClient();

		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

		// Convert to ISO 8601 format
		String formattedDate = thirtyDaysAgo.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String urlForAPI = BASE_URL + formattedDate;
		System.out.println(urlForAPI);

		HttpRequest request = HttpRequest.newBuilder()

				.uri(URI.create(BASE_URL + "/envelopes/" + envelopeId + "/documents/" + documentID + "/tabs"))
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.statusCode());
		//System.out.println(response.body());

		JSONObject jsonResponse = new JSONObject(response.body());
		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));
		System.out.println("Document Data: " + jsonResponse.toString());
		return jsonResponse.toString(4);

	}	


}
