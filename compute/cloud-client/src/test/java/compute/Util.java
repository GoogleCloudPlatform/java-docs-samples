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

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstancesScopedList;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

public class Util {
  // Cleans existing test resources if any.
  // If the project contains too many instances, use "filter" when listing resources
  // and delete the listed resources based on the timestamp.

  private static final int DELETION_THRESHOLD_TIME_HOURS = 24;

  // Delete templates which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingInstanceTemplates(String prefixToDelete, String projectId)
      throws IOException, ExecutionException, InterruptedException {
    for (InstanceTemplate template : ListInstanceTemplates.listInstanceTemplates(projectId)
        .iterateAll()) {
      if (!template.hasCreationTimestamp()) {
        continue;
      }
      if (template.getName().contains(prefixToDelete)
          && isCreatedBeforeThresholdTime(template.getCreationTimestamp())) {
        DeleteInstanceTemplate.deleteInstanceTemplate(projectId, template.getName());
      }
    }

  }

  // Delete instances which starts with the given prefixToDelete and
  // has creation timestamp >24 hours.
  public static void cleanUpExistingInstances(String prefixToDelete, String projectId,
      String instanceZone)
      throws IOException, ExecutionException, InterruptedException {
    for (Entry<String, InstancesScopedList> instanceGroup : ListAllInstances.listAllInstances(
        projectId).iterateAll()) {
      for (Instance instance : instanceGroup.getValue().getInstancesList()) {
        if (!instance.hasCreationTimestamp()) {
          continue;
        }
        if (instance.getName().contains(prefixToDelete)
            && isCreatedBeforeThresholdTime(instance.getCreationTimestamp())) {
          DeleteInstance.deleteInstance(projectId, instanceZone, instance.getName());
        }
      }
    }
  }

  public static boolean isCreatedBeforeThresholdTime(String timestamp) {
    return OffsetDateTime.parse(timestamp).toInstant()
        .isBefore(Instant.now().minus(DELETION_THRESHOLD_TIME_HOURS, ChronoUnit.HOURS));
  }

}