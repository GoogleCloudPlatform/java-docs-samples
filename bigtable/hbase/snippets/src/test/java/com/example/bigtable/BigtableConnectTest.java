/*
 * Copyright 2018 Google LLC
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

package com.example.bigtable;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class BigtableConnectTest {

  // provide your project id as an env var
  private final String projectId = System.getProperty("bigtable.projectID");
  private final String instanceId = System.getProperty("bigtable.instanceID");
  BigtableConnect helper;

  @Before
  public void prepare() throws Exception {
    helper = new BigtableConnect();
    helper.main(projectId, instanceId);
  }

  @After
  public void tearDown() throws IOException {
    helper.closeConnection();
  }

  @Test
  public void connection() throws Exception {
    helper.connect();

    assertThat(helper.connection.toString()).contains("project=" + projectId);
    assertThat(helper.connection.toString()).contains("instance=" + instanceId);
  }

  @Test
  public void connectionWithAppProfile() throws Exception {
    helper.connectWithAppProfile();

    assertThat(helper.connection.toString()).contains("project=" + projectId);
    assertThat(helper.connection.toString()).contains("instance=" + instanceId);
  }

  @Test
  public void connectionWithConfiguration() throws Exception {
    helper.connectWithConfiguration();

    assertThat(helper.connection.toString()).contains("project=" + projectId);
    assertThat(helper.connection.toString()).contains("instance=" + instanceId);
  }
}
