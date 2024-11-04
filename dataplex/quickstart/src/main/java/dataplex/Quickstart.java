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

package dataplex;

// [START dataplex_quickstart]
import com.google.cloud.dataplex.v1.Aspect;
import com.google.cloud.dataplex.v1.AspectType;
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.EntryGroup;
import com.google.cloud.dataplex.v1.EntryGroupName;
import com.google.cloud.dataplex.v1.EntryName;
import com.google.cloud.dataplex.v1.EntrySource;
import com.google.cloud.dataplex.v1.EntryType;
import com.google.cloud.dataplex.v1.EntryView;
import com.google.cloud.dataplex.v1.GetEntryRequest;
import com.google.cloud.dataplex.v1.LocationName;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Quickstart {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";

    quickstart(projectId, location);
  }

  // Method to demonstrate lifecycle of different Dataplex resources and their interactions.
  // Method creates Aspect Type, Entry Type, Entry Group and Entry, retrieves Entry
  // and cleans up created resources.
  public static void quickstart(String projectId, String location) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      // 0) Prepare variables used in following steps
      String aspectTypeId = "dataplex-quickstart-aspect-type";
      String aspectKey = String.format("%s.global.%s", projectId, aspectTypeId);
      String entryTypeId = "dataplex-quickstart-entry-type";
      String entryGroupId = "dataplex-quickstart-entry-group";
      String entryId = "dataplex-quickstart-entry";
      LocationName globalLocationName = LocationName.of(projectId, "global");
      LocationName specificLocationName = LocationName.of(projectId, location);

      // 1) Create Aspect Type that will be attached to Entry Type
      AspectType.MetadataTemplate aspectField =
          AspectType.MetadataTemplate.newBuilder()
              // The name must follow regex ^(([a-zA-Z]{1})([\\w\\-_]{0,62}))$
              // That means name must only contain alphanumeric character or dashes or underscores,
              // start with an alphabet, and must be less than 63 characters.
              .setName("example_field")
              // Metadata Template is recursive structure,
              // primitive types such as "string" or "integer" indicate leaf node,
              // complex types such as "record" or "array" would require nested Metadata Template
              .setType("string")
              .setIndex(1)
              .setAnnotations(
                  AspectType.MetadataTemplate.Annotations.newBuilder()
                      .setDescription("example field to be filled during entry creation")
                      .build())
              .setConstraints(
                  AspectType.MetadataTemplate.Constraints.newBuilder()
                      // Specifies if field will be required in Aspect Type.
                      .setRequired(true)
                      .build())
              .build();
      AspectType aspectType =
          AspectType.newBuilder()
              .setDescription("aspect type for dataplex quickstart")
              .setMetadataTemplate(
                  AspectType.MetadataTemplate.newBuilder()
                      .setName("example_template")
                      .setType("record")
                      // Aspect Type fields, that themselves are Metadata Templates
                      .addAllRecordFields(List.of(aspectField))
                      .build())
              .build();
      AspectType createdAspectType =
          client
              .createAspectTypeAsync(
                  // Aspect Type is created in "global" location to highlight, that resources from
                  // "global" region can be attached Entry created in specific location
                  globalLocationName, aspectType, aspectTypeId)
              .get();
      System.out.println("Step 1: Created aspect type -> " + createdAspectType.getName());

      // 2) Create Entry Type, of which type Entry will be created
      EntryType entryType =
          EntryType.newBuilder()
              .setDescription("entry type for dataplex quickstart")
              .addRequiredAspects(
                  EntryType.AspectInfo.newBuilder()
                      // Aspect Type created in step 1
                      .setType(
                          String.format(
                              "projects/%s/locations/global/aspectTypes/%s",
                              projectId, aspectTypeId))
                      .build())
              .build();
      EntryType createdEntryType =
          client
              // Entry Type is created in "global" location to highlight, that resources from
              // "global" region can be attached Entry created in specific location
              .createEntryTypeAsync(globalLocationName, entryType, entryTypeId)
              .get();
      System.out.println("Step 2: Created entry type -> " + createdEntryType.getName());

      // 3) Create Entry Group in which Entry will be located
      EntryGroup entryGroup =
          EntryGroup.newBuilder().setDescription("entry group for dataplex quickstart").build();
      EntryGroup createdEntryGroup =
          client
              // Entry Group is created for specific location
              .createEntryGroupAsync(specificLocationName, entryGroup, entryGroupId)
              .get();
      System.out.println("Step 3: Created entry group -> " + createdEntryGroup.getName());

      // 4) Create Entry
      Entry entry =
          Entry.newBuilder()
              .setEntryType(
                  // Entry is an instance of Entry Type created in step 2
                  String.format(
                      "projects/%s/locations/global/entryTypes/%s", projectId, entryTypeId))
              .setEntrySource(
                  EntrySource.newBuilder().setDescription("entry for dataplex quickstart").build())
              .putAllAspects(
                  Map.of(
                      // Attach Aspect that is an instance of Aspect Type created in step 1
                      aspectKey,
                      Aspect.newBuilder()
                          .setAspectType(
                              String.format(
                                  "projects/%s/locations/global/aspectTypes/%s",
                                  projectId, aspectTypeId))
                          .setData(
                              Struct.newBuilder()
                                  .putFields(
                                      "example_field",
                                      Value.newBuilder()
                                          .setStringValue("example value for the field")
                                          .build())
                                  .build())
                          .build()))
              .build();
      Entry createdEntry =
          client.createEntry(
              // Entry is created in specific location, but it is still possible to link it with
              // resources (Aspect Type and Entry Type) from "global" location
              EntryGroupName.of(projectId, location, entryGroupId), entry, entryId);
      System.out.println("Step 4: Created entry -> " + createdEntry.getName());

      // 5) Retrieve created Entry
      GetEntryRequest getEntryRequest =
          GetEntryRequest.newBuilder()
              .setName(EntryName.of(projectId, location, entryGroupId, entryId).toString())
              .setView(EntryView.FULL)
              .build();
      Entry retreivedEntry = client.getEntry(getEntryRequest);
      System.out.println("Step 5: Retrieved entry -> " + retreivedEntry.getName());
      retreivedEntry
          .getAspectsMap()
          .values()
          .forEach(
              retreivedAspect -> {
                System.out.println("Retrieved aspect for entry:");
                System.out.println(" * aspect type -> " + retreivedAspect.getAspectType());
                System.out.println(
                    " * aspect field value -> "
                        + retreivedAspect
                            .getData()
                            .getFieldsMap()
                            .get("example_field")
                            .getStringValue());
              });

      // 6) Clean created resources
      client
          .deleteEntryGroupAsync(
              String.format(
                  "projects/%s/locations/%s/entryGroups/%s", projectId, location, entryGroupId))
          .get();
      client
          .deleteEntryTypeAsync(
              String.format("projects/%s/locations/global/entryTypes/%s", projectId, entryTypeId))
          .get();
      client
          .deleteAspectTypeAsync(
              String.format("projects/%s/locations/global/aspectTypes/%s", projectId, aspectTypeId))
          .get();
      System.out.println("Step 6: Successfully cleaned up resources");

    } catch (IOException | InterruptedException | ExecutionException e) {
      System.err.println("Error during quickstart execution: " + e);
    }
  }
}
// [END dataplex_quickstart]
