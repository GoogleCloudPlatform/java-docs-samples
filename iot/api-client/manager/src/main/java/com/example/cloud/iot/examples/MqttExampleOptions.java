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
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/** Command line options for the MQTT example. */
public class MqttExampleOptions {
  String projectId;
  String registryId;
  String command = "mqtt-demo";
  String deviceId;
  String gatewayId;
  String privateKeyFile;
  String algorithm;
  String cloudRegion = "us-central1";
  int numMessages = 100;
  int tokenExpMins = 20;
  String telemetryData = "Specify with -telemetry_data";

  String mqttBridgeHostname = "mqtt.googleapis.com";
  short mqttBridgePort = 8883;
  String messageType = "event";
  int waitTime = 120;

  /** Construct an MqttExampleOptions class from command line flags. */
  public static MqttExampleOptions fromFlags(String[] args) {
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
            .longOpt("gateway_id")
            .hasArg()
            .desc("The identifier for the Gateway.")
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
            .longOpt("command")
            .hasArg()
            .desc(
                "Command to run:"
                    + "\n\tlisten-for-config-messages"
                    + "\n\tsend-data-from-bound-device")
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
    options.addOption(
        Option.builder()
            .type(Number.class)
            .longOpt("wait_time")
            .hasArg()
            .desc("Wait time (in seconds) for commands.")
            .build());

    CommandLineParser parser = new DefaultParser();
    CommandLine commandLine;
    try {
      commandLine = parser.parse(options, args);
      MqttExampleOptions res = new MqttExampleOptions();

      res.projectId = commandLine.getOptionValue("project_id");
      res.registryId = commandLine.getOptionValue("registry_id");
      res.deviceId = commandLine.getOptionValue("device_id");
      res.privateKeyFile = commandLine.getOptionValue("private_key_file");
      res.algorithm = commandLine.getOptionValue("algorithm");
      if (commandLine.hasOption("command")) {
        res.command = commandLine.getOptionValue("command");
      }
      if (commandLine.hasOption("gateway_id")) {
        res.gatewayId = commandLine.getOptionValue("gateway_id");
      }
      if (commandLine.hasOption("wait_time")) {
        res.waitTime = ((Number) commandLine.getParsedOptionValue("wait_time")).intValue();
      }
      if (commandLine.hasOption("cloud_region")) {
        res.cloudRegion = commandLine.getOptionValue("cloud_region");
      }
      if (commandLine.hasOption("telemetry_data")) {
        res.telemetryData = commandLine.getOptionValue("telemetry_data");
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
      System.err.println(e.getMessage());
      return null;
    }
  }
}
