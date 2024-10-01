/*
 * Copyright 2024 Google Inc.
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

// [START dataplex_create_aspect_type]
import com.google.cloud.dataplex.v1.AspectType;
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.LocationName;

// Sample to create Aspect Type
public class CreateAspectType {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    String location = "MY_LOCATION";
    String aspectTypeId = "MY_ASPECT_TYPE_ID";

    LocationName locationName = LocationName.of(projectId, location);
    createAspectType(locationName, aspectTypeId);
  }

  public static void createAspectType(LocationName locationName, String aspectTypeId)
      throws Exception {
    AspectType aspectType =
        AspectType.newBuilder()
            .setDescription("description of the aspect type")
            .setMetadataTemplate(
                AspectType.MetadataTemplate.newBuilder()
                    // Name cannot have spaces.
                    .setName("name_of_the_template")
                    // Metadata Template is recursive structure,
                    // Type "record" indicates it is not a leaf field.
                    .setType("record")
                    // Aspect Type fields, that themselves are Metadata Template
                    .addRecordFields(
                        AspectType.MetadataTemplate.newBuilder()
                            // Name cannot have spaces.
                            .setName("name_of_the_field")
                            .setType("string")
                            .setIndex(1)
                            .setAnnotations(
                                AspectType.MetadataTemplate.Annotations.newBuilder()
                                    .setDescription("description of the field")
                                    .build())
                            .setConstraints(
                                AspectType.MetadataTemplate.Constraints.newBuilder()
                                    // Specified, if field will be required in Aspect Type
                                    .setRequired(true)
                                    .build())
                            .build())
                    .build())
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources,
    // or use "try-with-close" statement to do this automatically.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      AspectType createdAspectType =
          client.createAspectTypeAsync(locationName, aspectType, aspectTypeId).get();
      System.out.println("Successfully created aspect type: " + createdAspectType.getName());
    }
  }
}
// [END dataplex_create_aspect_type]
