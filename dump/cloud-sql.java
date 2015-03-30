// [START import]
import java.io.*;
import javax.servlet.http.*;
import java.sql.*;
// [END register_the_JDBC_driver]

// [START connect_and_post_to_your_database_1]
import java.io.*;
import java.sql.*;
import javax.servlet.http.*;
import com.google.appengine.api.utils.SystemProperty;

public class GuestbookServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String url = null;
		try {
			if (SystemProperty.environment.value() ==
					SystemProperty.Environment.Value.Production) {
				// Load the class that provides the new "jdbc:google:mysql://" prefix.
				Class.forName("com.mysql.jdbc.GoogleDriver");
				url = "jdbc:google:mysql://your-project-id:your-instance-name/guestbook?user=root";
			} else {
				// Local MySQL instance to use during development.
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:mysql://127.0.0.1:3306/guestbook?user=root";

				// Alternatively, connect to a Google Cloud SQL instance using:
				// jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		PrintWriter out = resp.getWriter();
		try {
			Connection conn = DriverManager.getConnection(url);
			try {
				String fname = req.getParameter("fname");
				String content = req.getParameter("content");
				if (fname == "" || content == "") {
					out.println(
							"<html><head></head><body>You are missing either a message or a name! Try again! " +
							"Redirecting in 3 seconds...</body></html>");
				} else {
					String statement = "INSERT INTO entries (guestName, content) VALUES( ? , ? )";
					PreparedStatement stmt = conn.prepareStatement(statement);
					stmt.setString(1, fname);
					stmt.setString(2, content);
					int success = 2;
					success = stmt.executeUpdate();
					if (success == 1) {
						out.println(
								"<html><head></head><body>Success! Redirecting in 3 seconds...</body></html>");
					} else if (success == 0) {
						out.println(
								"<html><head></head><body>Failure! Please try again! " +
								"Redirecting in 3 seconds...</body></html>");
					}
				}
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		resp.setHeader("Refresh", "3; url=/guestbook.jsp");
	}
}
// [END connect_and_post_to_your_database_1]

// [START connect_and_post_to_your_database_2]
Connection conn = DriverManager.getConnection(
		"jdbc:google:mysql://your-project-id:your-instance-name/database",
		"user", "password");
// [END connect_and_post_to_your_database_2]

// [START create_your_webform]
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.google.appengine.api.utils.SystemProperty" %>

<html>
<body>

<%
String url = null;
if (SystemProperty.environment.value() ==
		SystemProperty.Environment.Value.Production) {
	// Load the class that provides the new "jdbc:google:mysql://" prefix.
	Class.forName("com.mysql.jdbc.GoogleDriver");
	url = "jdbc:google:mysql://your-project-id:your-instance-name/guestbook?user=root";
} else {
	// Local MySQL instance to use during development.
	Class.forName("com.mysql.jdbc.Driver");
	url = "jdbc:mysql://127.0.0.1:3306/guestbook?user=root";
}

Connection conn = DriverManager.getConnection(url);
ResultSet rs = conn.createStatement().executeQuery(
		"SELECT guestName, content, entryID FROM entries");
%>

<table style="border: 1px solid black">
<tbody>
<tr>
<th width="35%" style="background-color: #CCFFCC; margin: 5px">Name</th>
<th style="background-color: #CCFFCC; margin: 5px">Message</th>
<th style="background-color: #CCFFCC; margin: 5px">ID</th>
</tr>

<%
while (rs.next()) {
	String guestName = rs.getString("guestName");
	String content = rs.getString("content");
	int id = rs.getInt("entryID");
	%>
		<tr>
		<td><%= guestName %></td>
		<td><%= content %></td>
		<td><%= id %></td>
		</tr>
		<%
}
conn.close();
%>

</tbody>
</table>
<br />
No more messages!
<p><strong>Sign the guestbook!</strong></p>
<form action="/sign" method="post">
<div>First Name: <input type="text" name="fname"></input></div>
<div>Message:
<br /><textarea name="content" rows="3" cols="60"></textarea>
</div>
<div><input type="submit" value="Post Greeting" /></div>
<input type="hidden" name="guestbookName" />
</form>
</body>
</html>
// [END create_your_webform]

// [START map_your_servlet]
<?xml version="1.0" encoding="utf-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xmlns="http://java.sun.com/xml/ns/javaee"
xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" version="2.5">
<servlet>
<servlet-name>sign</servlet-name>
<servlet-class>guestbook.GuestbookServlet</servlet-class>
</servlet>
<servlet-mapping>
<servlet-name>sign</servlet-name>
<url-pattern>/sign</url-pattern>
</servlet-mapping>
<welcome-file-list>
<welcome-file>guestbook.jsp</welcome-file>
</welcome-file-list>
</web-app>
// [END map_your_servlet]

// [START enable_connector_j]
<?xml version="1.0" encoding="utf-8"?>
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
...
<use-google-connector-j>true</use-google-connector-j>
</appengine-web-app>
// [END enable_connector_j]

// [START persistence_pattern]
import java.util.Map;
import java.util.HashMap;
import com.google.appengine.api.utils.SystemProperty;

...
// Set the persistence driver and url based on environment, production or local.
Map<String, String> properties = new HashMap();
if (SystemProperty.environment.value() ==
		SystemProperty.Environment.Value.Production) {
	properties.put("javax.persistence.jdbc.driver",
			"com.mysql.jdbc.GoogleDriver");
	properties.put("javax.persistence.jdbc.url",
			"jdbc:google:mysql://your-project-id:your-instance-name/demo");
} else {
	properties.put("javax.persistence.jdbc.driver",
			"com.mysql.jdbc.Driver");
	properties.put("javax.persistence.jdbc.url",
			"jdbc:mysql://127.0.0.1:3306/demo");
}

// Create a EntityManager which will perform operations on the database.
EntityManagerFactory emf = Persistence.createEntityManagerFactory(
		"persistence-unit-name", propertiesMap);
...
// [END persistence_pattern]
