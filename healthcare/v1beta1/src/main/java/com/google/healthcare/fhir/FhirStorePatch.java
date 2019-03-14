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

package com.google.healthcare.fhir;

// [START healthcare_patch_fhir_store]

import com.google.HealthcareQuickstart;
import com.google.api.services.healthcare.v1beta1.model.FhirStore;
import com.google.api.services.healthcare.v1beta1.model.NotificationConfig;
import com.google.gson.Gson;

import java.io.IOException;

public class FhirStorePatch {
  private static final Gson GSON = new Gson();

  public static void patchFhirStore(String fhirStoreName, String pubsubTopic) throws IOException {
    FhirStore patched = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .get(fhirStoreName)
        .execute();
    NotificationConfig notificationConfig = new NotificationConfig();
    notificationConfig.setPubsubTopic(pubsubTopic);
    patched.setNotificationConfig(notificationConfig);
    FhirStore response = HealthcareQuickstart.getCloudHealthcareClient()
        .projects()
        .locations()
        .datasets()
        .fhirStores()
        .patch(fhirStoreName, patched)
        .setUpdateMask("notificationConfig")
        .execute();
    System.out.println("Patched FHIR store: " + GSON.toJson(response));
  }
}
// [END healthcare_patch_fhir_store]
