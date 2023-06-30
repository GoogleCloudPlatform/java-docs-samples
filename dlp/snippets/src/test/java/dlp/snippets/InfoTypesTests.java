/*
 * Copyright 2020 Google Inc.
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.CreateStoredInfoTypeRequest;
import com.google.privacy.dlp.v2.StoredInfoType;
import com.google.privacy.dlp.v2.UpdateStoredInfoTypeRequest;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class InfoTypesTests extends TestBase {

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT");
  }

  @Test
  public void testListInfoTypes() throws Exception {
    InfoTypesList.listInfoTypes();
    String output = bout.toString();
    assertThat(output).contains("Name");
    assertThat(output).contains("Display name");
  }

  @Test
  public void testCreateStoredInfoType() throws IOException {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(DlpServiceClient::create).thenReturn(dlpServiceClient);
      StoredInfoType response =
          StoredInfoType.newBuilder()
              .setName("projects/project_id/locations/global/storedInfoTypes/github-usernames")
              .build();
      when(dlpServiceClient.createStoredInfoType(any(CreateStoredInfoTypeRequest.class)))
          .thenReturn(response);
      CreateStoredInfoType.createStoredInfoType("project_id", "gs://bucket_name");
      String output = bout.toString();
      assertThat(output)
          .contains(
              "Created Stored InfoType: "
                  + "projects/project_id/locations/global/storedInfoTypes/github-usernames");
      verify(dlpServiceClient, times(1))
          .createStoredInfoType(any(CreateStoredInfoTypeRequest.class));
    }
  }

  @Test
  public void testUpdateStoredInfoType() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(DlpServiceClient::create).thenReturn(dlpServiceClient);
      StoredInfoType response =
          StoredInfoType.newBuilder()
              .setName("projects/project_id/locations/global/storedInfoTypes/github-usernames")
              .build();
      when(dlpServiceClient.updateStoredInfoType(any(UpdateStoredInfoTypeRequest.class)))
          .thenReturn(response);
      UpdateStoredInfoType.updateStoredInfoType(
          "project_id", "gs://bucket_name/term_list.txt", "gs://bucket_name", "github-usernames");
      String output = bout.toString();
      assertThat(output).contains("Updated stored InfoType successfully");
      verify(dlpServiceClient, times(1))
          .updateStoredInfoType(any(UpdateStoredInfoTypeRequest.class));
    }
  }
}
