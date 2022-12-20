/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package webrisk;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.protobuf.ByteString;
import com.google.webrisk.v1.CompressionType;
import com.google.webrisk.v1.ThreatType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private final PrintStream originalOut = System.out;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut.reset();
    System.setOut(originalOut);
  }

  @Test
  public void testComputeThreatListDiff() throws IOException {
    ComputeThreatListDiff.computeThreatDiffList(ThreatType.MALWARE, ByteString.EMPTY, 1024, 1024,
        CompressionType.RAW);
    assertThat(stdOut.toString()).contains("Obtained threat list diff.");
  }

  @Test
  public void testSearchHash() throws IOException, NoSuchAlgorithmException {
    String uri = "http://example.com";
    String encodedUri = Base64.getUrlEncoder().encodeToString(uri.getBytes(StandardCharsets.UTF_8));
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(encodedUri.getBytes(StandardCharsets.UTF_8));

    List<ThreatType> threatTypes = Arrays.asList(ThreatType.MALWARE, ThreatType.SOCIAL_ENGINEERING);

    SearchHashes.searchHash(ByteString.copyFrom(hash),
        threatTypes);
    assertThat(stdOut.toString()).contains("Completed searching threat hashes.");
  }

  @Test
  public void testSearchUriWithThreat() throws IOException {
    // The URL to be searched
    String uri = "http://testsafebrowsing.appspot.com/s/malware.html";
    ThreatType threatType = ThreatType.MALWARE;
    SearchUri.searchUri(uri, threatType);
    assertThat(stdOut.toString()).contains("The URL has the following threat:");
    assertThat(stdOut.toString()).contains(ThreatType.MALWARE.name());
  }

  @Test
  public void testSearchUriWithoutThreat() throws IOException {
    // The URL to be searched
    String uri = "http://testsafebrowsing.appspot.com/malware.html";
    ThreatType threatType = ThreatType.MALWARE;
    SearchUri.searchUri(uri, threatType);
    assertThat(stdOut.toString()).contains("The URL is safe!");
  }

  @Test
  public void testSubmitUri()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    String uri = "http://testsafebrowsing.appspot.com/s/malware.html";
    SubmitUri.submitUri(PROJECT_ID, uri);
    assertThat(stdOut.toString()).contains("Submission response: ");
  }

}
