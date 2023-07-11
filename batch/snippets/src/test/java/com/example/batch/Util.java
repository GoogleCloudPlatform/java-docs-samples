// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.batch;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.batch.v1.BatchServiceClient;
import com.google.cloud.batch.v1.Job;
import com.google.cloud.batch.v1.JobName;
import com.google.cloud.batch.v1.JobStatus.State;
import com.google.cloud.compute.v1.DeleteInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstanceTemplatesClient.ListPagedResponse;
import com.google.cloud.compute.v1.ListInstanceTemplatesRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Util {

  private static final int DELETION_THRESHOLD_TIME_HOURS = 24;
  private static final List<State> WAIT_STATES = new ArrayList<>(
      Arrays.asList(State.STATE_UNSPECIFIED, State.QUEUED, State.RUNNING, State.SCHEDULED));

  // Delete templates which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingInstanceTemplates(String prefixToDelete, String projectId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    for (InstanceTemplate template : listFilteredInstanceTemplates(projectId, prefixToDelete)
        .iterateAll()) {
      if (!template.hasCreationTimestamp()) {
        continue;
      }
      if (template.getName().contains(prefixToDelete)
          && isCreatedBeforeThresholdTime(template.getCreationTimestamp())
          && template.isInitialized()) {
        deleteInstanceTemplate(projectId, template.getName());
      }
    }
  }

  private static ListPagedResponse listFilteredInstanceTemplates(String projectId,
      String instanceTemplatePrefix) throws IOException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {
      ListInstanceTemplatesRequest listInstanceTemplatesRequest =
          ListInstanceTemplatesRequest.newBuilder()
              .setProject(projectId)
              .setFilter(String.format("name:%s", instanceTemplatePrefix))
              .build();

      return instanceTemplatesClient.list(listInstanceTemplatesRequest);
    }
  }

  private static boolean isCreatedBeforeThresholdTime(String timestamp) {
    return OffsetDateTime.parse(timestamp).toInstant()
        .isBefore(Instant.now().minus(DELETION_THRESHOLD_TIME_HOURS, ChronoUnit.HOURS));
  }

  // Delete an instance template.
  private static void deleteInstanceTemplate(String projectId, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {

      DeleteInstanceTemplateRequest deleteInstanceTemplateRequest = DeleteInstanceTemplateRequest
          .newBuilder()
          .setProject(projectId)
          .setInstanceTemplate(templateName).build();

      instanceTemplatesClient.deleteAsync(deleteInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);
    }
  }

  public static Job getJob(String projectId, String region, String jobName) throws IOException {
    try (BatchServiceClient batchServiceClient = BatchServiceClient.create()) {
      return
          batchServiceClient.getJob(
              JobName.newBuilder()
                  .setProject(projectId)
                  .setLocation(region)
                  .setJob(jobName)
                  .build());
    }
  }

  public static void waitForJobCompletion(Job job)
      throws IOException, InterruptedException {
    String[] jobName = job.getName().split("/");
    Instant startTime = Instant.now();
    while (WAIT_STATES.contains(job.getStatus().getState())) {
      if (Instant.now().getEpochSecond() - startTime.getEpochSecond() > 900) {
        throw new Error("Timed out waiting for operation to complete.");
      }
      job = getJob(jobName[1], jobName[3], jobName[5]);
      TimeUnit.SECONDS.sleep(10);
    }
    job = getJob(jobName[1], jobName[3], job.getName().split("/")[5]);
    assertThat(job.getStatus().getState() == State.SUCCEEDED);
    System.out.println("Job completed.");
  }
}
