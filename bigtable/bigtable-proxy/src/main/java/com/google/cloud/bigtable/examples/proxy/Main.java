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

package com.google.cloud.bigtable.examples.proxy;

import com.google.cloud.bigtable.examples.proxy.commands.Serve;
import com.google.cloud.bigtable.examples.proxy.commands.Verify;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main entry point for proxy commands under {@link
 * com.google.cloud.bigtable.examples.proxy.commands}.
 */
@Command(
    subcommands = {Serve.class, Verify.class},
    name = "bigtable-proxy")
public final class Main {
  public static void main(String[] args) {
    SLF4JBridgeHandler.install();
    new CommandLine(new Main()).execute(args);
  }
}
