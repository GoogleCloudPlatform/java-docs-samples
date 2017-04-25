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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SamplesBuilder {

  private final MergeYaml mergeYaml;
  private final String sourcePath;
  private final String destinationPath;
  private final String basePath;
  private final String[] packageNames;
  private final String appYamlFile = "app.yaml";
  private final String cronYamlFile = "cron.yaml";

  private SamplesBuilder(String basePath, String[] packageNames)
      throws Exception {
    this.mergeYaml = new MergeYaml();
    this.basePath = basePath;
    this.sourcePath = basePath + "/samples-source";
    this.destinationPath = basePath + "/samples-runner";
    this.packageNames = packageNames;
  }

  private void mergeAndWriteAppYaml() throws Exception {
    List<File> appEngineFiles = new ArrayList<>();
    List<File> cronFiles = new ArrayList<>();
    for (String packageName : packageNames) {
      appEngineFiles.add(
          new File(sourcePath + "/" + packageName + "/src/main/appengine/" + appYamlFile));
      cronFiles.add(
          new File(sourcePath + "/" + packageName + "/src/main/appengine/" + cronYamlFile));

    }
    Map<String, Object> mergedResult = new LinkedHashMap<>();
    mergeYaml.merge(mergedResult, appEngineFiles);
    if (mergedResult.size() > 0) {
      write(mergedResult, appYamlFile);
    }

    mergedResult.clear();
    mergeYaml.merge(mergedResult, cronFiles);
    if (mergedResult.size() > 0) {
      write(mergedResult, cronYamlFile);
    }
  }

  private List<String> read(String fileName) throws IOException {
    return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
  }

  private List<String> extractBlock(String packageName, List<String> lines, String blockName)
      throws IllegalArgumentException {
    boolean inBlock = false;
    List<String> block = new LinkedList<>();
    for (String line : lines) {
      if (inBlock) {
        if (line.contains("END " + blockName)) {
          inBlock = false;
          break;
        }
        block.add(line);
      }
      if (line.contains("START " + blockName)) {
        inBlock = true;
      }
    }

    if (inBlock) {
      throw new IllegalArgumentException(blockName + " not closed in " + packageName + " pom.xml");
    }
    if (block.size() > 0) {
      String indent = "    ";
      block.add(0, indent + "<!-- [START " + packageName + " ] -->");
      block.add(indent + "<!-- [END " + packageName + " ] -->");
    }
    return block;
  }

  private void parsePom(String packageName, List<String> properties, List<String> dependencies)
      throws IllegalArgumentException, IOException {
    List<String> pom = read(sourcePath + "/" + packageName + "/pom.xml");
    properties.addAll(extractBlock(packageName, pom, "properties"));
    dependencies.addAll(extractBlock(packageName, pom, "dependencies"));
  }

  private void copyFile(String fileName) throws IOException {
    FileUtils.copyFile(new File(sourcePath + "/" + fileName),
        new File(basePath + "/" + fileName));
  }

  private void copyPackageFiles(String path) {
    for (String packageName : packageNames) {
      try {
        FileUtils.copyDirectory(
            new File(sourcePath + "/" + packageName + "/" + path + "/" + packageName),
            new File(basePath + "/" + path + "/" + packageName));
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
    List<String> sourceDirs = new ArrayList<>();
    List<String> testSourceDirs = new ArrayList<>();
    for (String packageName : packageNames) {
      parsePom(packageName, properties, dependencies);
      sourceDirs.add("<source> " + sourcePath + "/" + packageName + "/src/main/java" + "</source>");
      testSourceDirs.add(
          "<source>" + sourcePath + "/" + packageName + "/src/main/test" + "</source>");
    }
    addToPom(pom, "properties", properties);
    addToPom(pom, "dependencies", dependencies);
    addToPom(pom, "source-dirs", sourceDirs);
    addToPom(pom, "test-source-dirs", testSourceDirs);
    write(pom, destinationPath + "/pom.xml");
  }

  private void write(List<String> lines, String fileName) throws Exception {
    File outputFile = new File(fileName);
    outputFile.getParentFile().mkdirs();
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
    File outputFile = new File(
        basePath + "/src/main/appengine/" + outputFileName);
    outputFile.getParentFile().mkdirs();
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

  private static String[] getAllDirectories(String path) {
    File[] files = new File(path).listFiles();
    List<String> fileNamesList = Arrays.stream(files)
        .filter(File::isDirectory)
        .map(File::getName)
        .collect(Collectors.toList());
    String[] fileNames = new String[fileNamesList.size()];
    fileNamesList.toArray(fileNames);
    return fileNames;
  }

  public static void main(String[] args) throws Exception {
    System.out.println(args[0]);
    String baseDir = args[0];
    String[] packageNames;
    if (args[1] == null || args[1].equals("all")) {
      packageNames = getAllDirectories(args[0] + "/samples-source");
    } else {
      packageNames = args[1].split(",");
    }
    SamplesBuilder samplesBuilder = new SamplesBuilder(baseDir, packageNames);
    System.out.println("Merging app.yaml files");
    samplesBuilder.mergeAndWriteAppYaml();

    System.out.println("Generating pom.xml");
    samplesBuilder.generatePom();

    System.out.println("Copying source and webapp files");
    samplesBuilder.copyPackageFiles("src/main/webapp");
    samplesBuilder.copyPackageFiles("src/main/resources");
    samplesBuilder.copyFile("index.jsp");
  }
}

