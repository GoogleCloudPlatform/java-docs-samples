/*
 * Copyright 2023 Google LLC
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

package cloudrun.snippets.services;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServicesTests {

  private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String location = "us-central1";
  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream output;

  @Before
  public void beforeEach() {
    output = new ByteArrayOutputStream();
    System.setOut(new PrintStream(output));
    System.setErr(new PrintStream(output));
  }

  @After
  public void afterEach() {
    System.setOut(originalOut);
    output.reset();
  }

  @Test
  public void listServices() throws IOException {
    // SyncListServicesLocationname.syncListServicesLocationname(project, location);
    // assertThat(output.toString()).contains("my service");

    //     AsyncListServices.asyncListServices(project, location);
    // assertThat(output.toString()).contains("my service");

    // AsyncListServicesPaged.asyncListServicesPaged(project, location);
    // assertThat(output.toString()).contains("my service");
  }

  String service = "filesystem-app";

  @Test
  public void getService() throws IOException {
    GetService.syncGetServiceServicename(project, location, service);
    assertThat(output.toString()).contains("my service");
  }
  @Test
  public void createService() throws IOException, InterruptedException, ExecutionException {
    AsyncCreateServiceLRO.asyncCreateServiceLRO(project, location);
    SyncCreateService.syncCreateService(project, location);
    SyncCreateServiceLocationnameServiceString.syncCreateServiceLocationnameServiceString(project, location);
  }
  @Test
  public void updateService() throws IOException {
  }
  @Test
  public void deleteService() throws IOException {
  }
  @Test
  public void iamPolicy() throws IOException {
  }

}
