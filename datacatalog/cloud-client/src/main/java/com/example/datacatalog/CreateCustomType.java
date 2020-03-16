/*
 * Copyright 2020 Google Inc.
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

package com.example.datacatalog;

// [START datacatalog_custom_entry_tag]

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.NotFoundException;
import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.cloud.datacatalog.v1beta1.*;

import java.io.IOException;


public class CreateCustomType {


public static void createCustomType() {
  // TODO: Set these values before running the sample.
  String projectId = "my-project";
  String entryGroupId = "onprem_entry_group";
  String entryId = "onprem_entry_id";
  String tagTemplateId = "onprem_tag_template";
  createCustomType(projectId, entryGroupId, entryId, tagTemplateId);
}

public static void createCustomType(String projectId, String entryGroupId, String entryId, String tagTemplateId) {
  // Currently, Data Catalog stores metadata in the us-central1 region.
  String location = "us-central1";

  // Initialize client that will be used to send requests. This client only needs to be created
  // once, and can be reused for multiple requests. After completing all of your requests, call
  // the "close" method on the client to safely clean up any remaining background resources.
  try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {

    // 1. Environment cleanup: delete pre-existing data.
    // Delete any pre-existing Entry with the same name
    // that will be used in step 3.
    try {
      String entryName = EntryName.of(projectId, location, entryGroupId, entryId).toString();
      dataCatalogClient.deleteEntry(entryName);
      System.out.printf("\nDeleted Entry: %s", entryName);
    } catch (PermissionDeniedException | NotFoundException e) {
      // PermissionDeniedException or NotFoundException are thrown if
      // Entry does not exist.
      System.out.println("Entry does not exist.");
    }

    // Delete any pre-existing Entry Group with the same name
    // that will be used in step 2.
    try {
      String entryGroupName = EntryGroupName.of(projectId, location, entryGroupId).toString();
      dataCatalogClient.deleteEntryGroup(entryGroupName);
      System.out.printf("\nDeleted Entry Group: %s", entryGroupName);
    } catch (PermissionDeniedException | NotFoundException e) {
      // PermissionDeniedException or NotFoundException are thrown if
      // Entry Group does not exist.
      System.out.println("Entry Group does not exist.");
    }

    String tagTemplateName =
        TagTemplateName.newBuilder()
            .setProject(projectId)
            .setLocation(location)
            .setTagTemplate(tagTemplateId)
            .build()
            .toString();

    // Delete any pre-existing Template with the same name
    // that will be used in step 4.
    try {
      dataCatalogClient.deleteTagTemplate(
          DeleteTagTemplateRequest.newBuilder()
              .setName(tagTemplateName)
              .setForce(true)
              .build());
      System.out.printf("\nDeleted template: %s", tagTemplateName);
    } catch (Exception e) {
      System.out.printf("\nCannot delete template: %s", tagTemplateName);
    }

    // 2. Create an Entry Group.
    // Construct the EntryGroup for the EntryGroup request.
    EntryGroup entryGroup =
        EntryGroup.newBuilder()
            .setDisplayName("My awesome Entry Group")
            .setDescription("This Entry Group represents an external system")
            .build();

    // Construct the EntryGroup request to be sent by the client.
    CreateEntryGroupRequest entryGroupRequest =
        CreateEntryGroupRequest.newBuilder()
            .setParent(LocationName.of(projectId, location).toString())
            .setEntryGroupId(entryGroupId)
            .setEntryGroup(entryGroup)
            .build();

    // Use the client to send the API request.
    EntryGroup createdEntryGroup = dataCatalogClient.createEntryGroup(entryGroupRequest);

    System.out.printf("\nEntry Group created with name: %s", createdEntryGroup.getName());

    // 3. Create an Entry.
    // Construct the Entry for the Entry request.
    Entry entry =
        Entry.newBuilder()
            .setUserSpecifiedSystem("onprem_data_system")
            .setUserSpecifiedType("onprem_data_asset")
            .setDisplayName("My awesome data asset")
            .setDescription("This data asset is managed by an external system.")
            .setLinkedResource("//my-onprem-server.com/dataAssets/my-awesome-data-asset")
            .setSchema(
                Schema.newBuilder()
                    .addColumns(
                        ColumnSchema.newBuilder()
                            .setColumn("first_column")
                            .setDescription("This columns consists of ....")
                            .setMode("NULLABLE")
                            .setType("DOUBLE")
                            .build())
                    .addColumns(
                        ColumnSchema.newBuilder()
                            .setColumn("second_column")
                            .setDescription("This columns consists of ....")
                            .setMode("REQUIRED")
                            .setType("STRING")
                            .build())
                    .build())
            .build();

    // Construct the Entry request to be sent by the client.
    CreateEntryRequest entryRequest =
        CreateEntryRequest.newBuilder()
            .setParent(createdEntryGroup.getName())
            .setEntryId(entryId)
            .setEntry(entry)
            .build();

    // Use the client to send the API request.
    Entry createdEntry = dataCatalogClient.createEntry(entryRequest);
    System.out.printf("\nCreated entry: %s", createdEntry.getName());

    // 4. Create a Tag Template.
    // For more field types, including ENUM, please refer to
    // https://cloud.google.com/data-catalog/docs/quickstarts/quickstart-search-tag#data-catalog-quickstart-java.
    TagTemplateField sourceField =
        TagTemplateField.newBuilder()
            .setDisplayName("Source of data asset")
            .setType(FieldType.newBuilder().setPrimitiveType(
                FieldType.PrimitiveType.STRING).build())
            .build();

    TagTemplate tagTemplate =
        TagTemplate.newBuilder()
            .setDisplayName("Demo Tag Template")
            .putFields("source", sourceField)
            .build();

    CreateTagTemplateRequest createTagTemplateRequest =
        CreateTagTemplateRequest.newBuilder()
            .setParent(
                LocationName.newBuilder()
                    .setProject(projectId)
                    .setLocation(location)
                    .build()
                    .toString())
            .setTagTemplateId(tagTemplateId)
            .setTagTemplate(tagTemplate)
            .build();

    TagTemplate createdTagTemplate = dataCatalogClient.createTagTemplate(createTagTemplateRequest);
    System.out.printf("\nCreated template: %s", createdTagTemplate.getName());

    TagField sourceValue =
        TagField.newBuilder().setStringValue("On-premises system name").build();

    Tag tag =
        Tag.newBuilder()
            .setTemplate(createdTagTemplate.getName())
            .putFields("source", sourceValue)
            .build();

    CreateTagRequest createTagRequest =
        CreateTagRequest.newBuilder().setParent(createdEntry.getName()).setTag(tag).build();

    Tag createdTag = dataCatalogClient.createTag(createTagRequest);
    System.out.printf("\nCreated tag: %s", createdTag.getName());

  } catch (AlreadyExistsException | IOException e) {
    // AlreadyExistsException's are thrown if EntryGroup or Entry already exists.
    // IOException's are thrown when unable to create the DataCatalogClient,
    // for example an invalid Service Account path.
    System.out.println("Error creating entry:\n" + e.toString());
  }
}

public static void main(String[] args) {
  createCustomType();
  }
}

// [END datacatalog_custom_entry_tag]