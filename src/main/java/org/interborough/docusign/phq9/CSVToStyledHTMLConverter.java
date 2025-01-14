package org.interborough.docusign.phq9;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class CSVToStyledHTMLConverter {

    private String csvFilePath;
    private String htmlFilePath;
    private String header;

    // Constructor
    public CSVToStyledHTMLConverter(String csvFilePath, String htmlFilePath, String header) {
        this.csvFilePath = csvFilePath;
        this.htmlFilePath = htmlFilePath;
        this.header = header;
    }
    
    public String formatDate()
    {
    	LocalDate today = LocalDate.now();

    	// Subtract one day to get yesterday's date
    

    	// Format the date as a string
    	DateTimeFormatter todayFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Customize the format if needed
    	String yesterdayAsString = today.format(todayFormat);
		return yesterdayAsString;
    }
    
    private String getPhq9CssClass(int value) {
        if (value >= 0 && value <= 4) {
            return "phq9-0-4";
        } else if (value >= 5 && value <= 9) {
            return "phq9-5-9";
        } else if (value >= 10 && value <= 14) {
            return "phq9-10-14";
        } else if (value >= 15 && value <= 19) {
            return "phq9-15-19";
        } else if (value >= 20) {
            return "phq9-20";
        }
        return ""; // Default case (no styling)
    }


    // Method to convert CSV to a styled HTML table
    public void convert() {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
             FileWriter fw = new FileWriter(htmlFilePath)) {

            // Start the HTML document with basic CSS for styling
            fw.write("<html>\n<head>\n<title>CSV to HTML</title>\n");
            fw.write("<style>\n");
            fw.write("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f9; }\n");
            fw.write("table { border-collapse: collapse; width: 100%; margin-top: 20px; background-color: white; }\n");
            fw.write("th, td { border: 1px solid #303063; text-align: left; padding: 8px; }\n");
            fw.write("th { background-color: #303063; color: white; text-align: center; }\n");
            fw.write("tr:nth-child(even) { background-color: #f9f9f9; }\n");
            fw.write("tr:hover { background-color: #ddd; }\n");
            fw.write("td.numeric { text-align: right; }\n");
            fw.write("h4 { text-align: center; color: #333; margin-bottom: 20px; }\n");
            fw.write(".phq9-0-4 { background-color: #d9fdd9; } /* Light green */\n");
            fw.write(".phq9-5-9 { background-color: #ffff99; } /* Yellow */\n");
            fw.write(".phq9-10-14 { background-color: #ffcc66; } /* Orange */\n");
            fw.write(".phq9-15-19 { background-color: #ff9966; } /* Reddish orange */\n");
            fw.write(".phq9-20 { background-color: #ff6666; } /* Red */\n");
            fw.write("</style>\n</head>\n<body>\n");

            // Add dynamic header with the current date
            fw.write("<h4>" + this.header + " " + formatDate() + "</h4>\n");
            fw.write("<table>\n");

            String line;
            boolean isHeader = true;
            int phq9Index = -1;

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle CSV with quoted values

                // Remove empty columns from the end of the row
                columns = Arrays.stream(columns)
                                .filter(column -> !column.isEmpty()) // Filter out empty columns
                                .toArray(String[]::new);

                // Write table rows
                fw.write("<tr>");
                for (int i = 0; i < columns.length; i++) {
                    String column = columns[i].trim();

                    if (isHeader) {
                        fw.write("<th>" + column + "</th>");
                        if ("PHQ9_Score_1".equalsIgnoreCase(column)) {
                            phq9Index = i; // Remember the index of the PHQ9_Score column
                        }
                    } else {
                        if (i == phq9Index && isNumeric(column)) {
                            int value = Integer.parseInt(column);
                            String cssClass = getPhq9CssClass(value);
                            fw.write("<td class=\"" + cssClass + " numeric\">" + column + "</td>");
                        } else if (isNumeric(column)) {
                            fw.write("<td class=\"numeric\">" + column + "</td>");
                        } else {
                            fw.write("<td>" + column + "</td>");
                        }
                    }
                }
                fw.write("</tr>\n");

                isHeader = false; // Only the first row is treated as a header
            }

            // Close the HTML table and document
            fw.write("</table>\n</body>\n</html>");
            System.out.println("Styled HTML file with PHQ9 coloring created at: " + htmlFilePath);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str); // Check if the string can be parsed as a number
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // Main method for testing
    public static void main(String[] args) {
        // Define the input CSV file and output HTML file paths
        String csvFilePath = "c:\\temp\\output.csv";  // Replace with your actual CSV file path
        String htmlFilePath = "c:\\temp\\output.html"; // Replace with your desired output HTML path
        

        // Create an instance of the converter class
        CSVToStyledHTMLConverter converter = new CSVToStyledHTMLConverter(csvFilePath, htmlFilePath, "PHQ9 Report For ");

        // Perform the conversion
        converter.convert();
    }
}