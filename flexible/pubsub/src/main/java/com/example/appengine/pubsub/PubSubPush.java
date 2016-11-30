package com.example.appengine.pubsub;

import static java.lang.Thread.sleep;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Map;
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
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    final String apiToken = System.getenv("PUBSUB_VERIFICATION_TOKEN");

    try {
      // message = JSON.parse request.body.read
      JsonReader jsonReader = new JsonReader(req.getReader());

      // Token doesn't match apiToken
      if (req.getParameter("token").compareTo(apiToken) != 0) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      Map<String, Map<String, String>> responseBody = new Gson()
          .fromJson(jsonReader, Map.class);
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



  public void saveMessage(String message) {
    new Thread(new Runnable() {
      private final long maxSleepTime = 30000; // 30 seconds

      @Override
      public void run() {
        try {
          // set starting sleepTime
          long sleepTime = 1000;
          // Prepare message list if it's empty
          while (sleepTime < maxSleepTime) {
            if (createMessageList()) {
              break;
            }
            sleep(sleepTime);
            // Exponential backoff
            sleepTime *= 2;
          }

          // reset starting sleepTime
          sleepTime = 1000;
          // Attempt to save message
          while (sleepTime < maxSleepTime) {
            if (trySaveMessage(message)) {
              break;
            }
            sleep(sleepTime);
          }
        } catch (InterruptedException ie) {
          System.err.println(ie);
        }
      }
    }).start();
  }

  private boolean createMessageList() {
    // Start a new transaction
    Transaction transaction = datastoreService.newTransaction();

    // Create a Gson object to serialize messages LinkedList as a JSON string
    Gson gson = new Gson();

    // Transaction flag
    boolean messagesFound = false;

    try {
      // Create a keyfactory for entries of kind pushed_messages
      KeyFactory keyFactory = datastoreService.newKeyFactory()
          .setKind("pushed_messages");

      // Lookup message_list
      Key key = keyFactory.newKey("message_list");
      Entity entity = transaction.get(key);

      // Entity doesn't exist so let's create it!
      if (entity == null) {
        LinkedList<String> messages = new LinkedList<>();
        entity = Entity.newBuilder(key)
            .set("messages", gson.toJson(messages))
            .build();
        transaction.put(entity);
        transaction.commit();
      } else {
        transaction.rollback();
      }
      messagesFound = true;
    } finally {
      if (transaction.isActive()) {
        // we don't have an entry yet transaction failed
        transaction.rollback();
        messagesFound = false;
      }
    }
    // we have an entry to work with
    return messagesFound;
  }

  private boolean trySaveMessage(String message) {
    // Start a new transaction
    Transaction transaction = datastoreService.newTransaction();

    // Create a Gson object to parse and serialize an LinkedList
    Gson gson = new Gson();

    // Transaction flag
    boolean messagesSaved = false;

    try {
      // Lookup pushed_messages
      KeyFactory keyFactory = datastoreService.newKeyFactory()
          .setKind("pushed_messages");
      Key key = keyFactory.newKey("message_list");
      Entity entity = transaction.get(key);

      // Parse JSON into an LinkedList
      LinkedList<String> messages = gson.fromJson(entity.getString("messages"),
          new TypeToken<LinkedList<String>>() {
          }.getType());

      // Add new message and save updated entry
      messages.add(message);
      entity = Entity.newBuilder(entity)
          .set("messages", gson.toJson(messages))
          .build();
      transaction.update(entity);
      transaction.commit();
      messagesSaved = true;
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
        messagesSaved = false;
      }
    }
    // It saved the new entry!
    return messagesSaved;
  }
}

