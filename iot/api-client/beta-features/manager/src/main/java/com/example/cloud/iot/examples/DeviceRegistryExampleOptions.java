/*
 * Copyright 2018 Google Inc.
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

package com.example.cloud.iot.examples;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command line options for the Device Manager example. */
public class DeviceRegistryExampleOptions {
  String cloudRegion = "us-central1";
  String command = "help";
  String commandData = "Specify with --data";
  String deviceId; // Default to UUID?
  String projectId;
  String registryName;

  static final Options options = new Options();

  /** Construct an DeviceRegistryExampleOptions class from command line flags. */
  public static DeviceRegistryExampleOptions fromFlags(String[] args) {
    // Required arguments
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("command")
            .hasArg()
            .desc(
                "Command to run:"
                + "\n\tsend-command")
            .required()
            .build());

    // Optional arguments.
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("cloud_region")
            .hasArg()
            .desc("GCP cloud region.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("project_id")
            .hasArg()
            .desc("GCP cloud project name.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("registry_name")
            .hasArg()
            .desc("Name for your Device Registry.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("device_id")
            .hasArg()
            .desc("Name for your Device.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("data")
            .hasArg()
            .desc("The command data (string or JSON) to send to the specified device.")
            .build());


    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
      DeviceRegistryExampleOptions res = new DeviceRegistryExampleOptions();

      res.command = commandLine.getOptionValue("command");

      if (res.command.equals("help") || res.command.equals("")) {
        throw new ParseException("Invalid command, showing help.");
      }

      if (commandLine.hasOption("cloud_region")) {
        res.cloudRegion = commandLine.getOptionValue("cloud_region");
      }
      if (commandLine.hasOption("data")) {
        res.commandData = commandLine.getOptionValue("data");
      }
      if (commandLine.hasOption("device_id")) {
        res.deviceId = commandLine.getOptionValue("device_id");
      }
      if (commandLine.hasOption("project_id")) {
        res.projectId = commandLine.getOptionValue("project_id");
      } else {
        try {
          res.projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        } catch (NullPointerException npe) {
          res.projectId = System.getenv("GCLOUD_PROJECT");
        }
      }
      if (commandLine.hasOption("registry_name")) {
        res.registryName = commandLine.getOptionValue("registry_name");
      }


      return res;
    } catch (ParseException e) {
      String header = "Cloud IoT Core Commandline Example (Device / Registry management): \n\n";
      String footer = "\nhttps://cloud.google.com/iot-core";

      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("DeviceRegistryExample", header, options, footer,
          true);

      System.err.println(e.getMessage());
      return null;
    }
  }
}
