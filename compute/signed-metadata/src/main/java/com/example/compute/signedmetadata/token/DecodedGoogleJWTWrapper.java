/*
 * Copyright 2017 Google Inc.
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

package com.example.compute.signedmetadata.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.base.Suppliers;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

//CHECKSTYLE OFF: AbbreviationAsWordInName
public class DecodedGoogleJWTWrapper {
  //CHECKSTYLE ON: AbbreviationAsWordInName

  private static final String KEY_PROJECT_ID = "project_id";
  private static final String KEY_PROJECT_NUMBER = "project_number";
  private static final String GOOGLE_METADATA_SPACE = "google";
  private static final String COMPUTE_ENGINE_METADATA_SUBSPACE = "compute_engine";

  private DecodedJWT jwt;
  private Supplier<Map<String, Object>> computeEngineMetadata = Suppliers.memoize(
      () -> {
      Map<String, Object> googleMetadata = jwt.getClaims().get(GOOGLE_METADATA_SPACE).asMap();
      Object computeEngineObject = googleMetadata.get(COMPUTE_ENGINE_METADATA_SUBSPACE);
      return castToMetadataMap(computeEngineObject);
      }
  );

  DecodedGoogleJWTWrapper(DecodedJWT jwt) {
    this.jwt = jwt;
  }

  public String getProjectId() {
    return getComputeEngineMetadata(KEY_PROJECT_ID);
  }

  public String getProjectNumber() {
    return getComputeEngineMetadata(KEY_PROJECT_NUMBER);
  }

  private String getComputeEngineMetadata(String key) {
    return computeEngineMetadata.get().get(key).toString();
  }

  // In Java we can only assure that an object is of class Map, we can check for key and value
  // types of an object added to Map, but only if Map is not empty.
  @SuppressWarnings({"rawtypes","unchecked"})
  private Map<String, Object> castToMetadataMap(Object object) {
    if (object instanceof Map) {
      Map map = (Map) object;
      if (map.isEmpty()) {
        // Map is empty, so we will create new map with desired types
        return new HashMap<>();
      }
      Set<Map.Entry> set = map.entrySet();
      Map.Entry someEntry = set.iterator().next();
      if (someEntry.getKey() instanceof String) {
        return (Map<String, Object>) object;
      }
    }
    throw new RuntimeException("We have not received a map of metadata");
  }
}
