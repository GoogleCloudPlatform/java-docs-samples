/*
 * Copyright 2019 Google LLC
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

package com.google.healthcare.datasets;

// [START healthcare_deidentify_dataset]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.Dataset;
import com.google.api.services.healthcare.v1beta1.model.DeidentifyConfig;
import com.google.api.services.healthcare.v1beta1.model.DeidentifyDatasetRequest;
import com.google.api.services.healthcare.v1beta1.model.DicomConfig;
import com.google.api.services.healthcare.v1beta1.model.TagFilterList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

public class DatasetDeIdentify {
  private static final Gson GSON = new Gson();
  private static List<String> defaultDicomKeepList = ImmutableList.of("PatientID");

  public static void deidentifyDataset(
      String sourceDatasetName,
      String destinationDatasetName,
      String... whitelistTags) throws IOException {
    DeidentifyDatasetRequest request = new DeidentifyDatasetRequest();
    request.setDestinationDataset(destinationDatasetName);
    DeidentifyConfig deidConfig = new DeidentifyConfig();
    DicomConfig dicomConfig = new DicomConfig();
    TagFilterList tagFilterList = new TagFilterList();
    List<String> whitelistTagList = Lists.newArrayList(defaultDicomKeepList);
    whitelistTagList.addAll(Lists.newArrayList(whitelistTags));
    tagFilterList.setTags(whitelistTagList);
    dicomConfig.setKeepList(tagFilterList);
    deidConfig.setDicom(dicomConfig);
    request.setConfig(deidConfig);
    HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .deidentify(sourceDatasetName, request)
        .execute();

    Dataset deidDataset = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .get(destinationDatasetName)
        .execute();

    System.out.println("Deidentified Dataset: " + GSON.toJson(deidDataset));
  }
}
// [END healthcare_deidentify_dataset]
