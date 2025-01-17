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
	public void executeQuery(Properties props, String client_id, DemoGraphicsEntity phqe) {
		// Database connection details


		// SQL query
		String query = """
				SELECT TOP 1
				ms.staff_id,
				ms.full_name,
				ms.current_credential,
				p.last_name,
				p.first_name,
				o.name
				FROM 
				person p
				LEFT JOIN 
				client_program cp
				ON 
				cp.client_id = p.person_id
				LEFT JOIN 
				mv_staff ms
				ON 
				cp.staff_id = ms.staff_id
				LEFT JOIN 
				organization o
				ON 
				o.organization_id = p.organization_id
				WHERE 
				p.person_id= """ + client_id + 
				"order by cp.created_date asc"
				
				
				
				;


		try (

				PreparedStatement preparedStatement = this. connection.prepareStatement(query);
				ResultSet resultSet = preparedStatement.executeQuery()
				) {
			System.out.println("Connected to the database!");
			while (resultSet.next()) {
				//int staffId = resultSet.getInt("staff_id");
				
				String fullName = resultSet.getString("full_name");
				//String currentCredential = resultSet.getString("current_credential");
				//phqe.setStaffName(fullName);
				phqe.setClientName(resultSet.getString("last_name") + "," + resultSet.getString("first_name")  );
				
				//phqe.setCredentials(currentCredential);
				// we will take the organization from the properties file
				//phqe.setOrganization(props.getProperty("organization"));


				// Print results
				System.out.printf("Full Name: %s",
					 fullName);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


