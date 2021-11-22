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

// [START functions_log_cloudevent]
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesSetLabelsRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.SetLabelsInstanceRequest;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class AutoLabelInstance implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(LogCloudEvent.class.getName());

  @Override
  public void accept(CloudEvent event) throws Exception {

    if (event.getData() != null) {
      // Extract data and convert to JSON object
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
      Gson gson = new Gson();
      JsonObject eventData = gson.fromJson(cloudEventData, JsonObject.class);

      // Extract the Cloud Audit Logging entry from the protoPayload
      JsonObject payload = eventData.getAsJsonObject("protoPayload");
      JsonObject auth = payload.getAsJsonObject("authenticationInfo");

      if (auth == null) throw new Exception("Creator not specified");
      // The email address of the authenticated user 
      // (or service account on behalf of third party principal) making the request
      String creator = auth.get("principleEmail").getAsString();
      // Format the 'creator' parameter to match GCE label validation requirements
      creator = creator.toLowerCase().replaceAll("/\\W/g", "-");

      // Get relevant VM instance details from the cloudevent's `subject` property
      // Example value:
      //   compute.googleapis.com/projects/<PROJECT>/zones/<ZONE>/instances/<INSTANCE>
      String subject = event.getSubject();
      String[] params = subject.split("/");

      // Validate data
      if (params.length < 7) throw new Exception("Params not right");

      try (InstancesClient instancesClient = InstancesClient.create()) {
        String project = params[2];
        String zone = params[4];
        String instanceName = params[6];
        
        // Get the newly-created VM instance's label fingerprint
        // This is required by the Compute Engine API to prevent duplicate labels
        GetInstanceRequest getInstanceRequest =
          GetInstanceRequest.newBuilder()
              .setInstance(instanceName)
              .setProject(project)
              .setZone(zone)
              .build();
        Instance instance = instancesClient.get(getInstanceRequest);
        String fingerPrint = instance.getFingerprint();

        // Label the instance with its creator
        SetLabelsInstanceRequest setLabelRequest = 
          SetLabelsInstanceRequest.newBuilder()
            .setInstance(instanceName)
            .setProject(project)
            .setZone(zone)
            .setInstancesSetLabelsRequestResource(
              InstancesSetLabelsRequest.newBuilder()
                .putLabels("creator", creator)
                .setLabelFingerprint(fingerPrint))
            .build();

        Operation response =
            instancesClient.setLabels(setLabelRequest);
      }
    }
  }
}
// [END functions_log_cloudevent]
