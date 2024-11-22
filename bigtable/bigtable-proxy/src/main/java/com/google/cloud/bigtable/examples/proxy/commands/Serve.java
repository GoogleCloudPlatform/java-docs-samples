/*
 * Copyright 2024 Google LLC
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

package com.google.cloud.bigtable.examples.proxy.commands;

import com.google.bigtable.admin.v2.BigtableInstanceAdminGrpc;
import com.google.bigtable.admin.v2.BigtableTableAdminGrpc;
import com.google.bigtable.v2.BigtableGrpc;
import com.google.cloud.bigtable.examples.proxy.core.ProxyHandler;
import com.google.cloud.bigtable.examples.proxy.core.Registry;
import com.google.common.collect.ImmutableMap;
import com.google.longrunning.OperationsGrpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerCallHandler;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

@Command(name = "serve", mixinStandardHelpOptions = true, description = "Start the proxy server")
public class Serve implements Callable<Void> {
  private static final Logger LOGGER = LoggerFactory.getLogger(Serve.class);

  @Option(
      names = "--listen-port",
      required = true,
      description = "Local port to accept connections on")
  int listenPort;

  @Option(names = "--useragent", showDefaultValue = Visibility.ALWAYS)
  String userAgent = "bigtable-java-proxy";

  @Option(
      names = "--bigtable-data-endpoint",
      converter = Endpoint.ArgConverter.class,
      showDefaultValue = Visibility.ALWAYS)
  Endpoint dataEndpoint = Endpoint.create("bigtable.googleapis.com", 443);

  @Option(
      names = "--bigtable-admin-endpoint",
      converter = Endpoint.ArgConverter.class,
      showDefaultValue = Visibility.ALWAYS)
  Endpoint adminEndpoint = Endpoint.create("bigtableadmin.googleapis.com", 443);

  ManagedChannel adminChannel = null;
  ManagedChannel dataChannel = null;
  Server server;

  @Override
  public Void call() throws Exception {
    start();
    server.awaitTermination();
    cleanup();
    return null;
  }

  void start() throws IOException {
    if (dataChannel == null) {
      dataChannel =
          ManagedChannelBuilder.forAddress(dataEndpoint.getName(), dataEndpoint.getPort())
              .userAgent(userAgent)
              .maxInboundMessageSize(256 * 1024 * 1024)
              .disableRetry()
              .keepAliveTime(30, TimeUnit.SECONDS)
              .keepAliveTimeout(10, TimeUnit.SECONDS)
              .build();
    }
    if (adminChannel == null) {
      adminChannel =
          ManagedChannelBuilder.forAddress(adminEndpoint.getName(), adminEndpoint.getPort())
              .userAgent(userAgent)
              .disableRetry()
              .build();
    }

    Map<String, ServerCallHandler<byte[], byte[]>> serviceMap =
        ImmutableMap.of(
            BigtableGrpc.SERVICE_NAME,
            new ProxyHandler<>(dataChannel),
            BigtableInstanceAdminGrpc.SERVICE_NAME,
            new ProxyHandler<>(adminChannel),
            BigtableTableAdminGrpc.SERVICE_NAME,
            new ProxyHandler<>(adminChannel),
            OperationsGrpc.SERVICE_NAME,
            new ProxyHandler<>(adminChannel));

    server =
        NettyServerBuilder.forAddress(
                new InetSocketAddress("localhost", listenPort), InsecureServerCredentials.create())
            .fallbackHandlerRegistry(new Registry(serviceMap))
            .maxInboundMessageSize(256 * 1024 * 1024)
            .build();

    server.start();
    LOGGER.info("Listening on port {}", server.getPort());
  }

  void cleanup() throws InterruptedException {
    dataChannel.shutdown();
    adminChannel.shutdown();
  }
}
