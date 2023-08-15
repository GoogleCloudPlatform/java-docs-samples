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
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.google.cloud.run.v2.ServiceName;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServicesTests {

  private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String location = "us-central1";
  private static final String imageUrl = "us-docker.pkg.dev/cloudrun/container/hello";
  private static String service;
  private final PrintStream originalOut = System.out;
  private ByteArrayOutputStream output;

  @BeforeClass
  public static void before() {
    service = "test-service-" + UUID.randomUUID().toString().substring(0, 30);
  }

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
  public void a_createService() throws IOException, InterruptedException, ExecutionException {
    CreateService.createService(project, location, service, imageUrl);
    assertThat(output.toString()).contains(ServiceName.of(project, location, service).toString());
  }

  @Test
  public void b_getService() throws IOException {
    GetService.getService(project, location, service);
    assertThat(output.toString()).contains(ServiceName.of(project, location, service).toString());
  }

  @Test
  public void c_listServices() throws IOException {
    ListServices.listServices(project, location);
    assertThat(output.toString()).contains(ServiceName.of(project, location, service).toString());
  }

  @Test
  public void d_updateService() throws IOException, InterruptedException, ExecutionException {
    UpdateService.updateService(project, location, service);
    assertThat(output.toString()).contains(ServiceName.of(project, location, service).toString());
  }

  @Test
  public void e_iamPolicy() throws IOException {
    // Set Policy
    String member1 = "user:akitsch@google.com";
    SetIamPolicy.setIamPolicy(project, location, service, member1, "roles/run.invoker");
    assertThat(output.toString()).contains(member1);
    // Get Policy
    GetIamPolicy.getIamPolicy(project, location, service);
    assertThat(output.toString()).contains("Role:");
    // Update Policy
    String member2 = "user:foo@domain.com";
    UpdateIamPolicy.updateIamPolicy(project, location, service, member2, "roles/run.invoker");
    assertThat(output.toString()).contains(String.format("[%s, %s]",member1, member2));
  }

  @Test
  public void f_deleteService() throws IOException, InterruptedException, ExecutionException {
    DeleteService.deleteService(project, location, service);
    assertThat(output.toString()).contains(ServiceName.of(project, location, service).toString());
  }
}
