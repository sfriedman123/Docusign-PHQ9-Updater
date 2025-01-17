package org.interborough.docusign.phq9;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.docusign.esign.client.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BulkEnvelopes {

	String token = "";

//	public static void main(String[] args) throws Exception {
//
//		String configFilePath = args[0];
//		Properties prop = new Properties();
//		FileInputStream fis = null;
//
//		fis = new FileInputStream(configFilePath);
//
//		prop.load(fis);
//
//		String appFilePath = args[1];
//		Properties applicationProps = new Properties();
//		fis = new FileInputStream(appFilePath);
//		applicationProps.load(fis);
//
//		BulkEnvelopes benvs = new BulkEnvelopes();
//		benvs.storeAccessToken(configFilePath);
//
//		List<String> bulkSendIds = benvs.fetchAllBulkSendBatches(prop.getProperty("baseURL"), configFilePath);
//		System.out.println("Fetched Bulk Send IDs: " + bulkSendIds);
//		benvs.processEnvelopes(configFilePath, prop.getProperty("baseURL"), configFilePath, applicationProps);
//
//	}

	/**
	 * Fetch all bulk send batch IDs from the DocuSign API.
	 *
	 * @return List of bulk send batch IDs.
	 * @throws Exception
	 */

	public void processEnvelopes(String configFilePat, String URL, String configFilePath, Properties prop, String fromDate)
			throws Exception {
		BulkEnvelopes benvs = new BulkEnvelopes();
		benvs.storeAccessToken(configFilePath);
		List<String> bulkSendIds = benvs.fetchAllBulkSendBatches(URL, configFilePath, fromDate);
		for (String bulkId : bulkSendIds) {
			List<String> envelopes = benvs.getEnvelopesForBatch(bulkId, URL);
			System.out.println("Envelopes for Batch ID " + bulkId + ": " + envelopes);
			for (String envelope : envelopes) {
				DemoGraphicsEntity phqe = new DemoGraphicsEntity();
				phqe.setOrganization(prop.getProperty("organization"));
				DemograhpicsMany childRecord = new DemograhpicsMany();
				childRecord.setEnvelopeId(envelope);

				String json = getRecipientForEnvelope(this.token, URL, envelope);
				String status = extractValueFromNode(json, "signedDateTime");
				// we only want those that have been signed
				
				if (status != null) {
					String sentDateTime = extractValueFromNode(json, "sentDateTime");
					// Parse the date-time string to an OffsetDateTime
					OffsetDateTime offsetDateTime = OffsetDateTime.parse(sentDateTime);
					// Extract only the date
					LocalDate date = offsetDateTime.toLocalDate();
					java.util.Date utilDate = java.sql.Date.valueOf(date);
					childRecord.setSignedDate((Date) utilDate);

					String allTabs = getTabs(this.token, URL, envelope);

					System.out.println("ECR1: " + extractFirstECR1(allTabs));
					if (!extractFirstECR1(allTabs).equals("")) {
						String ECRCode = extractFirstECR1(allTabs);
//						if (ECRCode.trim().equals("158635")) {
//							System.out.println("issue");
//						}

						phqe.setClientId(extractFirstECR1(allTabs));
						childRecord.setClientId(phqe.getClientId());
						QueryExecutor qe = new QueryExecutor(prop);
						qe.executeQuery(prop, extractFirstECR1(allTabs), phqe);

						String JsonData = getDocumentsForEnvelopes(this.token, URL, envelope);
						String documentID = findDocumentIdByName(JsonData, "PHQ9");
						if (documentID != null) {
							System.out.println("FOUND PHQ");
							System.out.println(documentID);
							DatabaseHelper dbHelper = new DatabaseHelper(prop);
							if (!dbHelper.doesClientIdExist(phqe.getClientId()))
								dbHelper.insertIntoDocusignPhq9Master(phqe);
							else
								System.out.println("Already in master");
							String jsonData = getDocumentData(this.token, URL, envelope, documentID);
							DemographicsScores phqScore = new DemographicsScores();
							childRecord.setPhqScore(phqScore.  computeScore(jsonData));
							childRecord.setBulk(true);
							System.out.println("Computed score " + childRecord.getPhqScore());
							if (!dbHelper.doesEnvelopeIdExist(childRecord.getEnvelopeId(), phqe.getClientId())) {
								dbHelper.insertIntoPhqResults(childRecord);
							} else

							{
								System.out.println("this envelope exists.  Not writing it to the database");
							}

						} else

							System.out.println("NO PHQ");
					}
				} else {
					System.out.println("NOT SIGNED");
				}
			}

		}

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
		// System.out.println(response.body());
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
		// System.out.println(response.body());
		System.out.println("documents for this envelope");

		JSONObject jsonResponse = new JSONObject(response.body());
		// System.out.println("Envelope Status: " + jsonResponse.getString("status"));
		System.out.println(jsonResponse.toString());
		return jsonResponse.toString(4);

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
		// System.out.println(response.body());

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

				.uri(URI.create(BASE_URL + "/envelopes/" + envelopeId + "/recipients/1" + "/tabs"))
				.header("Authorization", "Bearer " + accessToken).header("Accept", "application/json").build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.statusCode());
		// System.out.println(response.body());
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

	public static String extractValueFromNode(String json, String nodeToFind) {
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

	public List<String> getAllBulkSendIds(String URL, String configFilePath) throws Exception {
		DocusignEnvelopeStatusAPI api = new DocusignEnvelopeStatusAPI();

		this.token = api.getAccessToken(configFilePath);
		System.out.println(token);
		String url = URL + "/bulk_send_batch?from_date=2024-07-01";

		String response = makeApiCall(url);
		System.out.println(response);

		// Parse JSON response to extract bulk send IDs
		List<String> bulkIds = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(response);

		if (rootNode.has("bulkSendBatchSummaries")) {
			for (JsonNode batch : rootNode.get("bulkSendBatchSummaries")) {
				if (batch.has("batchId")) {
					bulkIds.add(batch.get("batchId").asText());
				}
			}
		}

		return bulkIds;
	}

	/**
	 * Fetch all envelopes for a given bulk send batch ID.
	 *
	 * @param bulkSendId The bulk send batch ID to fetch envelopes for.
	 * @return List of envelope IDs for the batch.
	 */
	public List<String> getEnvelopesForBatch(String bulkSendId, String URL) throws Exception {
		String url = URL + "bulk_send_batch/" + bulkSendId + "/envelopes";

		String response = makeApiCall(url);

		// Parse JSON response to extract envelope IDs
		List<String> envelopeIds = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(response);

		if (rootNode.has("envelopes")) {
			for (JsonNode envelope : rootNode.get("envelopes")) {
				if (envelope.has("envelopeId")) {
					envelopeIds.add(envelope.get("envelopeId").asText());
				}
			}
		}

		return envelopeIds;
	}

	/**
	 * Makes a REST API call to the given URL.
	 *
	 * @param apiUrl The URL of the API endpoint.
	 * @return The response body as a String.
	 */
	private String makeApiCall(String apiUrl) throws Exception {
		URL url = new URL(apiUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		// Set the Authorization header
		String bearerAuth = "Bearer " + this.token;
		connection.setRequestProperty("Authorization", bearerAuth);

		// Read the response
		int responseCode = connection.getResponseCode();
		if (responseCode != 200) {
			throw new RuntimeException("HTTP GET Request Failed with Error code : " + responseCode);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	public void storeAccessToken(String configFilePath) throws IllegalArgumentException, IOException, ApiException {
		DocusignEnvelopeStatusAPI api = new DocusignEnvelopeStatusAPI();

		this.token = api.getAccessToken(configFilePath);
		System.out.println(token);

	}

	public List<String> fetchAllBulkSendBatches(String URL, String configFilePath, String fromDate) throws Exception {
		List<String> allBatchIds = new ArrayList<>();

		String url = URL + "/bulk_send_batch?from_date=" + fromDate + "&status=sent";

		while (url != null && !url.isEmpty()) {
			String response = makeApiCall(url);
			List<String> batchIdsFromResponse = parseBatchIds(response);
			allBatchIds.addAll(batchIdsFromResponse);

			// Extract the nextUri from the response JSON
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(response);

			if (rootNode.has("nextUri") && !rootNode.get("nextUri").isNull()) {
				url = URL + rootNode.get("nextUri").asText();
			} else {
				url = null; // Stop when no further 'nextUri' is available
			}
		}

		return allBatchIds;
	}

	/**
	 * Parse batch IDs from the JSON response.
	 * 
	 * @param jsonResponse Raw JSON response.
	 * @return List of batch IDs.
	 */
	private static List<String> parseBatchIds(String jsonResponse) throws Exception {
		List<String> batchIds = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonResponse);

		if (rootNode.has("bulkBatchSummaries")) {
			for (JsonNode batchSummary : rootNode.get("bulkBatchSummaries")) {
				if (batchSummary.has("batchId")) {
					batchIds.add(batchSummary.get("batchId").asText());
				}
			}
		}

		return batchIds;
	}

}
