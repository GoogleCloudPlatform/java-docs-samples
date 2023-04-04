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
import com.google.webrisk.v1.CreateSubmissionRequest;
import com.google.webrisk.v1.Submission;
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
  // If the result verifies the existence of malicious content, the site will be added to the
  // Google's Social Engineering lists in order to protect users that could get exposed to this
  // threat in the future. Only allow-listed projects can use this method during Early Access.
  public static void submitUri(String projectId, String uri)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `webRiskServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (WebRiskServiceClient webRiskServiceClient = WebRiskServiceClient.create()) {

      Submission submission = Submission.newBuilder()
          .setUri(uri)
          .build();

      CreateSubmissionRequest submissionRequest =
          CreateSubmissionRequest.newBuilder()
              .setParent(String.format("projects/%s", projectId))
              .setSubmission(submission)
              .build();

      Submission submissionResponse = webRiskServiceClient.createSubmissionCallable()
              .futureCall(submissionRequest).get(3, TimeUnit.MINUTES);

      System.out.println("Submission response: " + submissionResponse);
    }
  }
}
// [END webrisk_submit_uri]
