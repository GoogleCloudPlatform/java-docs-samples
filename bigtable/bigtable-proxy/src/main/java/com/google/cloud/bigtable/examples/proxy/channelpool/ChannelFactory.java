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

// Copied from
// https://github.com/googleapis/sdk-platform-java/blob/a333b0709023c971f12a85e5287b6d77d1b57c48/gax-java/gax-grpc/src/main/java/com/google/api/gax/grpc/ChannelFactory.java
// Changes:
// - package name
// - removed InternalApi annotation

package com.google.cloud.bigtable.examples.proxy.channelpool;

import io.grpc.ManagedChannel;
import java.io.IOException;

/**
 * This interface represents a factory for creating one ManagedChannel
 *
 * <p>This is public only for technical reasons, for advanced usage.
 */
public interface ChannelFactory {
  ManagedChannel createSingleChannel() throws IOException;
}
