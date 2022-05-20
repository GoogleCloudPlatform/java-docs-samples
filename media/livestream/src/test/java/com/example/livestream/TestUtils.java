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

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.video.livestream.v1.DeleteInputRequest;
import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.ListInputsRequest;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient;
import com.google.cloud.video.livestream.v1.LocationName;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestUtils {

  private static final int DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS = 10800; // 3 hours

  public static void cleanStaleInputs(String projectId, String location) {
    try (LivestreamServiceClient livestreamServiceClient = LivestreamServiceClient.create()) {
      var listInputsRequest =
          ListInputsRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .build();

      LivestreamServiceClient.ListInputsPagedResponse response =
          livestreamServiceClient.listInputs(listInputsRequest);

      for (Input input : response.iterateAll()) {
        if (input.getCreateTime().getSeconds()
            < Instant.now().getEpochSecond() - DELETION_THRESHOLD_TIME_HOURS_IN_SECONDS) {
          var deleteInputRequest =
              DeleteInputRequest.newBuilder()
                  .setName(input.getName())
                  .build();
          livestreamServiceClient.deleteInputAsync(deleteInputRequest).get(1, TimeUnit.MINUTES);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      e.printStackTrace();
    }
  }
}