/*
 * Copyright 2020 Google LLC
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

package webrisk;

// [START webrisk_submit_uri]

import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import com.google.longrunning.Operation;
import com.google.webrisk.v1.Submission;
import com.google.webrisk.v1.SubmitUriRequest;
import com.google.webrisk.v1.ThreatDiscovery;
import com.google.webrisk.v1.ThreatDiscovery.Platform;
import com.google.webrisk.v1.ThreatInfo;
import com.google.webrisk.v1.ThreatInfo.AbuseType;
import com.google.webrisk.v1.ThreatInfo.Confidence;
import com.google.webrisk.v1.ThreatInfo.Confidence.ConfidenceLevel;
import com.google.webrisk.v1.ThreatInfo.ThreatJustification;
import com.google.webrisk.v1.ThreatInfo.ThreatJustification.JustificationLabel;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SubmitUri {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The name of the project that is making the submission.
    String projectId = "GOOGLE_CLOUD_PROJECT";
    // The URI that is being reported for malicious content to be analyzed.
    String uri = "http://testsafebrowsing.appspot.com/s/malware.html";

    submitUri(projectId, uri);
  }

  // Submits a URI suspected of containing malicious content to be reviewed. Returns a
  // google.longrunning.Operation which, once the review is complete, is updated with its result.
  // You can use the [Pub/Sub API] (https://cloud.google.com/pubsub) to receive notifications for
  // the returned Operation.
  // If the result verifies the existence of malicious content, the site will be added to the
  // Google's Social Engineering lists in order to protect users that could get exposed to this
  // threat in the future. Only allow-listed projects can use this method during Early Access.
  public static void submitUri(String projectId, String uri)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (WebRiskServiceClient webRiskServiceClient = WebRiskServiceClient.create()) {

      // Set the URI to be submitted.
      Submission submission = Submission.newBuilder()
          .setUri(uri)
          .build();

      // Set the context about the submission including the type of abuse found on the URI and
      // supporting details.
      ThreatInfo threatInfo = ThreatInfo.newBuilder()
          // The abuse type found on the URI.
          .setAbuseType(AbuseType.SOCIAL_ENGINEERING)
          // Confidence that a URI is unsafe.
          .setThreatConfidence(Confidence.newBuilder()
              .setLevel(ConfidenceLevel.MEDIUM)
              .build())
          // Context about why the URI is unsafe.
          .setThreatJustification(ThreatJustification.newBuilder()
              // Labels that explain how the URI was classified.
              .addLabels(JustificationLabel.AUTOMATED_REPORT)
              // Free-form context on why this URI is unsafe.
              .addComments("Testing Submission")
              .build())
          .build();

      // Set the details about how the threat was discovered.
      ThreatDiscovery threatDiscovery = ThreatDiscovery.newBuilder()
          // Platform on which the threat was discovered.
          .setPlatform(Platform.MACOS)
          // CLDR region code of the countries/regions the URI poses a threat ordered
          // from most impact to least impact. Example: "US" for United States.
          .addRegionCodes("US")
          .build();

      SubmitUriRequest submitUriRequest = SubmitUriRequest.newBuilder()
          .setParent(String.format("projects/%s", projectId))
          .setSubmission(submission)
          .setThreatInfo(threatInfo)
          .setThreatDiscovery(threatDiscovery)
          .build();

      Operation submissionResponse = webRiskServiceClient.submitUriCallable()
          .futureCall(submitUriRequest)
          .get(30, TimeUnit.SECONDS);

      System.out.println("Submission response: " + submissionResponse);
    }
  }
}
// [END webrisk_submit_uri]
