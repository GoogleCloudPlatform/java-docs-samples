package com.example.managedvms.pubsub;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "main", value = "/")
public class PubSubHome extends HttpServlet{
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
    // Prepare to present list
    resp.setContentType("text/html");
    PrintWriter out = resp.getWriter();
    out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<title>Send a PubSub Message</title>\n"
        + "</head>\n"
        + "<body>");

    // Get Messages
    ArrayList<String> messageList = getMessages();

    // Display received messages
    out.println("Received Messages:<br />");
    for (String message : messageList) {
        out.printf("%s<br />\n", message);
    }

    // Add Form to publish a new message
    out.println("<form action=\"publish\" method=\"POST\">\n"
        + "<label for=\"payload\">Message:</label>\n"
        + "<input id=\"payload\" type=\"input\"  name=\"payload\" />\n"
        + "<input id=\"submit\"  type=\"submit\" value=\"Send\" />\n"
        + "</form>");

    // Finish HTML Response
    out.println("</body>\n"
        + "</html>");
  }

  private ArrayList<String> getMessages() {
    // Get Message saved in Datastore
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query = Query.newEntityQueryBuilder()
        .setKind("pushed_messages")
        .setLimit(1)
        .build();
    QueryResults<Entity> results = datastore.run(query);

    // Convert stored JSON into ArrayList<String>
    ArrayList<String> messageList = new ArrayList<>();
    Gson gson = new Gson();
    if (results.hasNext()) {
      Entity entity = results.next();
      messageList = gson.fromJson(entity.getString("messages"),
          messageList.getClass());
    }

    return messageList;
  }
}
