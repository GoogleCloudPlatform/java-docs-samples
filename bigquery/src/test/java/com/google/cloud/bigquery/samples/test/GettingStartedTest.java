package com.google.cloud.bigquery.samples.test;

import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.cloud.bigquery.samples.GettingStarted;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test;

import static com.jcabi.matchers.RegexMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;


/**
 * Test for GettingStarted.java
 */
public class GettingStartedTest extends BigquerySampleTest {
  private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
  private static final PrintStream REAL_OUT = System.out;
  private static final PrintStream REAL_ERR = System.err;

  public GettingStartedTest() throws FileNotFoundException {
    super();
  }

  @Before
  public void setUp() {
    System.setOut(new PrintStream(stdout));
    System.setErr(new PrintStream(stderr));
  }

  @After
  public void tearDown() {
    System.setOut(REAL_OUT);
    System.setErr(REAL_ERR);
  }

  @Test
  public void testSyncQuery() throws IOException {
    GettingStarted.main(new String[] { CONSTANTS.getProjectId() });
    String out = stdout.toString();
    assertThat(out, containsPattern("Query Results:"));
    assertThat(out, containsString("hamlet"));
  }
}
