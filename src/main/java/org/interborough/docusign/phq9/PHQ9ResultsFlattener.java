package org.interborough.docusign.phq9;



import java.sql.*;
import java.util.*;

public class PHQ9ResultsFlattener {

    // Method to flatten query results
    public static void flattenPHQ9Results(String url, String user, String password, String query) {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // LinkedHashMap to maintain insertion order for Client_IDs
            Map<String, List<Map<String, String>>> clientDataMap = new LinkedHashMap<>();

            // Process the result set
            while (rs.next()) {
                String clientId = rs.getString("Client_ID");

                // Initialize list if this Client_ID appears for the first time
                clientDataMap.putIfAbsent(clientId, new ArrayList<>());

                // Create a map of PHQ9_Date and PHQ9_Score for this record
                Map<String, String> scoreData = new HashMap<>();
                scoreData.put("PHQ9_Date", rs.getString("PHQ9_Date"));
                scoreData.put("PHQ9_Score", rs.getString("PHQ9_Score"));
                scoreData.put("EnvelopeID", rs.getString("EnvelopeID")); // Add EnvelopeID

                // Add score data for this client
                clientDataMap.get(clientId).add(scoreData);
            }

            // Flatten and print results
            for (Map.Entry<String, List<Map<String, String>>> entry : clientDataMap.entrySet()) {
                String clientId = entry.getKey();
                List<Map<String, String>> scores = entry.getValue();

                StringBuilder flattenedRow = new StringBuilder("Client_ID: " + clientId);
                for (int i = 0; i < scores.size(); i++) {
                    flattenedRow.append(", PHQ9_Date_").append(i + 1).append(": ").append(scores.get(i).get("PHQ9_Date"));
                    flattenedRow.append(", PHQ9_Score_").append(i + 1).append(": ").append(scores.get(i).get("PHQ9_Score"));
                    flattenedRow.append(", EnvelopeID_").append(i + 1).append(": ").append(scores.get(i).get("EnvelopeID")); // Add EnvelopeID to the flattened output
                }
                System.out.println(flattenedRow);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}