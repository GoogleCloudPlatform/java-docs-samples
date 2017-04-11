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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SamplesBuilder {

  private final MergeYaml mergeYaml;
  private final String sourcePath;
  private final String destinationPath;
  private final String[] packageNames;
  private final String appYamlFile = "app.yaml";
  private final String cronYamlFile = "cron.yaml";

  // Pom snippets used to generate pom.xml
  private final String dependenciesFile = "dependencies.xml";
  private final String propertiesFile = "properties.xml";

  private SamplesBuilder(String sourcePath, String destinationPath, String[] packageNames)
      throws Exception {
    this.mergeYaml = new MergeYaml();
    this.sourcePath = sourcePath;
    this.destinationPath = destinationPath;
    this.packageNames = packageNames;
  }

  private void mergeAndWriteAppYaml() throws Exception {
    List<File> appEngineFiles = new ArrayList<>();
    List<File> cronFiles = new ArrayList<>();
    for (String packageName : packageNames) {
      appEngineFiles.add(
          new File(sourcePath + "/src/main/appengine/" + packageName + "/" + appYamlFile));
      cronFiles.add(
          new File(sourcePath + "/src/main/appengine/" + packageName + "/" + cronYamlFile));

    }
    Map<String, Object> mergedResult = new LinkedHashMap<>();
    mergeYaml.merge(mergedResult, appEngineFiles);
    write(mergedResult, appYamlFile);

    mergedResult.clear();
    mergeYaml.merge(mergedResult, cronFiles);
    write(mergedResult, cronYamlFile);
  }

  private List<String> read(String fileName) throws IOException {
    return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
  }

  private List<String> getPomSnippet(String packageName, String fileName) {
    List<String> pomEntries;
    try {
      List<String> snippet = read(sourcePath + "/pom-snippets/" + packageName + "/" + fileName);
      pomEntries = new ArrayList<>();
      pomEntries.add("        <!-- [START " + packageName + " ] -->");
      List<String> formattedSnippet = snippet.stream().map(line -> "        " + line)
          .collect(Collectors.toList());
      pomEntries.addAll(formattedSnippet);
      pomEntries.add("        <!-- [END " + packageName + " ] -->");

    } catch (IOException e) {
      System.out.println("No " + fileName + " found for package : " + packageName);
      pomEntries = Collections.emptyList();
    }
    return pomEntries;
  }


  private void copyFiles(String path) {
    for (String packageName : packageNames) {
      try {
        FileUtils.copyDirectory(new File(sourcePath + path + packageName),
            new File(destinationPath + path + packageName));
      } catch (IOException e) {
        // ignore if no directory exists
      }
    }
  }

  private void addToPom(List<String> pom, String blockTag, List<String> lines) throws Exception {
    int i;
    String blockName = "[START " + blockTag + "]";
    for (i = 0; i < pom.size(); i++) {
      if (pom.get(i).contains(blockName)) {
        break;
      }
    }
    if (i == pom.size()) {
      throw new Exception(blockName + " block not found");
    }

    pom.addAll(i + 1, lines);
  }

  private void generatePom() throws Exception {
    List<String> pom = read(sourcePath + "/pom-base.xml");
    List<String> dependencies = new ArrayList<>();
    List<String> properties = new ArrayList<>();
    for (String packageName : packageNames) {
      dependencies.addAll(getPomSnippet(packageName, dependenciesFile));
      properties.addAll(getPomSnippet(packageName, propertiesFile));
    }
    addToPom(pom, "properties", properties);
    addToPom(pom, "dependencies", dependencies);
    write(pom, destinationPath + "/pom.xml");
  }

  private void write(List<String> lines, String fileName) throws Exception {
    PrintWriter pw = new PrintWriter(new PrintWriter(fileName));
    for (String line : lines) {
      pw.write(line);
      pw.write("\n");
    }
    pw.close();
  }

  private void write(Map<String, Object> mergedResult, String outputFileName) {
    String yamlOutput = mergeYaml.getYaml(mergedResult);
    PrintWriter writer = null;
    File outputFile = new File(destinationPath + "/src/main/appengine/" + outputFileName);
    try {
      writer = new PrintWriter(outputFile, "UTF-8");
      writer.write(yamlOutput);
    } catch (Exception e) {
      System.err.println("Error writing to output file : " + e.getMessage());
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    SamplesBuilder samplesBuilder = new SamplesBuilder(args[0],
        args[1], args[2].split(","));
    System.out.println("Merging app.yaml files");
    samplesBuilder.mergeAndWriteAppYaml();

    System.out.println("Generating pom.xml");
    samplesBuilder.generatePom();

    System.out.println("Copying source and webapp files");
    samplesBuilder.copyFiles("/src/main/webapp/");
    samplesBuilder.copyFiles("/src/main/java/com/example/flexible/");
    samplesBuilder.copyFiles("/src/main/resources/");
  }
}

