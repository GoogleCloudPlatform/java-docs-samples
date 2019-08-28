/*
 * Copyright 2019 Google LLC
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

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.pubsub.v1.ProjectTopicName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for real time feed sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class RealTimeFeed {
  private static final String topicId = "topicId";
  private static final String feedId = UUID.randomUUID().toString();
  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private final String projectNumber = getProjectNumber(projectId);
  private final String feedName = String.format("projects/%s/feeds/%s", projectNumber, feedId);
  private final String[] assetNames = {UUID.randomUUID().toString()};
  private static final ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
  private static ByteArrayOutputStream bout;
  private static PrintStream out;

  private String getProjectNumber(String projectId) {
    ResourceManager resourceManager = ResourceManagerOptions.getDefaultInstance().getService();
    ProjectInfo project = resourceManager.get(projectId);
    return Long.toString(project.getProjectNumber());
  }

  @BeforeClass
  public static void setUp() {
    //TopicAdminClient topicAdminClient = TopicAdminClient.create();
    //topicAdminClient.deleteTopic(topicName);
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void tearDown() {
    String consoleOutput = bout.toString();
    System.setOut(null);
    bout.reset();
    //TopicAdminClient topicAdminClient = TopicAdminClient.create();
    //topicAdminClient.createTopic(topicName);
  }

  @Test
  public void testCreateFeedExample() throws Exception {
    CreateFeedExample.createFeed(assetNames, feedId, topicName.toString(), projectId);
    String got = bout.toString();
    assertThat(got).contains("Feed created successfully:" + feedName);
  }

  @Test
  public void testGetFeedExample() throws Exception {
    GetFeedExample.getFeed(feedName);
    String got = bout.toString();
    assertThat(got).contains(feedId);
  }

  @Test
  public void testListFeedsExample() throws Exception {
    ListFeedsExample.listFeeds(projectId);
    String got = bout.toString();
    assertThat(got).contains(feedId);
  }

  @Test
  public void testUpdateFeedExample() throws Exception {
    UpdateFeedExample.updateFeed(feedName, topicName.toString());
    String got = bout.toString();
    assertThat(got).contains("Feed updated successfully:" + feedName);
  }

  @Test
  public void testDeleteFeedExample() throws Exception {
    DeleteFeedExample.deleteFeed(feedName);
    String got = bout.toString();
    assertThat(got).contains("Feed deleted");
  }
}
