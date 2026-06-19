/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.securitycenter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.securitycenter.v1.OrganizationName;
import com.google.cloud.securitycenter.v1.SourceName;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

/** Smoke tests for {@link com.google.cloud.examples.securitycenter.snippets.SourceSnippets} */
public class ITSourceSnippets {

  private static SourceName SOURCE_NAME;

  @BeforeClass
  public static void setUp() throws IOException {
    org.junit.Assume.assumeTrue(
        "Skipping tests: GCLOUD_ORGANIZATION env var is not set.",
        System.getenv("GCLOUD_ORGANIZATION") != null);
    SOURCE_NAME = SourceName.parse(SourceSnippets.createSource(getOrganizationId()).getName());
  }

  @Test
  public void testCreateSource() throws IOException {
    assertNotNull(SourceSnippets.createSource(getOrganizationId()));
  }

  @Test
  public void testListSources() throws IOException {
    assertTrue(SourceSnippets.listSources(getOrganizationId()).size() > 1);
  }

  @Test
  public void testUpdateSource() throws IOException {
    assertEquals(
        "Updated Display Name", SourceSnippets.updateSource(SOURCE_NAME).getDisplayName());

  @Test
  public void testGetSource() throws IOException {
    assertTrue(SourceSnippets.getSource(SOURCE_NAME).getName().equals(SOURCE_NAME.toString()));
  }

  @Test
  public void testSetSourceIamPolicy() throws IOException {
    assertTrue(
        SourceSnippets.setIamPolicySource(SOURCE_NAME, "csccclienttest@gmail.com")
            .getBindings(0)
            .getRole()
            .equals("roles/securitycenter.findingsEditor"));
  }

  @Test
  public void testGetSourceIamPolicy() throws IOException {
    assertNotNull(SourceSnippets.getIamPolicySource(SOURCE_NAME));
  }

  private static OrganizationName getOrganizationId() {
    return OrganizationName.of(System.getenv("GCLOUD_ORGANIZATION"));
  }
}
