/*
 * Copyright 2024 Google LLC
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

package tpu;

import com.google.cloud.tpu.v2.Node;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

public class Util {
  private static final int DELETION_THRESHOLD_TIME_MINUTES = 10;

  // Delete TPU VMs which starts with the given prefixToDelete and
  // has creation timestamp >30 minutes.
  public static void cleanUpExistingTpu(String prefixToDelete, String projectId, String zone)
      throws IOException, ExecutionException, InterruptedException {
    try (TpuClient tpuClient = TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);
      for (Node node : tpuClient.listNodes(parent).iterateAll()) {
        String creationTime = formatTimestamp(node.getCreateTime());
        String name = node.getName().substring(node.getName().lastIndexOf("/") + 1);
        if (containPrefixToDeleteAndZone(node, prefixToDelete, zone)
            && isCreatedBeforeThresholdTime(creationTime)) {
          DeleteTpuVm.deleteTpuVm(projectId, zone, name);
        }
      }
    }
  }

  // Delete TPU VMs which starts with the given prefixToDelete and
  // has creation timestamp >30 minutes.
  public static void cleanUpExistingQueuedResources(
      String prefixToDelete, String projectId, String zone)
      throws IOException {
    try (com.google.cloud.tpu.v2alpha1.TpuClient tpuClient =
             com.google.cloud.tpu.v2alpha1.TpuClient.create()) {
      String parent = String.format("projects/%s/locations/%s", projectId, zone);

      for (QueuedResource queuedResource : tpuClient.listQueuedResources(parent).iterateAll()) {

        com.google.cloud.tpu.v2alpha1.Node node = queuedResource.getTpu().getNodeSpec(0).getNode();
        String creationTime = formatTimestamp(node.getCreateTime());
        String name = queuedResource.getName().substring(node.getName().lastIndexOf("/") + 1);
        if (containPrefixToDeleteAndZone(queuedResource, prefixToDelete, zone)
            && isCreatedBeforeThresholdTime(creationTime)) {
          DeleteQueuedResource.deleteQueuedResource(projectId, zone, name);
        }
      }
    } catch (ExecutionException | InterruptedException e) {
      System.out.println("Exception for deleting QueuedResource: " + e.getMessage());
    }
  }

  public static boolean containPrefixToDeleteAndZone(
      Object resource, String prefixToDelete, String zone) {
    boolean containPrefixAndZone = false;
    try {
      if (resource instanceof Node) {
        containPrefixAndZone = ((Node) resource).getName().contains(prefixToDelete)
            && ((Node) resource).getName().split("/")[3].contains(zone);
      }
      if (resource instanceof QueuedResource) {
        containPrefixAndZone = ((QueuedResource) resource).getName().contains(prefixToDelete)
            && ((QueuedResource) resource).getName().split("/")[3].contains(zone);
      }
    } catch (NullPointerException e) {
      System.out.println("Resource not found, skipping deletion:");
    }
    return containPrefixAndZone;
  }

  public static boolean isCreatedBeforeThresholdTime(String timestamp) {
    return OffsetDateTime.parse(timestamp).toInstant()
        .isBefore(Instant.now().minus(DELETION_THRESHOLD_TIME_MINUTES, ChronoUnit.MINUTES));
  }

  private static String formatTimestamp(Timestamp timestamp) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
        ZoneOffset.UTC);
    return formatter.format(offsetDateTime);
  }
}
