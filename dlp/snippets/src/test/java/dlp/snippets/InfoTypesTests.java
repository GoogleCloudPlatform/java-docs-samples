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

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class InfoTypesTests extends TestBase {

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS");
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
    CreateStoredInfoType.createStoredInfoType(PROJECT_ID, GCS_PATH);
    String output = bout.toString();
    String storedInfoTypeId = output.split("Created Stored InfoType: ")[1].split("\n")[0];
    assertThat(output).contains("Created Stored InfoType: ");
    assertThat(output).contains("storedInfoTypes");
    try (DlpServiceClient dlp = DlpServiceClient.create()) {
      dlp.deleteStoredInfoType(storedInfoTypeId);
    }
  }
}
