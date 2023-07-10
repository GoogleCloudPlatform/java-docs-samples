package contentwarehouse.v1;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.google.cloud.contentwarehouse.v1.DocumentName;
import com.google.cloud.contentwarehouse.v1.DocumentServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentServiceSettings;
import com.google.cloud.contentwarehouse.v1.RequestMetadata;
import com.google.cloud.contentwarehouse.v1.SetAclRequest;
import com.google.cloud.contentwarehouse.v1.SetAclResponse;
import com.google.cloud.contentwarehouse.v1.UserInfo;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;

public class SetAcl {
    public static void setAcl() throws IOException,
          InterruptedException, ExecutionException, TimeoutException {
        // TODO(developer): Replace these variables before running the samples
        String projectId = "your-project-id"; 
        String location = "your-region"; // Format is "us" or "eu"
        }

    public static void setAcl(String projectId, String location,
          String userId, String documentId) throws IOException,
          InterruptedException, ExecutionException, TimeoutException { 
        String endpoint = "contentwarehouse.googleapis.com:443";
        if (!"us".equals(location)) {
        endpoint = String.format("%s-%s", location, endpoint);
        }
        DocumentServiceSettings documentServiceSettings =
            DocumentServiceSettings.newBuilder().setEndpoint(endpoint).build();
        
        // Define Request Metadata for enforcing access control
        RequestMetadata requestMetadata = RequestMetadata.newBuilder()
            .setUserInfo(
            UserInfo.newBuilder()
              .setId(userId).build()).build();

        Binding aclBinding = Binding.newBuilder()
                        .setMembers(0, "user:andrewchasin@google.com")
                        
                        .setRole("roles/documentAdmin").build();

        Policy aclPolicy = Policy.newBuilder()
                        .setBindings(0, aclBinding).build();

        try (DocumentServiceClient documentServiceClient = 
            DocumentServiceClient.create(documentServiceSettings)) {
                if(!documentId.isEmpty()){ 
                    /* The full resource name of the document, e.g.: 
                     projects/{project_number/locations/{location/documents/{document_id} */
                    DocumentName documentName = 
                        DocumentName.ofProjectLocationDocumentName(projectId, location, documentId);
                    SetAclRequest setAclRequest = SetAclRequest.newBuilder()
                    .setResource(documentName.toString())
                    .setRequestMetadata(requestMetadata)
                    .setPolicy(aclPolicy).build();

                    SetAclResponse response = documentServiceClient.setAcl(setAclRequest);

                } else { 
                    // SetAclRequest setAclRequest = SetAclRequest.newBuilder()
                    // .setResource(String.format("%s/%s","projects",projectId))
                    // .setPolicy(Policy.newBuilder()
                    // .setBindings(0, Binding.newBuilder().setMembers(0, endpoint))
                    // .setProjectOwner(true).build();

                    // SetAclResponse response = documentServiceClient.setAcl(setAclRequest); 
                }
            }
    }
}
