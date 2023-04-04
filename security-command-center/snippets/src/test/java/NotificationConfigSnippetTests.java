/*
 * Copyright 2022 Google LLC
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class NotificationConfigSnippetTests {

  private static void createConfig(String configId) throws IOException {
    CreateNotificationConfigSnippets.createNotificationConfig(
        String.format("organizations/%s", getOrganizationId()), configId, getProject(),
        getTopicName());
  }

  private static void deleteConfig(String configId) throws IOException {
    assertTrue(
        DeleteNotificationConfigSnippets.deleteNotificationConfig(
            String.format("organizations/%s", getOrganizationId()), configId));
  }

  private static String getOrganizationId() {
    return "1081635000895";
  }

  private static String getProject() {
    return "project-a-id";
  }

  private static String getTopicName() {
    return "notifications-sample-topic";
  }

  @Test
  public void testCreateNotificationConfig() throws IOException {
    String configId = String.format("java-config-%s", UUID.randomUUID());
    assertNotNull(
        CreateNotificationConfigSnippets.createNotificationConfig(
            String.format("organizations/%s", getOrganizationId()), configId, getProject(),
            getTopicName()));

    deleteConfig(configId);
  }

  @Test
  public void testDeleteNotificationConfig() throws IOException {
    String configId = String.format("java-config-%s", UUID.randomUUID());
    createConfig(configId);

    assertTrue(
        DeleteNotificationConfigSnippets.deleteNotificationConfig(
            String.format("organizations/%s", getOrganizationId()), configId));
  }

  @Test
  public void testListNotificationConfig() throws IOException {
    String configId = String.format("java-config-%s", UUID.randomUUID());
    createConfig(configId);

    assertNotNull(ListNotificationConfigSnippets.listNotificationConfigs(
        String.format("organizations/%s", getOrganizationId())));

    deleteConfig(configId);
  }

  @Test
  public void testGetNotificationConfig() throws IOException {
    String configId = String.format("java-config-%s", UUID.randomUUID());
    createConfig(configId);

    assertNotNull(
        GetNotificationConfigSnippets.getNotificationConfig(
            String.format("organizations/%s", getOrganizationId()), configId));

    deleteConfig(configId);
  }

  @Test
  public void testUpdateNotificationConfig() throws IOException {
    String configId = String.format("java-config-%s", UUID.randomUUID());
    createConfig(configId);

    assertNotNull(
        UpdateNotificationConfigSnippets.updateNotificationConfig(
            String.format("organizations/%s", getOrganizationId()), configId, getProject(),
            getTopicName()));

    deleteConfig(configId);
  }
}
