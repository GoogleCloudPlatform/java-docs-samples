package com.example.appengine.pubsub;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class PubSubHomeTest {
  private static final String FAKE_URL = "fakeurl.google";
  private static final String KIND = "pushed_messages";
  private static final String KEY = "message_list";
  private static final String FIELD = "messages";
  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private PubSubHome servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    //  Set up some fake HTTP requests
    when(mockRequest.getRequestURI()).thenReturn(FAKE_URL);

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    // Create an instance of the PubSubHome servlet
    servletUnderTest = new PubSubHome();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void doGet_OneMessageIT() throws Exception {
    LinkedList<String> messageList = new LinkedList<>();
    messageList.add("Hello, World!");

    // clear Message list
    clearMessageList();

    // Add messages to local Datastore
    addMessages(messageList);

    // Insert messageList..
    servletUnderTest.doGet(mockRequest, mockResponse);

    // We expect our hello world response.
    assertThat(responseWriter.toString())
        .named("Send a PubSub Message")
        .contains(generateHtmlWithMessageList(messageList));

    // clean up
    clearMessageList();
  }

  @Test
  public void doGet_MultipleMessagesIT() throws Exception {
    LinkedList<String> messageList = new LinkedList<>();
    messageList.add("Hello, World!");
    messageList.add("Hello, World!");

    // clear Message list
    clearMessageList();

    // Add messages to local Datastore
    addMessages(messageList);

    //Insert messageList
    servletUnderTest.doGet(mockRequest, mockResponse);

    // Expect two Hello, World! messages
    assertThat(responseWriter.toString())
        .named("Send a PubSub Message")
        .contains(generateHtmlWithMessageList(messageList));

    // clean up
    clearMessageList();
  }

  private void clearMessageList() {
    Datastore datastore = DatastoreOptions.getDefaultInstance()
        .getService();

    // Clear all message
    Key pushedMessages = datastore.newKeyFactory().setKind(KIND).newKey(KEY);
    datastore.delete(pushedMessages);
  }

  private void addMessages(LinkedList<String> messageList) {
    Gson gson = new Gson();
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Key messages = datastore.newKeyFactory().setKind(KIND).newKey(KEY);

    // Create an entry with a list of messages
    Entity entity = Entity.newBuilder(messages)
        .set(FIELD, gson.toJson(messageList))
        .build();

    datastore.put(entity);
  }

  private String generateHtmlWithMessageList(LinkedList<String> messageList) {
    String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
        + "<html>\n"
        + "<head>\n"
        + "<title>Send a PubSub Message</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "Received Messages:<br />\n";

    for (String message : messageList) {
      html += message + "<br />\n";
    }

    // Add Form to publish a new message
    html += "<form action=\"publish\" method=\"POST\">\n"
        + "<label for=\"payload\">Message:</label>\n"
        + "<input id=\"payload\" type=\"input\"  name=\"payload\" />\n"
        + "<input id=\"submit\"  type=\"submit\" value=\"Send\" />\n"
        + "</form>\n"
        + "</body>\n"
        + "</html>\n";

    return html;
  }
}