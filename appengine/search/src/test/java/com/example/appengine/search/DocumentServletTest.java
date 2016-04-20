package com.example.appengine.search;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.appengine.api.search.Document;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DocumentServletTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private DocumentServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new DocumentServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_successfulyInvoked() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);
    String content = responseWriter.toString();
    assertThat(content)
        .named("DocumentServlet response: coverLetter")
        .contains("coverLetter: CoverLetter");
    assertThat(content)
        .named("DocumentServlet response: resume")
        .contains("resume: <html></html>");
    assertThat(content)
        .named("DocumentServlet response: fullName")
        .contains("fullName: Foo Bar");
    assertThat(content)
        .named("DocumentServlet response: submissionDate")
        .contains("submissionDate: ");
  }

  @Test
  public void createDocument_withSignedInUser() throws Exception {
    String email = "tmatsuo@example.com";
    String authDomain = "example.com";
    helper.setEnvEmail(email);
    helper.setEnvAuthDomain(authDomain);
    helper.setEnvIsLoggedIn(true);
    Document doc = servletUnderTest.createDocument();
    assertThat(doc.getOnlyField("content").getText())
        .named("content")
        .contains("the rain in spain");
    assertThat(doc.getOnlyField("email").getText())
        .named("email")
        .isEqualTo(email);
  }

  @Test
  public void createDocument_withoutSignedIn() throws Exception {
    helper.setEnvIsLoggedIn(false);
    Document doc = servletUnderTest.createDocument();
    assertThat(doc.getOnlyField("content").getText())
        .named("content")
        .contains("the rain in spain");
    assertThat(doc.getOnlyField("email").getText())
        .named("email")
        .isEmpty();
  }
}