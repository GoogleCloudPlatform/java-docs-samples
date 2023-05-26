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
import com.google.cloud.contentwarehouse.v1.CreateRuleSetRequest;
import com.google.cloud.contentwarehouse.v1.DeleteDocumentAction;
import com.google.cloud.contentwarehouse.v1.DeleteDocumentActionOrBuilder;
import com.google.cloud.contentwarehouse.v1.RuleSetServiceClient;
import com.google.cloud.contentwarehouse.v1.LocationName;
import com.google.cloud.contentwarehouse.v1.Rule;
import com.google.cloud.contentwarehouse.v1.RuleOrBuilder;
import com.google.cloud.contentwarehouse.v1.RuleSetServiceSettings;
import com.google.cloud.contentwarehouse.v1.Rule.TriggerType;
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
                .setActions(0, (Action) action);

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
    /*

# TODO(developer): Uncomment these variables before running the sample.
# project_number = "YOUR_PROJECT_NUMBER"
# location = "us" # Format is 'us' or 'eu'


def create_rule_set(project_number: str, location: str) -> None:
    # Create a client
    client = contentwarehouse.RuleSetServiceClient()

    # The full resource name of the location, e.g.:
    # projects/{project_number}/locations/{location}
    parent = client.common_location_path(project=project_number, location=location)

    actions = contentwarehouse.Action(
        delete_document_action=contentwarehouse.DeleteDocumentAction(
            enable_hard_delete=True
        )
    )

    rules = contentwarehouse.Rule(
        trigger_type="ON_CREATE",
        condition="documentType == 'W9' && STATE =='CA'",
        actions=[actions],
    )

    rule_set = contentwarehouse.RuleSet(
        description="W9: Basic validation check rules.",
        source="My Organization",
        rules=[rules],
    )

    # Initialize request argument(s)
    request = contentwarehouse.CreateRuleSetRequest(parent=parent, rule_set=rule_set)

    # Make the request
    response = client.create_rule_set(request=request)

    # Handle the response
    print(f"Rule Set Created: {response}")

    # Initialize request argument(s)
    request = contentwarehouse.ListRuleSetsRequest(
        parent=parent,
    )

    # Make the request
    page_result = client.list_rule_sets(request=request)

    # Handle the response
    for response in page_result:
        print(f"Rule Sets: {response}")
     */
}
// [END contentwarehouse_createruleset]

