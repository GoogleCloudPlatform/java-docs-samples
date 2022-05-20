/*
 * Copyright 2022 Google LLC
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

package com.example.livestream;

import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.InputName;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import java.io.IOException;
import java.time.Instant;

public class TestUtils {

  private static final int DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS = 10800; // 3 hours

  public boolean isInputStale(String projectID, String location, String inputID)
      throws IOException {
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {

      InputName name = InputName.of(projectID, location, inputID);
      Input response = livestreamServiceClient.getInput(name);

      return response.getCreateTime().getSeconds()
          < Instant.now().getEpochSecond() - DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS;
    }
  }
}
