/*
  Copyright 2017, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.cloud.iot.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/** Tests for iot "Management" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ManagerIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private DeviceRegistryExample app;
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String BUCKET = PROJECT_ID;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    app = new DeviceRegistryExample();
    // TODO: Create IoT PubSub Topic
    // TODO: Create Registry
  }

  @After
  public void tearDown() {
    // TODO: Delete registry
    // TODO: Remove PubSub Topic
    System.setOut(null);
  }

  @Test
  public void testCreateDeleteUnauthDevice() throws Exception {
    // TODO: Implement when library is live
  }

  @Test
  public void testCreateDeleteEsDevice() throws Exception {
    // TODO: Implement when library is live
  }

  @Test
  public void testCreateDeleteRsaDevice() throws Exception {
    // TODO: Implement when library is live
  }

  @Test
  public void testCreateGetDevice() throws Exception {
    // TODO: Implement when library is live
  }


  @Test
  public void testCreateListDevices() throws Exception {
    // TODO: Implement when library is live
  }

  @Test
  public void testCreateGetRegistry() throws Exception {
    // TODO: Implement when library is live
  }
}
