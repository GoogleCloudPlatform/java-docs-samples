package com.example.appengine.taskqueue.push;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link Worker}.
 */
@RunWith(JUnit4.class)
public class WorkerTest {
  private static final String FAKE_KEY_VALUE = "KEY"; 

  // To capture and restore stderr
  private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
  private static final PrintStream REAL_ERR = System.err;

  @Mock
  private HttpServletRequest mockRequest;
  @Mock
  private HttpServletResponse mockResponse;
  private Worker servletUnderTest;

  @Before
  public void setUp() throws Exception {
    //  Capture stderr to examine messages written to it
    System.setErr(new PrintStream(stderr));

    MockitoAnnotations.initMocks(this);

    when(mockRequest.getParameter("key")).thenReturn(FAKE_KEY_VALUE);

    servletUnderTest = new Worker();
  }

  @After
  public void tearDown() {
    //  Restore stderr
    System.setErr(WorkerTest.REAL_ERR);
  }

  @Test
  public void doPost_writesResponse() throws Exception {
    servletUnderTest.doPost(mockRequest, mockResponse);

    String out = stderr.toString();
    // We expect a log message to be created
    // with the following message.
    assertThat(out).contains("Worker is processing " + FAKE_KEY_VALUE);

  }
}
