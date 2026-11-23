/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.vision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for the SSRF / local-file-read guard in {@link VisionController}. These tests do not
 * call Cloud Vision or fetch any URL: the Vision template and resource loader are mocked so the
 * endpoint tests can assert that rejected URLs never reach them.
 */
@RunWith(JUnit4.class)
public class VisionControllerTest {

  private static final String VALID_URL =
      "https://storage.googleapis.com/cloud-samples-data/vision/text/screen.jpg";

  @Mock private CloudVisionTemplate cloudVisionTemplate;
  @Mock private ResourceLoader resourceLoader;
  @InjectMocks private VisionController controller;

  private AutoCloseable mocks;
  private MockMvc mvc;

  @Before
  public void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @After
  public void tearDown() throws Exception {
    mocks.close();
  }

  private static void assertRejected(String imageUrl) {
    try {
      VisionController.requireAllowedImageUrl(imageUrl);
      fail("Expected imageUrl to be rejected: " + imageUrl);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
    }
  }

  private static void assertAccepted(String imageUrl) {
    try {
      VisionController.requireAllowedImageUrl(imageUrl);
    } catch (ResponseStatusException e) {
      fail("Expected imageUrl to be accepted: " + imageUrl + " (" + e.getReason() + ")");
    }
  }

  // Accepted URLs.

  @Test
  public void requireAllowedImageUrl_acceptsHttpsStorageObject() {
    assertAccepted(VALID_URL);
  }

  @Test
  public void requireAllowedImageUrl_acceptsUppercaseSchemeAndHost() {
    assertAccepted("HTTPS://STORAGE.GOOGLEAPIS.COM/cloud-samples-data/vision/label/wakeupcat.jpg");
  }

  @Test
  public void requireAllowedImageUrl_acceptsObjectPathWithQueryString() {
    assertAccepted("https://storage.googleapis.com/my-bucket/dir/image%20name.png?generation=1");
  }

  // Missing or malformed input.

  @Test
  public void requireAllowedImageUrl_rejectsNullAndBlank() {
    assertRejected(null);
    assertRejected("");
    assertRejected("   ");
  }

  @Test
  public void requireAllowedImageUrl_rejectsMalformedUri() {
    assertRejected("not a url");
    assertRejected("https://storage.googleapis.com\\@evil.example/b/o");
    assertRejected(" https://storage.googleapis.com/b/o");
    assertRejected("https://storage.googleapis.com/b/o\n");
  }

  // Schemes and Spring ResourceLoader prefixes.

  @Test
  public void requireAllowedImageUrl_rejectsNonHttpsSchemes() {
    assertRejected("http://storage.googleapis.com/b/o.jpg");
    assertRejected("file:///etc/passwd");
    assertRejected("ftp://storage.googleapis.com/b/o.jpg");
    assertRejected("jar:https://storage.googleapis.com/b/o.jar!/x.jpg");
    assertRejected("gs://bucket/object.jpg");
  }

  @Test
  public void requireAllowedImageUrl_rejectsResourceLoaderLocations() {
    assertRejected("classpath:application.properties");
    assertRejected("/etc/passwd");
    assertRejected("https:/storage.googleapis.com/b/o");
    assertRejected("https:storage.googleapis.com/b/o");
  }

  // Host checks.

  @Test
  public void requireAllowedImageUrl_rejectsOtherHosts() {
    assertRejected("https://www.google.com/images/branding/googlelogo.png");
    assertRejected("https://169.254.169.254/computeMetadata/v1/");
    assertRejected("https://metadata.google.internal/computeMetadata/v1/");
    assertRejected("https://[::1]/x");
    assertRejected("https://[::ffff:169.254.169.254]/x");
    assertRejected("https://0xa9fea9fe/x");
  }

  @Test
  public void requireAllowedImageUrl_rejectsSubdomainAndSuffixSpoofing() {
    assertRejected("https://evil.storage.googleapis.com/b/o");
    assertRejected("https://evilstorage.googleapis.com/b/o");
    assertRejected("https://storage.googleapis.com.evil.example/b/o");
    assertRejected("https://storage.googleapis.com./b/o");
    assertRejected("https://storage%2egoogleapis.com/b/o");
  }

  @Test
  public void requireAllowedImageUrl_rejectsUnicodeCaseFoldingLookalikes() {
    // String.equalsIgnoreCase treats U+017F (long s) as 's', U+0131 (dotless i) as 'i' and
    // U+212A (Kelvin sign) as 'k'; these must still be rejected.
    assertRejected("https://ſtorage.googleapis.com/b/o");
    assertRejected("https://storage.googleapıs.com/b/o");
    assertRejected("https://storage.googKeapis.com/b/o");
  }

  @Test
  public void requireAllowedImageUrl_rejectsUserInfo() {
    assertRejected("https://storage.googleapis.com@evil.example/b/o");
    assertRejected("https://user:password@storage.googleapis.com/b/o");
    assertRejected("https://@storage.googleapis.com/b/o");
    assertRejected("https://evil.example#@storage.googleapis.com/b/o");
    assertRejected("https://evil.example?@storage.googleapis.com/b/o");
  }

  @Test
  public void requireAllowedImageUrl_rejectsExplicitPorts() {
    assertRejected("https://storage.googleapis.com:443/b/o");
    assertRejected("https://storage.googleapis.com:8080/b/o");
    assertRejected("https://storage.googleapis.com:abc/b/o");
  }

  // Path checks.

  @Test
  public void requireAllowedImageUrl_rejectsEmptyPath() {
    assertRejected("https://storage.googleapis.com");
    assertRejected("https://storage.googleapis.com/");
    assertRejected("https://storage.googleapis.com?x=1");
  }

  @Test
  public void requireAllowedImageUrl_rejectsDotDotSegments() {
    assertRejected("https://storage.googleapis.com/b/../../x");
    assertRejected("https://storage.googleapis.com/../computeMetadata");
  }

  // Endpoint behavior.

  @Test
  public void extractText_invalidUrl_returnsBadRequestWithoutLoadingResource() throws Exception {
    mvc.perform(get("/extractText").param("imageUrl", "file:///etc/passwd"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/extractText")).andExpect(status().isBadRequest());

    verifyNoInteractions(resourceLoader, cloudVisionTemplate);
  }

  @Test
  public void extractLabels_invalidUrl_returnsBadRequestWithoutLoadingResource() throws Exception {
    mvc.perform(
            get("/extractLabels")
                .param("imageUrl", "https://169.254.169.254/computeMetadata/v1/"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(resourceLoader, cloudVisionTemplate);
  }

  @Test
  public void extractText_validUrl_loadsResourceAndCallsVision() throws Exception {
    Resource resource = mock(Resource.class);
    when(resourceLoader.getResource(VALID_URL)).thenReturn(resource);
    when(cloudVisionTemplate.extractTextFromImage(resource)).thenReturn("hello");

    mvc.perform(get("/extractText").param("imageUrl", VALID_URL))
        .andExpect(status().isOk())
        .andExpect(content().string("Text from image: hello"));

    verify(resourceLoader).getResource(VALID_URL);
    verify(cloudVisionTemplate).extractTextFromImage(resource);
  }
}
