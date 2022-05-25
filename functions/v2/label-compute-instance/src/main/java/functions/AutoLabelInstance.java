/*
 * Copyright 2021 Google LLC
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

package functions;

// [START functions_label_gce_instance]
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSetLabelsRequest;
import com.google.cloud.compute.v1.SetLabelsInstanceRequest;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class AutoLabelInstance implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(AutoLabelInstance.class.getName());

  @Override
  public void accept(CloudEvent event) throws Exception {
    // Extract CloudEvent data
    if (event.getData() != null) {
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);

      // Convert data to JSON
      JsonObject eventData;
      try {
        Gson gson = new Gson();
        eventData = gson.fromJson(cloudEventData, JsonObject.class);
      } catch (JsonSyntaxException error) {
        throw new RuntimeException("CloudEvent data is not valid JSON: " + error.getMessage());
      }

      // Extract the Cloud Audit Logging entry from the data's protoPayload
      JsonObject payload = eventData.getAsJsonObject("protoPayload");
      JsonObject auth = payload.getAsJsonObject("authenticationInfo");

      // Extract the email address of the authenticated user
      // (or service account on behalf of third party principal) making the request
      String creator = auth.get("principalEmail").getAsString();
      if (creator == null) {
        throw new RuntimeException("`principalEmail` not found in protoPayload.");
      }
      // Format the 'creator' parameter to match GCE label validation requirements
      creator = creator.toLowerCase().replaceAll("\\W", "-");

      // Get relevant VM instance details from the CloudEvent `subject` property
      // Example: compute.googleapis.com/projects/<PROJECT>/zones/<ZONE>/instances/<INSTANCE>
      String subject = event.getSubject();
      if (subject == null || subject == "") {
        throw new RuntimeException("Missing CloudEvent `subject`.");
      }
      String[] params = subject.split("/");

      // Validate data
      if (params.length < 7) {
        throw new RuntimeException("Can not parse resource from CloudEvent `subject`: " + subject);
      }
      String project = params[2];
      String zone = params[4];
      String instanceName = params[6];

      // Instantiate the Compute Instances client
      try (InstancesClient instancesClient = InstancesClient.create()) {
        // Get the newly-created VM instance's label fingerprint
        // This is required by the Compute Engine API to prevent duplicate labels
        GetInstanceRequest getInstanceRequest =
            GetInstanceRequest.newBuilder()
                .setInstance(instanceName)
                .setProject(project)
                .setZone(zone)
                .build();
        Instance instance = instancesClient.get(getInstanceRequest);
        String fingerPrint = instance.getLabelFingerprint();

        // Label the instance with its creator
        SetLabelsInstanceRequest setLabelRequest =
            SetLabelsInstanceRequest.newBuilder()
                .setInstance(instanceName)
                .setProject(project)
                .setZone(zone)
                .setInstancesSetLabelsRequestResource(
                    InstancesSetLabelsRequest.newBuilder()
                        .putLabels("creator", creator)
                        .setLabelFingerprint(fingerPrint)
                        .build())
                .build();

        instancesClient.setLabels(setLabelRequest);
        logger.info(
            String.format(
                "Adding label, \"{'creator': '%s'}\", to instance, \"%s\".",
                creator, instanceName));
      } catch (Exception error) {
        throw new RuntimeException(
            String.format(
                "Error trying to label VM instance, %s: %s", instanceName, error.toString()));
      }
    }
  }
}
// [END functions_label_gce_instance]
