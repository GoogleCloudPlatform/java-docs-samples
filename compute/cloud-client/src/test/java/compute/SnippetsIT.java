/*
 * Copyright 2020 Google LLC
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

package compute;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import compute.CreateInstance;
import compute.DeleteInstance;
import compute.ListInstance;
import compute.ListAllInstances;


import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(JUnit4.class)
public class SnippetsIT {

    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static String ZONE;
    private static String MACHINE_NAME;
    private static String MACHINE_NAME_DELETE;
    private static String MACHINE_NAME_LIST_INSTANCE;

    private ByteArrayOutputStream stdOut;

    // check if the required environment variables are set
    public static void requireEnvVar(String envVarName) {
        assertWithMessage(String.format("Missing environment variable '%s' ", envVarName)).that(System.getenv(envVarName)).isNotEmpty();
    }

    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
        requireEnvVar("GOOGLE_CLOUD_PROJECT");

        ZONE = "us-central1-a";
        MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID().toString();
        MACHINE_NAME_DELETE = "my-new-test-instance" + UUID.randomUUID().toString();
        MACHINE_NAME_LIST_INSTANCE = "my-new-test-instance" + UUID.randomUUID().toString();

        CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME);
        CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
        CreateInstance.createInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);
    }


    @AfterClass
    public static void cleanup() throws IOException, InterruptedException {
        // delete all instances created for testing
        requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
        requireEnvVar("GOOGLE_CLOUD_PROJECT");

        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));

        DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME);
        DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_LIST_INSTANCE);

        stdOut.close();
        System.setOut(null);
    }

    @Before
    public void beforeEach() {
        stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));
    }

    @After
    public void afterEach() {
        stdOut = null;
        System.setOut(null);
    }

    @Test
    public void testCreateInstance() throws IOException {
        // check if the instance was successfully created during the setup
        try (InstancesClient instancesClient = InstancesClient.create()) {
            Instance response = instancesClient.get(PROJECT_ID, ZONE, MACHINE_NAME);
            assertThat(response.getName()).contains(MACHINE_NAME);
        }
    }

    @Test
    public void testListInstance() throws IOException {
        ListInstance.listInstances(PROJECT_ID, ZONE);
        assertThat(stdOut.toString()).contains(MACHINE_NAME_LIST_INSTANCE);
    }

    @Test
    public void testListAllInstances() throws IOException {
        ListAllInstances.listAllInstances(PROJECT_ID);
        assertThat(stdOut.toString()).contains(MACHINE_NAME_LIST_INSTANCE);
    }

    @Test
    public void testDeleteInstance() throws IOException, InterruptedException {
        DeleteInstance.deleteInstance(PROJECT_ID, ZONE, MACHINE_NAME_DELETE);
        assertThat(stdOut.toString()).contains("####### Instance deletion complete #######");
    }

}
