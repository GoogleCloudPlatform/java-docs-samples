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

package compute;

import com.google.cloud.compute.v1.AggregatedListInstancesRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstanceTemplatesClient.ListPagedResponse;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesClient.AggregatedListPagedResponse;
import com.google.cloud.compute.v1.InstancesScopedList;
import com.google.cloud.compute.v1.ListInstanceTemplatesRequest;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Util {
  // Cleans existing test resources if any.
  // If the project contains too many instances, use "filter" when listing resources
  // and delete the listed resources based on the timestamp.

  private static final int DELETION_THRESHOLD_TIME_HOURS = 24;

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
        DeleteInstanceTemplate.deleteInstanceTemplate(projectId, template.getName());
      }
    }

  }

  // Delete instances which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingInstances(String prefixToDelete, String projectId,
      String instanceZone)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    for (Entry<String, InstancesScopedList> instanceGroup : listFilteredInstances(
        projectId, prefixToDelete).iterateAll()) {
      for (Instance instance : instanceGroup.getValue().getInstancesList()) {
        if (!instance.hasCreationTimestamp()) {
          continue;
        }
        if (instance.getName().contains(prefixToDelete)
            && isCreatedBeforeThresholdTime(instance.getCreationTimestamp())
            && instance.getStatus().equalsIgnoreCase(Status.RUNNING.toString())) {
          DeleteInstance.deleteInstance(projectId, instanceZone, instance.getName());
        }
      }
    }
  }

  public static boolean isCreatedBeforeThresholdTime(String timestamp) {
    return OffsetDateTime.parse(timestamp).toInstant()
        .isBefore(Instant.now().minus(DELETION_THRESHOLD_TIME_HOURS, ChronoUnit.HOURS));
  }

  public static AggregatedListPagedResponse listFilteredInstances(String project,
      String instanceNamePrefix) throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {

      AggregatedListInstancesRequest aggregatedListInstancesRequest = AggregatedListInstancesRequest
          .newBuilder()
          .setProject(project)
          .setFilter(String.format("name:%s", instanceNamePrefix))
          .build();

      return instancesClient
          .aggregatedList(aggregatedListInstancesRequest);
    }
  }

  public static ListPagedResponse listFilteredInstanceTemplates(String projectId,
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

}