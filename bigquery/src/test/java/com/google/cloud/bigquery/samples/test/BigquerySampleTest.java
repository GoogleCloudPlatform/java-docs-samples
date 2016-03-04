/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples.test;

import com.google.cloud.bigquery.samples.BigqueryUtils;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Superclass for tests for samples.
 */
public class BigquerySampleTest extends BigqueryUtils {

  protected static class Constants {
    private String projectId;
    private String datasetId;
    private String currentTableId;
    private String newTableId;
    private String cloudStorageInputUri;
    private String cloudStorageOutputUri;
    private String query;

    public String getProjectId() {
      return projectId;
    }

    public String getDatasetId() {
      return datasetId;
    }

    public String getCurrentTableId() {
      return currentTableId;
    }

    public String getNewTableId() {
      return newTableId;
    }

    public String getQuery() {
      return query;
    }

    public String getCloudStorageOutputUri() {
      return cloudStorageOutputUri;
    }

    public String getCloudStorageInputUri() {
      return cloudStorageInputUri;
    }
  }

  @SuppressWarnings("checkstyle:abbreviationaswordinname")
  protected static Constants CONSTANTS = null;

  protected BigquerySampleTest()
      throws JsonSyntaxException, JsonIOException, FileNotFoundException {
    if (CONSTANTS == null) {
      InputStream is = this.getClass().getResourceAsStream("/constants.json");
      CONSTANTS = (new Gson()).<Constants>fromJson(new InputStreamReader(is), Constants.class);
    }
  }
}
