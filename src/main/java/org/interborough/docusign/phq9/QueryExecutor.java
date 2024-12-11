package org.interborough.docusign.phq9;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;



public class QueryExecutor {
	
	 Connection connection;

    public QueryExecutor(Properties prop) throws SQLException {
		// TODO Auto-generated constructor stub
    	 String jdbcURL = prop.getProperty("spring.datasource.url");
         String dbUser =  prop.getProperty("spring.datasource.username");
         String dbPassword =  prop.getProperty("spring.datasource.password");
         connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);

	}

	

	// Method to execute the query and display results
    public void executeQuery(Properties props, String client_id, PHQEntity phqe) {
        // Database connection details
      

        // SQL query
        String query = """
                SELECT 
                    ms.staff_id,
                    ms.full_name,
                    ms.current_credential,
                    p.last_name,
	                p.first_name,
	                o.name
                FROM 
                    client_program cp
                JOIN 
                    mv_staff ms
                ON 
                    cp.staff_id = ms.staff_id
                JOIN
        			person p 
        		ON  
        		    cp.client_id=p.person_id
        		JOIN
        		    organization o 
        		ON
        		 o.organization_id=p.organization_id
        		
                WHERE 
                    cp.client_id =""" + client_id + ";";
              

        try (
           
            PreparedStatement preparedStatement = this. connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            System.out.println("Connected to the database!");
            while (resultSet.next()) {
                int staffId = resultSet.getInt("staff_id");
                String fullName = resultSet.getString("full_name");
                String currentCredential = resultSet.getString("current_credential");
                phqe.setStaffName(fullName);
                phqe.setClientName(resultSet.getString("last_name") + "," + resultSet.getString("first_name")  );
                phqe.setCredentials(currentCredential);
                phqe.setOrganization(resultSet.getString("name"));

                
                // Print results
                System.out.printf("Staff ID: %d, Full Name: %s, Current Credential: %s%n",
                        staffId, fullName, currentCredential);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

       
