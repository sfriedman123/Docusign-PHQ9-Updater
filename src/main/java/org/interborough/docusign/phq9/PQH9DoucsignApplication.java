package org.interborough.docusign.phq9;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PQH9DoucsignApplication {

	// fech all envelopes.  
	// look up 

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		// get 

		String configFilePath = args[0];
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(configFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			prop.load(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String appFilePath = args[1];
		Properties applicationProps = new Properties();

		try {
			fis = new FileInputStream(appFilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			applicationProps.load(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		DocusignEnvelopeStatusAPI api = new DocusignEnvelopeStatusAPI();


		String token = api.getAccessToken(configFilePath);
		System.out.println(token);

		//getAllEnvelopes(token, "https://na4.docusign.net//restapi/v2.1/accounts/6543288/");
		fetchAllEnvelopes(token, prop.getProperty("baseURL"),"2021-09-01T00:00:00Z", "2024-12-10T23:59:59Z", applicationProps );

		//String json = getRecipientForEnvelope(token, "https://na4.docusign.net//restapi/v2.1/accounts/6543288/", "EB39B875-DC39-4D53-864F-9D4015849770");


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
					if (document.has("name") && document.get("name").asText().equals(documentName)) {
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



	public static Map<String, String> getSelectedRadioValues(String jsonData) {
		// Parse the input JSON string to get the radioGroupTabs array
		JSONArray radioGroupTabs = new JSONObject(jsonData).getJSONArray("radioGroupTabs");

		// Map to store selected radio button values for each group
		Map<String, String> selectedRadioValues = new HashMap<>();

		// Iterate over each radio group
		for (int i = 0; i < radioGroupTabs.length(); i++) {
			JSONObject radioGroup = radioGroupTabs.getJSONObject(i);
			String groupName = radioGroup.getString("groupName");
			JSONArray radios = radioGroup.getJSONArray("radios");

			// Iterate over each radio button in the group
			for (int j = 0; j < radios.length(); j++) {
				JSONObject radio = radios.getJSONObject(j);

				// Check if the radio button is selected
				if (radio.getString("selected").equals("true")) {
					// Store the selected value in the map
					selectedRadioValues.put(groupName, radio.getString("value"));
				}
			}
		}

		return selectedRadioValues;
	}

	public static List<Map<String, String>> extractEmailAndName(String json) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		// Parse the JSON
		JsonNode rootNode = objectMapper.readTree(json);

		// Access the "signers" array
		JsonNode signers = rootNode.get("signers");

		// Prepare the result list
		List<Map<String, String>> signerDetails = new ArrayList<>();

		for (JsonNode signer : signers) {
			Map<String, String> details = new HashMap<>();
			details.put("name", signer.get("name").asText());
			details.put("email", signer.get("email").asText());
			signerDetails.add(details);
		}

		return signerDetails;
	}

	public static String getDocumentData(String accessToken, String BASE_URL, String envelopeId, String documentID)
			throws IOException, InterruptedException {

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
		System.out.println(jsonResponse.toString());
		return jsonResponse.toString(4);

	}	

	public static String getRecipientForEnvelope(String accessToken, String BASE_URL, String envelopeId)
			throws Exception {

		HttpClient client = HttpClient.newHttpClient();

		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

		// Convert to ISO 8601 format
		String formattedDate = thirtyDaysAgo.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String urlForAPI = BASE_URL + formattedDate;
		System.out.println(urlForAPI);

		HttpRequest request = HttpRequest.newBuilder()

				.uri(URI.create(BASE_URL + "/envelopes/" + envelopeId + "/recipients"))
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.statusCode());
		//System.out.println(response.body());
		System.out.println("recipient for envelope");

		JSONObject jsonResponse = new JSONObject(response.body());
		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));
		System.out.println(jsonResponse.toString());

		List<Map<String, String>> results = extractEmailAndName(jsonResponse.toString());


		for (Map<String, String> signer : results) {
			System.out.println("Name: " + signer.get("name") + ", Email: " + signer.get("email"));

		}
		return jsonResponse.toString(4);

	}	

	public static String extractValueFromNode(String json, String nodeToFind)
	{
		String signedDate = null;
		try {

			ObjectMapper mapper = new ObjectMapper();

			// Parse JSON string into a JsonNode
			JsonNode rootNode = mapper.readTree(json);


			// Navigate to "signers[0].signedDateTime"
			JsonNode signedDateNode = rootNode.path("signers").get(0).path(nodeToFind);


			if (!signedDateNode.isMissingNode()) {
				signedDate = signedDateNode.asText();
				System.out.println("Signed Date: " + signedDate);
			} else {
				System.out.println("signedDateTime not found!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return signedDate;

	}

	public static String getDocumentsForEnvelopes(String accessToken, String BASE_URL, String envelopeId)
			throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();

		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

		// Convert to ISO 8601 format
		String formattedDate = thirtyDaysAgo.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String urlForAPI = BASE_URL + formattedDate;
		System.out.println(urlForAPI);

		HttpRequest request = HttpRequest.newBuilder()

				.uri(URI.create(BASE_URL + "/envelopes/" + envelopeId + "/documents"))
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.statusCode());
		//System.out.println(response.body());
		System.out.println("documents for this envelope");

		JSONObject jsonResponse = new JSONObject(response.body());
		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));
		System.out.println(jsonResponse.toString());
		return jsonResponse.toString(4);

	}	

	public static String getTabs(String accessToken, String BASE_URL, String envelopeId)
			throws IOException, InterruptedException {

		HttpClient client = HttpClient.newHttpClient();

		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

		// Convert to ISO 8601 format
		String formattedDate = thirtyDaysAgo.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String urlForAPI = BASE_URL + formattedDate;
		System.out.println(urlForAPI);

		HttpRequest request = HttpRequest.newBuilder()

				.uri(URI.create(BASE_URL + "/envelopes/" + envelopeId + "/recipients/1" + "/tabs" ))
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.statusCode());
		//System.out.println(response.body());
		System.out.println("document for envelope");

		JSONObject jsonResponse = new JSONObject(response.body());
		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));
		System.out.println(jsonResponse.toString());
		return jsonResponse.toString(4);

	}

	public static String extractFirstECR1(String json) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();

		// Parse the JSON
		JsonNode rootNode = objectMapper.readTree(json);

		// Access the "textTabs" array
		JsonNode textTabs = rootNode.get("textTabs");
		String value = "";

		if (textTabs != null && textTabs.isArray()) {
			for (JsonNode tab : textTabs) {
				String tabLabel = tab.get("tabLabel").asText();
				if ("ECR1".equals(tabLabel)) {
					value = tab.get("value").asText();

				}
			}
		}

		return value; // Return null if no ECR1 tab is found
	}
	public static int computeScore(String jsonData) {
		// Parse the input JSON string to get the radioGroupTabs array
		JSONArray radioGroupTabs = new JSONObject(jsonData).getJSONArray("radioGroupTabs");

		// Initialize the total score
		int totalScore = 0;

		// Define scores for each radio option (assuming each radio button has a score associated with its value)
		Map<String, Integer> radioScores = new HashMap<>();
		radioScores.put("Radio1", 0);  // Example: Radio1 has a score of 1
		radioScores.put("Radio2", 1);  // Example: Radio2 has a score of 2
		radioScores.put("Radio3", 2);  // Example: Radio3 has a score of 3
		radioScores.put("Radio4", 3);  // Example: Radio4 has a score of 4

		// Iterate over each radio group
		for (int i = 0; i < radioGroupTabs.length(); i++) {
			JSONObject radioGroup = radioGroupTabs.getJSONObject(i);
			JSONArray radios = radioGroup.getJSONArray("radios");

			// Iterate over each radio button in the group
			for (int j = 0; j < radios.length(); j++) {
				JSONObject radio = radios.getJSONObject(j);

				// Check if the radio button is selected
				if (radio.getString("selected").equals("true")) {
					String radioValue = radio.getString("value");

					// Add the score for the selected radio to the total score
					if (radioScores.containsKey(radioValue)) {
						totalScore += radioScores.get(radioValue);
					}
				}
			}
		}

		return totalScore;
	}


	public static List<JSONObject> fetchAllEnvelopes(String accessToken, String BASE_URL, String fromDate, String toDate,Properties prop) throws Exception {

		List<JSONObject> allEnvelopes = new ArrayList<>();

		HttpClient client = HttpClient.newHttpClient();

		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

		// Convert to ISO 8601 format
		String formattedDate = thirtyDaysAgo.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		String urlForAPI = BASE_URL + formattedDate;
		System.out.println(urlForAPI);
		String endpoint = BASE_URL + "/envelopes?from_date=" + fromDate + "&" + "to_Date=" + toDate + "&&status=completed";

		String nextUri = endpoint;

		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));

		while (! (nextUri.equals(""))) {
			// Fetch data from the current endpoint
			System.out.println(endpoint);


			HttpRequest request = HttpRequest.newBuilder()

					.uri(URI.create(endpoint))
					.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.statusCode());

			System.out.println(response.body());

			JSONObject responseBody = new JSONObject(response.body());
			JSONArray envelopes = responseBody.getJSONArray("envelopes");

			// Process each envelope
			for (int i = 0; i < envelopes.length(); i++) {
				JSONObject envelope = envelopes.getJSONObject(i);
				System.out.println("Envelope ID: " + envelope.getString("envelopeId"));

				PHQEntity phqe = new PHQEntity();
				PHQMany childRecord = new PHQMany();
				childRecord.setEnvelopeId( envelope.getString("envelopeId"));


				String json = getRecipientForEnvelope(accessToken, BASE_URL, envelope.getString("envelopeId"));
				String signedDate =  extractValueFromNode(json, "signedDateTime");
				Instant instant = Instant.parse(signedDate);

				// Convert Instant to SQL Date
				childRecord.setSignedDate(new Date(instant.toEpochMilli()));
				//String envelopeID =  extractValueFromNode(json, "envelopeId");

				System.out.println("signed date:" + signedDate + "envelopeid: " +   envelope.getString("envelopeId"));
				//phqe.setClientName(json);

				String allTabs = getTabs(accessToken, BASE_URL, envelope.getString("envelopeId") );
				//String signedDate =  extractSignedDate();

				//System.out.println("signed date:" + signedDate);
				System.out.println("ECR1: " + extractFirstECR1(allTabs ));
				if (! extractFirstECR1(allTabs ).equals("") )
				{
					phqe.setClientId(Integer.parseInt(extractFirstECR1(allTabs)));
					childRecord.setClientId(phqe.getClientId());

					QueryExecutor qe = new QueryExecutor(prop);
					qe.executeQuery(prop, extractFirstECR1(allTabs), phqe);

					String JsonData = getDocumentsForEnvelopes(accessToken, BASE_URL, envelope.getString("envelopeId"));

					String documentID = findDocumentIdByName(JsonData, "PHQ9");

					if (documentID != null)
					{
						System.out.println("FOUND PHQ");	
						System.out.println(documentID);
						DatabaseHelper dbHelper = new DatabaseHelper(prop);
						if (! dbHelper.doesClientIdExist(phqe.getClientId()))
							dbHelper.insertIntoDocusignPhq9Master(phqe);
						else
							System.out.println("Already in master");
						String jsonData = getDocumentData(accessToken, BASE_URL, envelope.getString("envelopeId"), documentID  );
						childRecord.setPhqScore(computeScore(jsonData));


						System.out.println(computeScore(jsonData));
						if (! dbHelper.doesEnvelopeIdExist(childRecord.getEnvelopeId()))
						{
							dbHelper.insertIntoPhqResults(childRecord);
						}
						{
							System.out.println("this envelope exists.  Not writing it to the database");
						}

					}
					else

						System.out.println("NO PHQ");
				}

			}

			// Get the next URI for pagination
			nextUri = responseBody.optString("nextUri", null);
			System.out.println("endpoint: " + nextUri);

			endpoint = "https://na4.docusign.net//restapi/v2.1" + nextUri;
			System.out.println("endpoint: " + endpoint);
			if (! (nextUri.equals(""))) {
				System.out.println("Fetching next page: " + nextUri);
			}

		}

		return allEnvelopes;
	}

	public static List<String> fetchAllEnvelopes2(String accessToken, String BASE_URL) throws Exception {
		String endpoint = BASE_URL +  "/envelopes?from_date=2024-11-29T00:00:00Z&to_date=2024-12-04T23:59:59Z&&status=completed";
		List<String> allEnvelopes = new ArrayList<>();

		HttpClient client = HttpClient.newHttpClient();

		while (endpoint != null) {
			// Build the full URL
			String url = BASE_URL + endpoint;

			// Prepare the request
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.header("Authorization", "Bearer " + accessToken)
					.header("Content-Type", "application/json")
					.GET()
					.build();

			// Send the request
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.statusCode());


			//			if (response.statusCode() != 200) {
			//				throw new RuntimeException("Failed: HTTP error code : " + response.statusCode());
			//			}

			// Parse the response to extract envelopes and nextUri
			String responseBody = response.body();
			String[] lines = responseBody.split("\\n"); // Quick way to split for plain text
			for (String line : lines) {
				if (line.contains("\"envelopeId\":")) {
					// Extract envelopeId and add it to the list
					String envelopeId = extractValue(line, "\"envelopeId\":");
					allEnvelopes.add(envelopeId);
				}
			}

			// Find nextUri
			endpoint = extractValue(responseBody, "\"nextUri\":");
			if (endpoint != null) {
				endpoint = endpoint.replace("\\", "").replace("\"", ""); // Clean up URI
			}
		}

		return allEnvelopes;
	}
	private static String extractValue(String responseBody, String key) {
		int startIndex = responseBody.indexOf(key);
		if (startIndex == -1) return null;
		startIndex += key.length();
		int endIndex = responseBody.indexOf(",", startIndex);
		if (endIndex == -1) endIndex = responseBody.indexOf("}", startIndex);
		return responseBody.substring(startIndex, endIndex).trim().replace("\"", "");
	}






}
