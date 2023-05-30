/*
 * Copyright 2023 Google LLC
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

package contentwarehouse.v1;
import com.google.cloud.contentwarehouse.v1.RuleSet;
import com.google.cloud.contentwarehouse.v1.RuleSetOrBuilder;
import com.google.cloud.contentwarehouse.v1.CreateRuleSetRequest;
import com.google.cloud.contentwarehouse.v1.CreateRuleSetRequestOrBuilder;
import com.google.cloud.contentwarehouse.v1.DeleteDocumentAction;
import com.google.cloud.contentwarehouse.v1.DeleteDocumentActionOrBuilder;
import com.google.cloud.contentwarehouse.v1.ListRuleSetsRequest;
import com.google.cloud.contentwarehouse.v1.ListRuleSetsRequestOrBuilder;
import com.google.cloud.contentwarehouse.v1.RuleSetServiceClient;
import com.google.cloud.contentwarehouse.v1.LocationName;
import com.google.cloud.contentwarehouse.v1.Rule;
import com.google.cloud.contentwarehouse.v1.RuleOrBuilder;
import com.google.cloud.contentwarehouse.v1.RuleSetServiceSettings;
import com.google.cloud.contentwarehouse.v1.Rule.TriggerType;
import com.google.cloud.contentwarehouse.v1.RuleSetServiceClient.ListRuleSetsPagedResponse;
import com.google.cloud.contentwarehouse.v1.Action;
import com.google.cloud.contentwarehouse.v1.ActionOrBuilder;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;

import java.io.IOException;

// [START contentwarehouse_createruleset]
public class CreateRuleSet {

    public static void createRuleSet() throws IOException { 
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    createRuleSet(projectId,location);
    }

    public static void createRuleSet(String projectId, String location)
        throws IOException {
        String projectNumber = getProjectNumber(projectId);

        
        String endpoint = String.format("%s-contentwarehouse.googleapis.com:443", location);
        RuleSetServiceSettings ruleSetServiceSettings =
            RuleSetServiceSettings.newBuilder().setEndpoint(endpoint).build();
        
        // Create a Rule Set Service Client 
        try(RuleSetServiceClient ruleSetServiceClient = 
            RuleSetServiceClient.create(ruleSetServiceSettings)){
            /*  The full resource name of the location, e.g.:
            projects/{project_number}/locations/{location} */
            String parent = LocationName.format(projectNumber, location); 

            // Create a Delete Document Action to be added to the Rule Set 
            DeleteDocumentActionOrBuilder deleteDocumentAction = 
                DeleteDocumentAction.newBuilder().setEnableHardDelete(true);

            // Add Delete Document Action to Action Object 
            ActionOrBuilder action = Action.newBuilder()
            .setDeleteDocumentAction((DeleteDocumentAction) deleteDocumentAction);
            
            // Create rule to add to rule set 
            RuleOrBuilder rule = Rule.newBuilder()
                .setTriggerType(TriggerType.ON_CREATE)
                .setCondition("documentType == 'W9' && STATE =='CA' ")
                .setActions(0, (Action) action).build();
            
            // Create rule set and add rule to it
            RuleSetOrBuilder ruleSetOrBuilder = RuleSet.newBuilder()
                .setDescription("W9: Basic validation check rules.")
                .setSource("My Organization")
                .addRules((Rule) rule).build();

            // Create and prepare rule set request to client
            CreateRuleSetRequestOrBuilder createRuleSetRequest = 
                CreateRuleSetRequest.newBuilder()
                    .setParent(parent)
                    .setRuleSet((RuleSet) ruleSetOrBuilder).build();
            
            RuleSet response = ruleSetServiceClient.createRuleSet((CreateRuleSetRequest) createRuleSetRequest);

            System.out.println("Rule set created: "+ response.toString());

            ListRuleSetsRequestOrBuilder listRuleSetsRequest = 
                ListRuleSetsRequest.newBuilder()
                    .setParent(parent);

            ListRuleSetsPagedResponse listRuleSetsPagedResponse = 
                ruleSetServiceClient.listRuleSets((ListRuleSetsRequest) listRuleSetsRequest);
            
            while(listRuleSetsPagedResponse.getNextPageToken() != null){ 
                System.out.println("Rule sets:" + listRuleSetsPagedResponse.getPage().toString());
            }
        }
    }

    private static String getProjectNumber(String projectId) throws IOException { 
        try (ProjectsClient projectsClient = ProjectsClient.create()) { 
          ProjectName projectName = ProjectName.of(projectId); 
          Project project = projectsClient.getProject(projectName);
          String projectNumber = project.getName(); // Format returned is projects/xxxxxx
          return projectNumber.substring(projectNumber.lastIndexOf("/") + 1);
        } 
      }
}
// [END contentwarehouse_createruleset]

