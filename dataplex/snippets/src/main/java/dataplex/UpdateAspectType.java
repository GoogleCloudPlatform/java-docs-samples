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

// [START dataplex_update_aspect_type]
import com.google.cloud.dataplex.v1.AspectType;
import com.google.cloud.dataplex.v1.AspectTypeName;
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.protobuf.FieldMask;
import java.util.List;

public class UpdateAspectType {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String aspectTypeId = "MY_ASPECT_TYPE_ID";

    AspectType.MetadataTemplate aspectField =
        AspectType.MetadataTemplate.newBuilder()
            // The name must follow regex ^(([a-zA-Z]{1})([\\w\\-_]{0,62}))$
            // That means name must only contain alphanumeric character or dashes or underscores,
            // start with an alphabet, and must be less than 63 characters.
            .setName("name_of_the_field")
            // Metadata Template is recursive structure,
            // primitive types such as "string" or "integer" indicate leaf node,
            // complex types such as "record" or "array" would require nested Metadata Template
            .setType("string")
            .setIndex(1)
            .setAnnotations(
                AspectType.MetadataTemplate.Annotations.newBuilder()
                    .setDescription("updated description of the field")
                    .build())
            .setConstraints(
                AspectType.MetadataTemplate.Constraints.newBuilder()
                    // Specifies if field will be required in Aspect Type
                    .setRequired(true)
                    .build())
            .build();
    List<AspectType.MetadataTemplate> aspectFields = List.of(aspectField);
    AspectType updatedAspectType =
        updateAspectType(projectId, location, aspectTypeId, aspectFields);
    System.out.println("Successfully updated aspect type: " + updatedAspectType.getName());
  }

  // Method to update Aspect Type located in projectId, location and with aspectTypeId and
  // aspectFields specifying schema of the Aspect Type
  public static AspectType updateAspectType(
      String projectId,
      String location,
      String aspectTypeId,
      List<AspectType.MetadataTemplate> aspectFields)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      AspectType aspectType =
          AspectType.newBuilder()
              .setName(AspectTypeName.of(projectId, location, aspectTypeId).toString())
              .setDescription("updated description of the aspect type")
              .setMetadataTemplate(
                  AspectType.MetadataTemplate.newBuilder()
                      // Because Record Fields is an array, it needs to be fully replaced.
                      // It is because you do not have a way to specify array elements in update
                      // mask.
                      .addAllRecordFields(aspectFields)
                      .build())
              .build();

      // Update mask specifies which fields will be updated.
      // For more information on update masks, see: https://google.aip.dev/161
      FieldMask updateMask =
          FieldMask.newBuilder()
              .addPaths("description")
              .addPaths("metadata_template.record_fields")
              .build();
      return client.updateAspectTypeAsync(aspectType, updateMask).get();
    }
  }
}
// [END dataplex_update_aspect_type]
