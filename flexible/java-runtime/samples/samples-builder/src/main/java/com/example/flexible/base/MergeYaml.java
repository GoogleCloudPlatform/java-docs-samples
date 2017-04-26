/**
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

package com.example.flexible.base;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class MergeYaml {

  Yaml yaml;

  MergeYaml() {
    this.yaml = new Yaml();
  }

  @SuppressWarnings("unchecked")
  public void merge(Map<String, Object> mergedResult, List<File> files) throws IOException {
    for (File file : files) {
      if (file.isDirectory() && file.listFiles() != null) {
        merge(mergedResult, Arrays.asList(file.listFiles()));
        return;
      }
      InputStream in = null;
      try {
        in = new FileInputStream(file);

        final Map<String, Object> yamlContents = (Map<String, Object>) yaml.load(in);

        merge(mergedResult, yamlContents);

      } catch (IOException e) {
        // ignore
      } finally {
        if (in != null) {
          in.close();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void merge(Map<String, Object> mergedResult, Map<String, Object> yamlContents) {
    if (yamlContents == null) {
      return;
    }

    for (String key : yamlContents.keySet()) {
      Object yamlValue = yamlContents.get(key);
      if (yamlValue == null) {
        addToMergedResult(mergedResult, key, yamlValue);
        continue;
      }

      Object existingValue = mergedResult.get(key);
      if (existingValue != null) {
        if (yamlValue instanceof Map) {
          if (existingValue instanceof Map) {
            merge((Map<String, Object>) existingValue, (Map<String, Object>) yamlValue);
          } else if (existingValue instanceof String) {
            throw new IllegalArgumentException(
                "Cannot flexible.base complex element into a simple element: " + key);
          } else {
            throw unknownValueType(key, yamlValue);
          }
        } else if (yamlValue instanceof List) {
          mergeLists(mergedResult, key, yamlValue);

        } else if (yamlValue instanceof String
            || yamlValue instanceof Boolean
            || yamlValue instanceof Double
            || yamlValue instanceof Integer) {
          addToMergedResult(mergedResult, key, yamlValue);

        } else {
          throw unknownValueType(key, yamlValue);
        }

      } else {
        if (yamlValue instanceof Map
            || yamlValue instanceof List
            || yamlValue instanceof String
            || yamlValue instanceof Boolean
            || yamlValue instanceof Integer
            || yamlValue instanceof Double) {
          addToMergedResult(mergedResult, key, yamlValue);
        } else {
          throw unknownValueType(key, yamlValue);
        }
      }
    }
  }

  private IllegalArgumentException unknownValueType(String key, Object yamlValue) {
    final String msg =
        "Cannot flexible.base element of unknown type: " + key + ": " + yamlValue.getClass()
            .getName();
    return new IllegalArgumentException(msg);
  }

  private Object addToMergedResult(Map<String, Object> mergedResult, String key, Object yamlValue) {
    return mergedResult.put(key, yamlValue);
  }

  @SuppressWarnings("unchecked")
  private void mergeLists(Map<String, Object> mergedResult, String key, Object yamlValue) {
    if (!(yamlValue instanceof List && mergedResult.get(key) instanceof List)) {
      throw new IllegalArgumentException("Cannot base a list with a non-list: " + key);
    }
    List<Object> originalList = (List<Object>) mergedResult.get(key);
    originalList.addAll((List<Object>) yamlValue);
    mergedResult.put(key, originalList.stream().distinct().collect(Collectors.toList()));
  }

  public String getYaml(Map<String, Object> mergedResult) {
    return yaml.dumpAs(mergedResult, Tag.MAP, DumperOptions.FlowStyle.BLOCK);
  }
}

