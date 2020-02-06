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

package dlp.snippets;

// [START dlp_list_jobs]

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.DlpJobType;
import com.google.privacy.dlp.v2.ListDlpJobsRequest;
import com.google.privacy.dlp.v2.ProjectName;

import java.io.IOException;

public class JobsList {

    public static void listJobs() throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        // For more info on filters and job types,
        // see https://cloud.google.com/dlp/docs/reference/rest/v2/projects.dlpJobs/list
        String projectId = "your-project-id";
        String filter = "state=DONE";
        String jobType = "INSPECT_JOB";
        listJobs(projectId, filter, jobType);
    }

    // Lists DLP jobs matching a filter and jobType
    public static void listJobs(String projectId, String filter, String jobType) throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

            // Construct the request to be sent by the client.
            ListDlpJobsRequest listDlpJobsRequest =
                    ListDlpJobsRequest.newBuilder()
                            .setParent(ProjectName.of(projectId).toString())
                            .setFilter(filter)
                            .setType(DlpJobType.valueOf(jobType))
                            .build();

            // Send the request to list jobs and process the response
            DlpServiceClient.ListDlpJobsPagedResponse response =
                    dlpServiceClient.listDlpJobs(listDlpJobsRequest);
            for (DlpJob dlpJob : response.getPage().getValues()) {
                System.out.println(dlpJob.getName() + " -- " + dlpJob.getState());
            }
        }
    }
}
// [END dlp_list_jobs]