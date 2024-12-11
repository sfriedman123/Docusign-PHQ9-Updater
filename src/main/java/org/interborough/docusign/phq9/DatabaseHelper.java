package org.interborough.docusign.phq9;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseHelper {
	
	
	
	 Connection connection;
	private String jdbcURL;
	private String dbUser;
	private String DB_PASSWORD;

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
        String insertQuery = "INSERT INTO phq9_results (PHQ9_date, envelopeID, client_id, phq9_score) " +
                             "VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            // Set the values for the prepared statement
            preparedStatement.setDate(1, phqDetails.getSignedDate());
            preparedStatement.setString(2, phqDetails.getEnvelopeId());
            preparedStatement.setString(3, phqDetails.getClientId());
            preparedStatement.setInt(4, phqDetails.getPhqScore());

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
    public boolean doesEnvelopeIdExist(String envelopeId) {
        String checkQuery = "SELECT COUNT(*) FROM PHQ9_Results WHERE EnvelopeID = ?";
        
        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(checkQuery)) {

            // Set the envelopeId parameter in the prepared statement
            preparedStatement.setString(1, envelopeId);

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




    public static void main(String[] args) {
        // Example usage
     


    }
}
