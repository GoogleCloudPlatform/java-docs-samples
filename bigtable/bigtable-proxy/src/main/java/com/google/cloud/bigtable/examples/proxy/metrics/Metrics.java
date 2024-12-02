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

import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.cloud.bigtable.examples.proxy.metrics.Metrics.MetricsAttributes;
import io.grpc.Status;
import java.time.Duration;

public interface Metrics {
  MetricsAttributes createAttributes(CallLabels callLabels);

  void recordCallStarted(MetricsAttributes attrs);

  void recordCredLatency(MetricsAttributes attrs, Status status, Duration duration);

  void recordQueueLatency(MetricsAttributes attrs, Duration duration);

  void recordRequestSize(MetricsAttributes attrs, long size);

  void recordResponseSize(MetricsAttributes attrs, long size);

  void recordGfeLatency(MetricsAttributes attrs, Duration duration);

  void recordGfeHeaderMissing(MetricsAttributes attrs);

  void recordCallLatency(MetricsAttributes attrs, Status status, Duration duration);

  void updateChannelCount(int delta);

  interface MetricsAttributes {}
}
