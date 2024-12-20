package org.interborough.docusign.phq9;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class PHQScore {


	public static int computeScore(String jsonData) {
		// Parse the input JSON string to get the radioGroupTabs array
		JSONArray radioGroupTabs = new JSONObject(jsonData).getJSONArray("radioGroupTabs");

		// Initialize the total score
		int totalScore = 0;

		// Define scores for each radio option (assuming each radio button has a score
		// associated with its value)
		Map<String, Integer> radioScores = new HashMap<>();
		radioScores.put("Radio1", 0); // Example: Radio1 has a score of 0
		radioScores.put("Radio2", 1); // Example: Radio2 has a score of 1
		radioScores.put("Radio3", 2); // Example: Radio3 has a score of 2
		radioScores.put("Radio4", 3); // Example: Radio4 has a score of 3

		// Iterate over each radio group
		// We want to skip the last set of radio buttons which has a question about
		// difficulty
		for (int i = 0; i < radioGroupTabs.length(); i++) {

			JSONObject radioGroup = radioGroupTabs.getJSONObject(i);

			if ("PHQ9Difficulty".equals(radioGroup.optString("groupName"))) {
				System.out.println("We don't want to process the difficulty radio score");
				continue;
			}

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

}
