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
  private static String topicId = UUID.randomUUID().toString();
  private String feedId = "java_feed";
  private static String projectId = ServiceOptions.getDefaultProjectId();
  private String feedName = "projects/846710131186/feeds/java_feed";
  private String[] assetNames = {UUID.randomUUID().toString()};
  private static ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
  private static ByteArrayOutputStream bout;
  private static PrintStream out;

  private static final void deleteTopic(ProjectTopicName topicName) {
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topicName);
    } catch (Exception e) {
      System.out.print("Error during deleteTopic: \n" + e.toString());
    }
  }

  private static final void createTopic(ProjectTopicName topicName) {
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.createTopic(topicName);
    } catch (Exception e) {
      System.out.print("Error during createTopic: \n" + e.toString());
    }
  }

  @BeforeClass
  public static void setUp() {
    createTopic(topicName);
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @AfterClass
  public static void tearDown() {
    String consoleOutput = bout.toString();
    System.setOut(null);
    deleteTopic(topicName);
  }

  @Test
  public void testCreateFeedExample() throws Exception {
    CreateFeedExample.createFeed(assetNames, feedId, topicName.toString(), projectId);
    String got = bout.toString();
    assertThat(got).contains(feedName);
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
    assertThat(got).contains(feedName);
  }

  @Test
  public void testDeleteFeedExample() throws Exception {
    DeleteFeedExample.deleteFeed(feedName);
  }
}
