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

import static com.google.common.truth.Truth.assertThat;

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.CloudHealthcare;
import com.google.api.services.healthcare.v1beta1.model.ListDatasetsResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

public class HealthcareTestBase {
  protected static final String DEFAULT_CLOUD_REGION = "us-central1";
  protected static final String DEFAULT_PROJECT_ID =
      System.getenv("GOOGLE_CLOUD_PROJECT");
  protected static final String GCLOUD_BUCKET_NAME =
      System.getenv("GCLOUD_BUCKET_NAME");
  protected static final String GCLOUD_PUBSUB_TOPIC =
      System.getenv("GCLOUD_PUBSUB_TOPIC");
  protected static String datasetId;
  protected static String suffix;
  protected static ByteArrayOutputStream bout;

  protected static void setUpClass() {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);

    suffix = UUID
        .randomUUID()
        .toString()
        .substring(0, 4)
        .replaceAll("-", "_");

    datasetId = "dataset-" + suffix;
  }

  protected static void tearDownClass() throws IOException {
    // Delete all lingering datasets.
    CloudHealthcare client = HealthcareQuickstart.getCloudHealthcareClient();
    String parentName = String.format(
        "projects/%s/locations/%s",
        DEFAULT_PROJECT_ID,
        DEFAULT_CLOUD_REGION);
    ListDatasetsResponse response = client
        .projects()
        .locations()
        .datasets()
        .list(parentName)
        .execute();
    if (response.getDatasets() == null) {
      return;
    }
    for (int i = 0; i < response.getDatasets().size(); i++) {
      client
          .projects()
          .locations()
          .datasets()
          .delete(response.getDatasets().get(i).getName())
          .execute();
    }
  }

  protected void tearDown() {
    bout.reset();
  }

  protected void assertBoutContents(String... expected) {
    for (String s : expected) {
      assertThat(bout.toString()).containsMatch(s);
    }
    bout.reset();
  }

  protected void assertNotBoutContents(String... notExpected) {
    for (String s : notExpected) {
      assertThat(bout.toString()).doesNotContainMatch(s);
    }
    bout.reset();
  }
}
