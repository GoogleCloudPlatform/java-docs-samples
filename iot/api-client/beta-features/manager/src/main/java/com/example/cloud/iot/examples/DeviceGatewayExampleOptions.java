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
public class DeviceGatewayExampleOptions {
  String algorithm;
  String cloudRegion = "us-central1";
  String command = "help";
  String deviceId; // Default to UUID?
  String gatewayId;
  String projectId;
  String publicKeyFile;
  String privateKeyFile;
  String registryName;
  String telemetryData = "Specify with -telemetry_data";

  String mqttBridgeHostname = "mqtt.googleapis.com";
  short mqttBridgePort = 8883;
  int numMessages = 10;
  int tokenExpMins = 60;
  String messageType = "event";


  static final Options options = new Options();

  /** Construct an DeviceGatewayExampleOptions class from command line flags. */
  public static DeviceGatewayExampleOptions fromFlags(String[] args) {
    // Required arguments
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("command")
            .hasArg()
            .desc(
                "Command to run:"
                + "\n\tbind-device-to-gateway"
                + "\n\tcreate-gateway"
                + "\n\tlist-gateways"
                + "\n\tlist-devices-for-gateway"
                + "\n\tlisten-for-config-messages"
                + "\n\tsend-data-from-bound-device"
                + "\n\tunbind-device-from-gateway")
            .required()
            .build());
    // Optional arguments.
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("algorithm")
            .hasArg()
            .desc("Algorithm used for public/private keys.")
            .build());
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
            .longOpt("device_id")
            .hasArg()
            .desc("The identifier for the device bound to the gateway.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("gateway_id")
            .hasArg()
            .desc("The identifier for the Gateway.")
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
            .longOpt("private_key_file")
            .hasArg()
            .desc("Private key file used for connecting devices and gateways.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("public_key_file")
            .hasArg()
            .desc("Public key file used for registering devices and gateways.")
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
            .longOpt("telemetry_data")
            .hasArg()
            .desc("The telemetry data (string or JSON) to send on behalf of the delegated device.")
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
            .type(String.class)
            .longOpt("mqtt_bridge_hostname")
            .hasArg()
            .desc("MQTT bridge hostname.")
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
            .type(Number.class)
            .longOpt("mqtt_bridge_port")
            .hasArg()
            .desc("MQTT bridge port.")
            .build());
    options.addOption(
        Option.builder()
            .type(String.class)
            .longOpt("message_type")
            .hasArg()
            .desc("Indicates whether the message is a telemetry event or a device state message")
            .build());


    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
      DeviceGatewayExampleOptions res = new DeviceGatewayExampleOptions();

      res.command = commandLine.getOptionValue("command");

      if (res.command.equals("help") || res.command.equals("")) {
        throw new ParseException("Invalid command, showing help.");
      }

      if (commandLine.hasOption("algorithm")) {
        res.algorithm = commandLine.getOptionValue("algorithm");
      }
      if (commandLine.hasOption("cloud_region")) {
        res.cloudRegion = commandLine.getOptionValue("cloud_region");
      }
      if (commandLine.hasOption("telemetry_data")) {
        res.telemetryData = commandLine.getOptionValue("telemetry_data");
      }
      if (commandLine.hasOption("device_id")) {
        res.deviceId = commandLine.getOptionValue("device_id");
      }
      if (commandLine.hasOption("gateway_id")) {
        res.gatewayId = commandLine.getOptionValue("gateway_id");
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
      if (commandLine.hasOption("private_key_file")) {
        res.privateKeyFile = commandLine.getOptionValue("private_key_file");
      }
      if (commandLine.hasOption("public_key_file")) {
        res.publicKeyFile = commandLine.getOptionValue("public_key_file");
      }
      if (commandLine.hasOption("registry_name")) {
        res.registryName = commandLine.getOptionValue("registry_name");
      }
      if (commandLine.hasOption("num_messages")) {
        res.numMessages = ((Number) commandLine.getParsedOptionValue("num_messages")).intValue();
      }
      if (commandLine.hasOption("token_exp_minutes")) {
        res.tokenExpMins =
            ((Number) commandLine.getParsedOptionValue("token_exp_minutes")).intValue();
      }
      if (commandLine.hasOption("mqtt_bridge_hostname")) {
        res.mqttBridgeHostname = commandLine.getOptionValue("mqtt_bridge_hostname");
      }
      if (commandLine.hasOption("mqtt_bridge_port")) {
        res.mqttBridgePort =
            ((Number) commandLine.getParsedOptionValue("mqtt_bridge_port")).shortValue();
      }
      if (commandLine.hasOption("message_type")) {
        res.messageType = commandLine.getOptionValue("message_type");
      }

      return res;
    } catch (ParseException e) {
      String header = "Cloud IoT Core Commandline Example (Device Gateways): \n\n";
      String footer = "\nhttps://cloud.google.com/iot-core";

      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("DeviceGatewayExample", header, options, footer,
          true);

      System.err.println(e.getMessage());
      return null;
    }
  }
}
