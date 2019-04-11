// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example;

import org.apache.commons.csv.CSVFormat;

import java.util.Arrays;

/**
 * Contains utility functions for getting a {@link CSVFormat} object.
 */
public abstract class CSVFormatUtils {

  private CSVFormatUtils() {
  }

  public static CSVFormat getCsvFormat(String csvFormatString) {
    // Check CSV Format:
    CSVFormat csvFormat;
    try {
      csvFormat = CSVFormat.Predefined.valueOf(csvFormatString).getFormat();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("--csvFormat value: '" + csvFormatString +
              "' is not recognised. Should be one of:" + Arrays.asList(CSVFormat.Predefined.values()), e);
    }
    return csvFormat;
  }
}
