package vtwo.source;

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.FindingName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.UpdateFindingRequest;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import java.io.IOException;
import java.time.Instant;

public class UpdateFindingSource {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the below variables.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    // Specify the source-id.
    String sourceId = "{source-id}";

    // Specify the finding-id.
    String findingId = "{finding-id}";

    updateFinding(organizationId, location,sourceId,findingId);
  }

  // Creates or updates a finding.
  public static Finding updateFinding(String organizationId,
      String location, String sourceId, String findingId) {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Optionally FindingName or String can be used.
     //  String findingName = String.format("organizations/%s/sources/%s/locations/%s/findings/%s",
     // organizationId,sourceId,location,findingId);
      FindingName findingName = FindingName
          .ofOrganizationSourceLocationFindingName(organizationId, sourceId, location, findingId);
      // Use the current time as the finding "event time".
      Instant eventTime = Instant.now();

      // Define source properties values as protobuf "Value" objects.
      Value stringValue = Value.newBuilder().setStringValue("value").build();

      FieldMask updateMask =
          FieldMask.newBuilder()
              .addPaths("event_time")
              .addPaths("source_properties.stringKey")
              .build();

      Finding finding =
          Finding.newBuilder()
              .setName(findingName.toString())
              .setDescription("Updated finding source")
              .setEventTime(
                  Timestamp.newBuilder()
                      .setSeconds(eventTime.getEpochSecond())
                      .setNanos(eventTime.getNano()))
              .putSourceProperties("stringKey", stringValue)
              .build();

      UpdateFindingRequest.Builder request =
          UpdateFindingRequest.newBuilder().setFinding(finding).setUpdateMask(updateMask);

      // Call the API.
      Finding response = client.updateFinding(request.build());

      System.out.println("Updated finding source: " + response);
      return response;
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
}
