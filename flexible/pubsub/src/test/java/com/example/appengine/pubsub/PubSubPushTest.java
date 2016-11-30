package com.example.appengine.pubsub;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class PubSubPushTest {
  private static final String FAKE_URL = "fakeurl.google";
  private static final String ENTRY_KEY = "message_list";
  private static final String ENTRY_FIELD = "messages";
  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private PubSubPush servletUnderTest;
  private String entryKind;

  @Before
  public void setUp() throws Exception {
    // Get environment variables
    entryKind = System.getenv("DATASTORE_TEST_ENTRY_KIND");

    // Initialize Mockito
    MockitoAnnotations.initMocks(this);

    // Set up some fake HTTP requests
    when(mockRequest.getRequestURI()).thenReturn(FAKE_URL);

    // Add token
    String token = System.getenv("PUBSUB_VERIFICATION_TOKEN");
    when(mockRequest.getParameter("token")).thenReturn(token);

    // Set up a fake HTTP response
    responseWriter = new StringWriter();

    // Create an instance of the PubSubHome servlet
    servletUnderTest = new PubSubPush();
    servletUnderTest.setTimeoutMilliSeconds(30000);
    servletUnderTest.setEntryKind(entryKind);
  }

  @Test
  public void doPostSingleMessageIT() throws Exception {
    // Clear all messages
    clearMessageList();

    // Mock reader for request
    String message = "Hello, World!";
    BufferedReader mockReader = generatePostMessage(message);
    when(mockRequest.getReader()).thenReturn(mockReader);

    // Expected output
    final List<String> expectedMessageList = new LinkedList<>();
    expectedMessageList.add(message);

    // Do POST
    servletUnderTest.doPost(mockRequest, mockResponse);

    // Test that Message exists in Datastore
    List<String> messages = getMessages();
    assertThat(messages).isEqualTo(expectedMessageList);

    // Clean up
    clearMessageList();
  }

  private BufferedReader generatePostMessage(String message) {
    String encodedPayload = Base64.getEncoder().encodeToString(
        message.getBytes());
    Map<String, Map<String, String>> messageObject = new HashMap<>();
    Map<String, String> dataObject = new HashMap<>();
    messageObject.put("message", dataObject);
    dataObject.put("data", encodedPayload);

    Gson gson = new Gson();
    StringReader reader = new StringReader(gson.toJson(messageObject,
        messageObject.getClass()));

    return (new BufferedReader(reader));
  }

  private void clearMessageList() {
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    // Clear all message
    Key pushedMessages = datastore.newKeyFactory().setKind(entryKind)
        .newKey(ENTRY_KEY);
    datastore.delete(pushedMessages);
  }

  private List<String> getMessages() {
    Gson gson = new Gson();
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query = Query.newEntityQueryBuilder().setKind(entryKind)
        .setLimit(1).build();

    Iterator<Entity> entities = datastore.run(query);
    List<String> messages = new LinkedList<>();
    if (entities.hasNext()) {
      Entity entity = entities.next();
      messages = gson.fromJson(entity.getString(ENTRY_FIELD),
          messages.getClass());
    }

    return messages;
  }
}