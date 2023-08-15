/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloudrun.snippets.revisions;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.google.cloud.run.v2.Revision;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.RevisionsClient.ListRevisionsPagedResponse;
import cloudrun.snippets.services.CreateService;
import cloudrun.snippets.services.DeleteService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RevisionsTests {

    private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String location = "us-central1";
    private static final String imageUrl = "us-docker.pkg.dev/cloudrun/container/hello";
    private static String service;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream output;

    @BeforeClass
    public static void before() throws IOException, InterruptedException, ExecutionException {
        service = "test-service-" + UUID.randomUUID().toString().substring(0, 30);
        CreateService.createService(project, location, service, imageUrl);
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

    @AfterClass
    public static void after() throws IOException, InterruptedException, ExecutionException {
        DeleteService.deleteService(project, location, service);
    }

    @Test
    public void a_listRevisions() throws IOException, InterruptedException, ExecutionException {
        ListRevisions.listRevisions(project, location, service);
        assertThat(output.toString())
                .contains(ServiceName.of(project, location, service).toString());
    }

    @Test
    public void b_getRevisions() throws Exception {
        List<Revision> revisions = ListRevisions.listRevisions(project, location, service);
        String revisionName = revisions.get(0).getName();
        int index = revisionName.lastIndexOf("/");
        GetRevision.getRevision(project, location, service, revisionName.substring(index + 1));
        assertThat(output.toString())
                .contains(ServiceName.of(project, location, service).toString());
    }

    @Test
    public void b_deleteRevisions() throws Exception {
        List<Revision> revisions = ListRevisions.listRevisions(project, location, service);
        String revisionName = revisions.get(0).getName();
        int index = revisionName.lastIndexOf("/");
        GetRevision.getRevision(project, location, service, revisionName.substring(index + 1));
        assertThat(output.toString())
                .contains(ServiceName.of(project, location, service).toString());
    }
}
