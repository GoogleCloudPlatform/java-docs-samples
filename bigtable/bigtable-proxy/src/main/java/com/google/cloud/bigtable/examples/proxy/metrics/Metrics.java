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

package com.google.cloud.bigtable.examples.proxy.metrics;

import io.grpc.Status;
import java.time.Duration;

public interface Metrics {

  void recordCallStarted(CallLabels labels);

  void recordCredLatency(CallLabels labels, Status status, Duration duration);

  void recordQueueLatency(CallLabels labels, Duration duration);

  void recordRequestSize(CallLabels labels, long size);

  void recordResponseSize(CallLabels labels, long size);

  void recordGfeLatency(CallLabels labels, Duration duration);

  void recordGfeHeaderMissing(CallLabels labels);

  void recordCallLatency(CallLabels labels, Status status, Duration duration);

  void updateChannelCount(int delta);
}
