/*
 * Copyright (c) 2016 Google Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.cloud.tasks.appenginequeues;

import static com.google.common.truth.Truth.assertThat;

import com.example.cloud.tasks.Common;

import com.google.api.services.cloudtasks.v2beta2.CloudTasks;
import com.google.api.services.cloudtasks.v2beta2.model.Task;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Integration tests for the Cloud Tasks snippets. */
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class CloudTasksAppEngineSnippetsIT {

  /** The Google Cloud project ID of the project created for the integration tests. */
  public static final String TEST_PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  /** The project ID of the project created for the integration tests. */
  public static final String TEST_LOCATION_ID = System.getenv("LOCATION_ID");

  public static final String TEST_QUEUE_ID = System.getenv("APPENGINE_QUEUE_ID");

  public static final String TEST_QUEUE_NAME =
      "projects/" + TEST_PROJECT_ID + "/locations/" + TEST_LOCATION_ID
              + "/queues/" + TEST_QUEUE_ID;

  private static final String PAYLOAD = "TEST_PAYLOAD";

  /** Cloud Tasks pull queue samples to run through integration tests. */
  private CloudTasksAppEngineQueueSnippets snippets;

  @Before
  public void setUp() throws Exception {
    CloudTasks client = Common.authenticate();
    snippets = new CloudTasksAppEngineQueueSnippets(client);
  }

  /** Integration tests that tests App Engine tasks are created successfully. */
  @Test
  public void testCreateTask() throws Exception {
    Task task = snippets.createAppEngineTask(TEST_QUEUE_NAME, PAYLOAD);
    assertThat(task).isNotNull();
  }

  @Test
  public void testMainRunner() throws Exception {
    String[] args = {TEST_QUEUE_NAME, PAYLOAD};
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    snippets.runner(args, new PrintStream(os));
    String output = os.toString();
    assertThat(output).contains("Created task");
  }
}
