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

package com.example.cloud.iot.examples;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command line options for the HTTP example. */
public class HttpExampleOptions {
  String projectId;
  String registryId;
  String deviceId;
  String privateKeyFile;
  String algorithm;
  String cloudRegion = "us-central1";
  int numMessages = 100;
  int tokenExpMins = 20;
  String httpBridgeAddress = "https://cloudiotdevice.googleapis.com";
  String apiVersion = "v1";
  String messageType = "event";

  /** Construct an HttpExampleOptions class from command line flags. */
  public static HttpExampleOptions fromFlags(String[] args) {
    Options options = new Options();
    // Required arguments
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("project_id")
            .hasArg()
            .desc("GCP cloud project name.")
            .required()
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("registry_id")
            .hasArg()
            .desc("Cloud IoT Core registry id.")
            .required()
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("device_id")
            .hasArg()
            .desc("Cloud IoT Core device id.")
            .required()
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("private_key_file")
            .hasArg()
            .desc("Path to private key file.")
            .required()
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("algorithm")
            .hasArg()
            .desc("Encryption algorithm to use to generate the JWT. Either 'RS256' or 'ES256'.")
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
            .type(Number.class)
            .longOpt("num_messages")
            .hasArg()
            .desc("Number of messages to publish.")
            .build());
    options.addOption(
        Option.builder()
            .type(Number.class)
            .longOpt("token_exp_minutes")
            .hasArg()
            .desc("Minutes to JWT token refresh (token expiration time).")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("http_bridge_address")
            .hasArg()
            .desc("HTTP bridge address.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("api_version")
            .hasArg()
            .desc("The version to use of the API.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("message_type")
            .hasArg()
            .desc("Indicates whether message is a telemetry event or a device state message")
            .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
      HttpExampleOptions res = new HttpExampleOptions();

      res.projectId = commandLine.getOptionValue("project_id");
      res.registryId = commandLine.getOptionValue("registry_id");
      res.deviceId = commandLine.getOptionValue("device_id");
      res.privateKeyFile = commandLine.getOptionValue("private_key_file");
      res.algorithm = commandLine.getOptionValue("algorithm");
      if (commandLine.hasOption("cloud_region")) {
        res.cloudRegion = commandLine.getOptionValue("cloud_region");
      }
      if (commandLine.hasOption("num_messages")) {
        res.numMessages = ((Number) commandLine.getParsedOptionValue("num_messages")).intValue();
      }
      if (commandLine.hasOption("token_exp_minutes")) {
        res.tokenExpMins =
            ((Number) commandLine.getParsedOptionValue("token_exp_minutes")).intValue();
      }
      if (commandLine.hasOption("http_bridge_address")) {
        res.httpBridgeAddress = commandLine.getOptionValue("http_bridge_address");
      }
      if (commandLine.hasOption("api_version")) {
        res.apiVersion = commandLine.getOptionValue("api_version");
      }
      if (commandLine.hasOption("message_type")) {
        res.messageType = commandLine.getOptionValue("message_type");
      }
      return res;
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      return null;
    }
  }
}
