package com.example.managedvms.pubsub;

import com.google.cloud.datastore.BlobValue;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Value;
import com.google.cloud.datastore.ValueBuilder;
import com.google.cloud.pubsub.Message;
import com.google.cloud.pubsub.PubSub;
import com.google.cloud.pubsub.PubSubOptions;
import com.google.cloud.pubsub.Topic;
import com.google.cloud.pubsub.TopicInfo;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "PubSubPush", value = "/pubsub/push")
public class PubSubPush extends HttpServlet {
  private Datastore datastoreService = null;

  @Override
  public void init(ServletConfig config) throws ServletException {
    // Initialize
    datastoreService = DatastoreOptions.getDefaultInstance().getService();

    // Prepare message list if it's empty
    while (true) {
      if (createMessageList()) {
        break;
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    final String apiToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");
    final String topicName = System.getenv("PUBSUB_TOPIC");

    try {
      // message = JSON.parse request.body.read
      JsonReader jsonReader = new JsonReader(req.getReader());

      // Token doesn't match apiToken
      if (req.getParameter("token").compareTo(apiToken) != 0) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      Map<String, Map<String, String>> responseBody = new Gson().fromJson(jsonReader, Map.class);
      final String data = responseBody.get("message").get("data");

      // Ugly...
      byte[] interm = Base64.getDecoder().decode(data);
      String payload = new String(interm, StandardCharsets.UTF_8);

      // Save payload to be displayed later
      saveMessage(payload);
    } catch (JsonParseException error) {
      resp.getWriter().print(error.toString());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private boolean createMessageList() {
    // Start a new transaction
    Transaction transaction = datastoreService.newTransaction();

    // Create a Gson object to serialize messages ArrayList as a JSON string
    Gson gson = new Gson();

    try {
      // Create a keyfactory for entries of kind pushed_messages
      KeyFactory keyFactory = datastoreService.newKeyFactory().setKind("pushed_messages");

      // Lookup message_list
      Key key = keyFactory.newKey("message_list");
      Entity entity = transaction.get(key);

      // Entity doesn't exist so let's create it!
      if (entity == null) {
        ArrayList<String> messages = new ArrayList<>();
        entity = Entity.newBuilder(key)
            .set("messages", gson.toJson(messages))
            .build();
        transaction.put(entity);
        transaction.commit();
      } else {
        return true;
      }
    } catch (Exception err) {
      System.err.println(""+err);
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
        // we don't have an entry yet transaction failed
        return false;
      }
    }
    // we have an entry to work with
    return true;
  }

  private void saveMessage(String message) {
    // Attempt to save message
    while (true) {
      if (trySaveMessage(message)) {
        break;
      }
    }
  }

  private boolean trySaveMessage(String message) {
    // Start a new transaction
    Transaction transaction = datastoreService.newTransaction();

    // Create a Gson object to parse and serialize an ArrayList
    Gson gson = new Gson();

    try {
      // Lookup pushed_messages
      KeyFactory keyFactory = datastoreService.newKeyFactory().setKind("pushed_messages");
      Key key = keyFactory.newKey("message_list");
      Entity entity = transaction.get(key);

      // Parse JSON into an ArrayList
      ArrayList<String> messages = null;
      messages = gson.fromJson(entity.getString("messages"), messages.getClass());

      // Add new message and save updated entry
      messages.add(message);
      entity = Entity.newBuilder(entity)
          .set("messages", gson.toJson(messages))
          .build();
      transaction.update(entity);
      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
        // didn't work out try again
        return false;
      }
    }
    // It saved the new entry!
    return true;
  }
}

