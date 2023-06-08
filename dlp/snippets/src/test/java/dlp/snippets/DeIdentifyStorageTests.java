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

package dlp.snippets;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({
  "javax.management.",
  "com.sun.org.apache.xerces.",
  "javax.xml.",
  "org.xml.",
  "org.w3c.dom.",
  "com.sun.org.apache.xalan.",
  "javax.activation.*"
})
@PrepareForTest({DlpServiceClient.class})
public class DeIdentifyStorageTests extends TestBase {

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS");
  }

  @Test
  public void testDeidentifyStorage() throws Exception {
    DlpServiceClient dlpServiceClientMock = mock(DlpServiceClient.class);
    PowerMockito.mockStatic(DlpServiceClient.class);
    when(DlpServiceClient.create()).thenReturn(dlpServiceClientMock);
    InfoTypeStats infoTypeStats =
        InfoTypeStats.newBuilder()
            .setInfoType(InfoType.newBuilder().setName("EMAIL_ADDRESS").build())
            .setCount(2)
            .build();
    DlpJob dlpJob =
        DlpJob.newBuilder()
            .setName("projects/project_id/locations/global/dlpJobs/job_id")
            .setState(DlpJob.JobState.DONE)
            .setInspectDetails(
                InspectDataSourceDetails.newBuilder()
                    .setResult(
                        InspectDataSourceDetails.Result.newBuilder()
                            .addInfoTypeStats(infoTypeStats)
                            .build()))
            .build();
    when(dlpServiceClientMock.createDlpJob(any())).thenReturn(dlpJob);
    when(dlpServiceClientMock.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
    DeidentifyCloudStorage.deidentifyCloudStorage(
        "project_id",
        "gs://input_bucket/test.txt",
        "table_id",
        "dataset_id",
        "gs://output_bucket",
        "deidentify_template_id",
        "deidentify_structured_template_id",
        "image_redact_template_id");

    String output = bout.toString();
    assertThat(output).contains("Job status: DONE");
    assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
    assertThat(output).contains("Info type: EMAIL_ADDRESS");
    assertThat(output).contains("Count: 2");
  }
}
