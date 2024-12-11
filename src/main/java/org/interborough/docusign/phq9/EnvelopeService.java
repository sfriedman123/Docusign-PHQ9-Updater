package org.interborough.docusign.phq9;




import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EnvelopeService {

    @Value("${docuSign.baseURL}")
    private String baseURL;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetch all envelopes between two dates
     */
    public List<JSONObject> fetchAllEnvelopes(String token, String fromDate, String toDate) throws Exception {
        List<JSONObject> allEnvelopes = new ArrayList<>();
        String endpoint = baseURL + "/envelopes?from_date=" + fromDate + "&to_date=" + toDate + "&status=completed";

        while (endpoint != null) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject responseBody = new JSONObject(response.body());
            JSONArray envelopes = responseBody.getJSONArray("envelopes");

            for (int i = 0; i < envelopes.length(); i++) {
                JSONObject envelope = envelopes.getJSONObject(i);
                allEnvelopes.add(envelope);
            }

            endpoint = responseBody.optString("nextUri", null);
            if (endpoint != null && !endpoint.isEmpty()) {
                endpoint = baseURL + endpoint;
            }
        }

        return allEnvelopes;
    }
}
