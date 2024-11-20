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

// [START tpu_queued_resources_time_bound]
import com.google.cloud.tpu.v2alpha1.CreateQueuedResourceRequest;
import com.google.cloud.tpu.v2alpha1.Node;
import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import com.google.protobuf.Duration;
// Uncomment the following line to use Interval or Date
//import com.google.protobuf.Timestamp;
//import com.google.type.Interval;
//import java.util.Date;
//import java.time.Instant;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateTimeBoundQueuedResource {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to create a node.
    String projectId = "YOUR_PROJECT_ID";
    // The zone in which to create the TPU.
    // For more information about supported TPU types for specific zones,
    // see https://cloud.google.com/tpu/docs/regions-zones
    String zone = "europe-west4-a";
    // The name for your TPU.
    String nodeName = "YOUR_NODE_ID";
    // The accelerator type that specifies the version and size of the Cloud TPU you want to create.
    // For more information about supported accelerator types for each TPU version,
    // see https://cloud.google.com/tpu/docs/system-architecture-tpu-vm#versions.
    String tpuType = "v2-8";
    // Software version that specifies the version of the TPU runtime to install.
    // For more information see https://cloud.google.com/tpu/docs/runtimes
    String tpuSoftwareVersion = "tpu-vm-tf-2.14.1";
    // The name for your Queued Resource.
    String queuedResourceId = "QUEUED_RESOURCE_ID";


    createTimeBoundQueuedResource(projectId, nodeName,
        queuedResourceId, zone, tpuType, tpuSoftwareVersion);
  }

  // Creates a Queued Resource with time bound configuration.
  public static QueuedResource createTimeBoundQueuedResource(
      String projectId, String nodeName, String queuedResourceName,
      String zone, String tpuType, String tpuSoftwareVersion)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (TpuClient tpuClient = TpuClient.create()) {
      // Define parent for requests
      String parent = String.format("projects/%s/locations/%s", projectId, zone);
      // Create a Duration object representing 6 hours.
      Duration validAfterDuration = Duration.newBuilder().setSeconds(6 * 3600).build();
      // Uncomment the appropriate lines to use other time bound configurations
      // Duration validUntilDuration = Duration.newBuilder().setSeconds(6 * 3600).build();
      // String validAfterTime = "2024-10-14T09:00:00Z";
      // String validUntilTime = "2024-12-14T09:00:00Z";

      // Create a node
      Node node =
          Node.newBuilder()
              .setName(nodeName)
              .setAcceleratorType(tpuType)
              .setRuntimeVersion(tpuSoftwareVersion)
              .setQueuedResource(
                  String.format(
                      "projects/%s/locations/%s/queuedResources/%s",
                      projectId, zone, queuedResourceName))
              .build();

      // Create queued resource
      QueuedResource queuedResource =
          QueuedResource.newBuilder()
              .setName(queuedResourceName)
              .setTpu(
                  QueuedResource.Tpu.newBuilder()
                      .addNodeSpec(
                          QueuedResource.Tpu.NodeSpec.newBuilder()
                              .setParent(parent)
                              .setNode(node)
                              .setNodeId(nodeName)
                              .build())
                      .build())
              .setQueueingPolicy(
                  QueuedResource.QueueingPolicy.newBuilder()
                      // You can specify a duration after which a resource should be allocated.
                      // corresponds   --valid-after-duration flag
                      .setValidAfterDuration(validAfterDuration)
                      .build())
              // Uncomment the appropriate lines to use other time bound configurations
              //.setQueueingPolicy(
              //QueuedResource.QueueingPolicy.newBuilder()
              // You can specify a time after which a resource should be allocated.
              // corresponds  --valid-until-duration flag
              //.setValidUntilDuration(validUntilDuration)
              //.build())
              //.setQueueingPolicy(
              //QueuedResource.QueueingPolicy.newBuilder()
              // You can specify a time before which the resource should be allocated.
              // corresponds  --valid-after-time flag
              //.setValidAfterTime(convertStringToTimestamp(validAfterTime))
              //.build())
              //.setQueueingPolicy(
              //QueuedResource.QueueingPolicy.newBuilder()
              // You can specify a time after which the resource should be allocated.
              // corresponds  --valid-until-time flag
              //.setValidUntilTime(convertStringToTimestamp(validUntilTime))
              //.build())
              //.setQueueingPolicy(
              //QueuedResource.QueueingPolicy.newBuilder()
              // You can specify a time interval before and after which
              // the resource should be allocated.
              //.setValidInterval(
              //Interval.newBuilder()
              //.setStartTime(convertStringToTimestamp(validAfterTime))
              //.setEndTime(convertStringToTimestamp(validUntilTime))
              //.build())
              //.build())
              .build();

      CreateQueuedResourceRequest request =
          CreateQueuedResourceRequest.newBuilder()
              .setParent(parent)
              .setQueuedResource(queuedResource)
              .setQueuedResourceId(queuedResourceName)
              .build();

      // You can wait until TPU Node is READY,
      // and check its status using getTpuVm() from "tpu_vm_get" sample.
      return tpuClient.createQueuedResourceAsync(request).get();
    }
  }

  // Uncomment this method if you want to use time bound configurations
  // Method to convert a string timestamp to a Protobuf Timestamp object
  //  private static Timestamp convertStringToTimestamp(String timestampString) {
  //    Instant instant = Instant.parse(timestampString);
  //    return Timestamp.newBuilder()
  //        .setSeconds(instant.getEpochSecond())
  //        .setNanos(instant.getNano())
  //        .build();
  //  }
}
// [END tpu_queued_resources_time_bound]