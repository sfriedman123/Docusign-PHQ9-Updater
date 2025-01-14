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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

public class PQH9DocusignApplication {

	// fech all envelopes.  
	// look up 

	final static String API =  "/envelopes?from_date=";

	static LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
	// static LocalDate threeMonthsAgo = LocalDate.now().minusDays(1);
	static String fromDate = threeMonthsAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

	// Output the calculated date


	public static void main(String[] args) throws Exception {

		String configFilePath = args[0];
		Properties prop = new Properties();
		FileInputStream fis = null;

		fis = new FileInputStream(configFilePath);

		prop.load(fis);

		String appFilePath = args[1];
		Properties applicationProps = new Properties();

		fis = new FileInputStream(appFilePath);

		applicationProps.load(fis);

		DocusignEnvelopeStatusAPI api = new DocusignEnvelopeStatusAPI();

		String token = api.getAccessToken(configFilePath);
		System.out.println(token);
		ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);

		// Format the date and time to the desired format
		String formattedDate = utcNow.format(DateTimeFormatter.ISO_INSTANT);

		// Doesn't look like we have any docusign phq9 before this date.


		fetchAllEnvelopes(token, prop.getProperty("baseURL"),fromDate, formattedDate, applicationProps, API, false,  prop);

		// Bulk has to use a different Docusign API

		//		BulkEnvelopes benvs = new BulkEnvelopes();
		//		benvs.storeAccessToken(configFilePath);
		//		
		//		List<String> bulkSendIds = benvs.fetchAllBulkSendBatches(prop.getProperty("baseURL"), configFilePath, fromDate);
		//		System.out.println("Fetched Bulk Send IDs: " + bulkSendIds);
		//		benvs.processEnvelopes(configFilePath, prop.getProperty("baseURL"), configFilePath, applicationProps, fromDate);

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
		//		String formattedDate = thirtyDaysAgo.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
		//		String urlForAPI = BASE_URL + formattedDate;
		//		System.out.println(urlForAPI);
		String urlRequest = BASE_URL + "/envelopes/" + envelopeId + "/documents";
		System.out.println("\n>> getDocumentsForEnvelopes: " + urlRequest);

		HttpRequest request = HttpRequest.newBuilder()

				.uri(URI.create(urlRequest))
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println("Response: " + response.statusCode());
		//System.out.println(response.body());
		System.out.println("documents for this envelope");

		JSONObject jsonResponse = new JSONObject(response.body());
		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));
		System.out.println(jsonResponse.toString());
		System.out.println("<< getDocumentsForEnvelopes");
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
	public static List<JSONObject> fetchAllEnvelopes(String accessToken, String BASE_URL, String fromDate, String toDate,Properties prop, String API, boolean bulk, Properties configProps ) throws Exception {

		List<JSONObject> allEnvelopes = new ArrayList<>();

		HttpClient client = HttpClient.newHttpClient();
		String endpoint = BASE_URL + API + fromDate + "&" + "to_Date=" + toDate + "&status=completed&order_by=sent";
		System.out.println("API call for envelopes: " + endpoint);
		String nextUri = endpoint;
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
				DatabaseHelper dbHelper = new DatabaseHelper(prop);
				JSONObject envelope = envelopes.getJSONObject(i);
				System.out.println("******************************************************************\n");
				System.out.println("Start Processing Envelope ID: " + envelope.getString("envelopeId"));
				System.out.println("Envelope ID: " + envelope.getString("envelopeId"));

				PHQEntity phqe = new PHQEntity();

				// let's populate the location from the properties file
				phqe.setOrganization(configProps .getProperty("organization"));				
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
				String ECR = extractFirstECR1(allTabs );
				if (! ECR.equals("") &&  (ECR.matches("-?\\d+") ))
					//if (! ECR.equals("")  )	
				{
					phqe.setClientId(extractFirstECR1(allTabs));
					childRecord.setClientId(phqe.getClientId());

					QueryExecutor qe = new QueryExecutor(prop);
					qe.executeQuery(prop, extractFirstECR1(allTabs), phqe);

					String JsonData = getDocumentsForEnvelopes(accessToken, BASE_URL, envelope.getString("envelopeId"));

					String documentID = findDocumentIdByName(JsonData, "PHQ9");

					if (documentID != null)
					{
						System.out.println("FOUND PHQ");	
						System.out.println(documentID);

						if (! dbHelper.doesClientIdExist(phqe.getClientId()))
							dbHelper.insertIntoDocusignPhq9Master(phqe);
						else
							System.out.println("Already in master");
						String jsonData = getDocumentData(accessToken, BASE_URL, envelope.getString("envelopeId"), documentID  );

						PHQScore phqScore = new PHQScore();
						childRecord.setPhqScore(phqScore.  computeScore(jsonData));
						childRecord.setBulk(false);
						System.out.println("Computed score " + childRecord.getPhqScore());

						if (! dbHelper.doesEnvelopeIdExist(childRecord.getEnvelopeId(), phqe.getClientId()))
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
				else //there's an invalid ECR code in the docusign document
				{  
					System.out.println("Invalid ECR.  Insert into Error table");
					dbHelper.insertPHQ9Error(ECR, signedDate,  envelope.getString("envelopeId"), phqe.getOrganization());



				}

				System.out.println("Finished Processiong Envelope ID: " + envelope.getString("envelopeId"));
				System.out.println("=====================================================================\n");

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


	private static String extractValue(String responseBody, String key) {
		int startIndex = responseBody.indexOf(key);
		if (startIndex == -1) return null;
		startIndex += key.length();
		int endIndex = responseBody.indexOf(",", startIndex);
		if (endIndex == -1) endIndex = responseBody.indexOf("}", startIndex);
		return responseBody.substring(startIndex, endIndex).trim().replace("\"", "");
	}






}
