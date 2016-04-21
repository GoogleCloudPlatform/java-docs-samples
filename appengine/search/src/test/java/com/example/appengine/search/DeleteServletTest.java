package com.example.appengine.search;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

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

public class DeleteServletTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper();

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  private StringWriter responseWriter;
  private DeleteServlet servletUnderTest;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();

    // Set up a fake HTTP response.
    responseWriter = new StringWriter();
    when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

    servletUnderTest = new DeleteServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void doGet_successfulyInvoked() throws Exception {
    servletUnderTest.doGet(mockRequest, mockResponse);
    assertThat(responseWriter.toString())
        .named("DeleteServlet response")
        .contains("Deleted documents.");
  }
}