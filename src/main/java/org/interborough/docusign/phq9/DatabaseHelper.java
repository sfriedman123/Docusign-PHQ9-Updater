package org.interborough.docusign.phq9;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DatabaseHelper {



	Connection connection;

	private String jdbcURL;
	private String dbUser;
	private String DB_PASSWORD;
	private String organization;
	private String masterChildPHQ9Query =	"SELECT LTRIM(RTRIM(" +
			"CASE " +
			"    WHEN CHARINDEX(' ', m.Staff_Name) > 0 THEN " +
			"        SUBSTRING(m.Staff_Name, CHARINDEX(' ', m.Staff_Name) + 1, LEN(m.Staff_Name)) " +
			"        + ', ' + " +
			"        SUBSTRING(m.Staff_Name, 1, CHARINDEX(' ', m.Staff_Name) - 1) " +
			"    ELSE m.Staff_Name " +
			"END)) AS Formatted_Staff_Name, " +
			"m.Credentials, " +
			"r.Client_ID, " +
			"m.Organization, " +
			"r.PHQ9_Date, " +
			"r.PHQ9_Score " +
			"FROM docusign_phq9_master m " +
			"JOIN Docusign_PHQ9_Results r " +
			"    ON m.Client_ID = r.Client_ID " +
			"WHERE m.Organization = 'Flatbush' " +
			"    AND m.Staff_Name IS NOT NULL " +
			"ORDER BY Formatted_Staff_Name ASC;";


	private String masterChildPHQ9Query2 = "select  mv_staff.last_name + ', ' + mv_staff.first_name as staffName, mv_staff.current_credential, "
			+ " m.Client_ID, m.Client_Name, m.Organization, r.PHQ9_Date, r.PHQ9_Score, r.EnvelopeID  \r\n"
			+ "from docusign_phq9_master m "
			+ " join "
			+ " Docusign_PHQ9_Results r"
			+ " on "
			+ " m.Client_ID=r.Client_ID "
			+ " join "
			+ " person "
			+ " on  "
			+ " m.Client_ID=person.person_id "
			+ " join "
			+ " mv_staff "
			+ " on mv_Staff.staff_id =person.created_by "
			+ " where organization='Flatbush'"

			+ " order by StaffName, PHQ9_Date";



	public DatabaseHelper(Properties prop) throws SQLException {
		// TODO Auto-generated constructor stub
		jdbcURL = prop.getProperty("spring.datasource.url");
		dbUser =  prop.getProperty("spring.datasource.username");
		DB_PASSWORD =  prop.getProperty("spring.datasource.password");
		connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);

	}

	public void insertIntoDocusignPhq9Master(PHQEntity staffDetails) {
		String insertQuery = "INSERT INTO docusign_phq9_master (staff_name, credentials, client_id, client_name, organization) " +
				"VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);
				PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

			// Set the values for the prepared statement
			preparedStatement.setString(1, staffDetails.getStaffName());
			preparedStatement.setString(2, staffDetails.getCredentials());

			preparedStatement.setString(3, staffDetails.getClientId());
			preparedStatement.setString(4, staffDetails.getClientName());
			preparedStatement.setString(5, staffDetails.getOrganization());
			System.out.println(staffDetails);

			// Execute the insert statement
			int rowsInserted = preparedStatement.executeUpdate();

			if (rowsInserted > 0) {
				System.out.println("A new record was inserted successfully!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertIntoPhqResults(PHQMany phqDetails) {
		String insertQuery = "INSERT INTO docusign_phq9_results (PHQ9_date, envelopeID, client_id, phq9_score, isbulk) " +
				"VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);
				PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

			// Set the values for the prepared statement
			preparedStatement.setDate(1, phqDetails.getSignedDate());
			preparedStatement.setString(2, phqDetails.getEnvelopeId());
			preparedStatement.setString(3, phqDetails.getClientId());
			preparedStatement.setInt(4, phqDetails.getPhqScore());
			preparedStatement.setBoolean (5, phqDetails.isBulk());


			// Execute the insert statement
			int rowsInserted = preparedStatement.executeUpdate();

			if (rowsInserted > 0) {
				System.out.println("A new record was inserted successfully!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if the given client ID exists in the docusign_phq9_master table.
	 *
	 * @param clientId The client ID to check.
	 * @return True if the client ID exists, otherwise false.
	 */
	public boolean doesClientIdExist(String clientId) {
		String checkQuery = "SELECT COUNT(*) FROM docusign_phq9_master WHERE client_id = ?";

		try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);
				PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {

			// Set the client_id parameter in the prepared statement
			preparedStatement.setString(1, clientId);

			// Execute the query and retrieve the result
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				int count = resultSet.getInt(1);
				return count > 0; // Return true if the count is greater than 0
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return false if an exception occurs or no result is found
		return false;
	}

	/**
	 * Checks if the given envelope ID exists in the PHQ9_Results table.
	 *
	 * @param envelopeId The envelope ID to check.
	 * @return True if the envelope ID exists, otherwise false.
	 */
	public boolean doesEnvelopeIdExist(String envelopeId, String clientId) {
		String checkQuery = "SELECT COUNT(*) FROM Docusign_PHQ9_Results WHERE EnvelopeID = ? AND Client_ID= ?";

		try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);
				PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {

			// Set the envelopeId parameter in the prepared statement
			preparedStatement.setString(1, envelopeId);
			preparedStatement.setString(2, clientId);

			// Execute the query and retrieve the result
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				int count = resultSet.getInt(1);
				return count > 0; // Return true if the count is greater than 0
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return false if an exception occurs or no result is found
		return false;

	}

	public  void flattenPHQ9Results(String query) throws SQLException, IOException {

		Connection connection = DriverManager.getConnection(this.jdbcURL, this.dbUser, this.DB_PASSWORD);
		Statement stmt = connection.createStatement();
		System.out.println(query);
		ResultSet rs = stmt.executeQuery(query);

		// LinkedHashMap to maintain insertion order for Client_IDs
		Map<String, Map<String, Object>> clientDataMap = new LinkedHashMap<>();

		// Store unique PHQ9_Date and PHQ9_Score count for dynamic columns
		Map<String, Integer> dateScoreCountMap = new HashMap<>();

		// Process ResultSet
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		FileWriter csvWriter = new FileWriter("c:/temp/abc.csv");

		while (rs.next()) {
			String clientId = rs.getString("Client_ID");
			if (clientId.trim().equals("159611"))
			{
				System.out.println("stop here");
			}



			clientDataMap.putIfAbsent(clientId, new LinkedHashMap<>());

			// Add all fields except PHQ9_Date and PHQ9_Score to the map
			for (int i = 1; i <= columnCount; i++) {
				String columnName = metaData.getColumnLabel(i);
				if (!columnName.equals("PHQ9_Date") && !columnName.equals("PHQ9_Score")) {
					clientDataMap.get(clientId).putIfAbsent(columnName, rs.getObject(columnName));
				}
			}

			// Add PHQ9_Date and PHQ9_Score with dynamic suffixes
			int count = dateScoreCountMap.getOrDefault(clientId, 0) + 1;
			dateScoreCountMap.put(clientId, count);
			clientDataMap.get(clientId).put("PHQ9_Date_" + count, rs.getString("PHQ9_Date"));
			clientDataMap.get(clientId).put("PHQ9_Score_" + count, rs.getString("PHQ9_Score"));
		}

		// Write to CSV
		// Step 1: Generate headers
		Set<String> headers = new LinkedHashSet<>();
		clientDataMap.values().forEach(record -> headers.addAll(record.keySet()));


		csvWriter.append(String.join(";", headers));
		csvWriter.append("\n");

		// Step 2: Write rows
		for (Map<String, Object> record : clientDataMap.values()) {
			List<String> row = new ArrayList<>();
			for (String header : headers) {
				Object value = record.getOrDefault(header, ""); // Default to "" if key is missing
				row.add(value != null ? value.toString() : ""); // Ensure null-safe conversion to String
			}
			csvWriter.append(String.join(";", row));
			csvWriter.append("\n");
		}

		System.out.println("CSV file generated successfully at: ");

	}


	public static void main(String[] args) throws SQLException, IOException {
		Properties prop = new Properties();

		FileInputStream fis = null;

		String configFilePath = args[0];

		fis = new FileInputStream(configFilePath);

		prop.load(fis);

		String appFilePath = args[1];
		Properties applicationProps = new Properties();

		fis = new FileInputStream(appFilePath);

		applicationProps.load(fis);

		fis = new FileInputStream(configFilePath);
		prop.load(fis);
		DatabaseHelper dbHelper = new DatabaseHelper(prop);
		dbHelper.organization = prop.getProperty("organization");

		dbHelper.flattenPHQ9Results(dbHelper.masterChildPHQ9Query2);




	}
}
