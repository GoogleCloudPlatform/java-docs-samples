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

public class NoopMetrics implements Metrics {

  @Override
  public void recordCallStarted(CallLabels labels) {}

  @Override
  public void recordCredLatency(CallLabels labels, Status status, Duration duration) {}

  @Override
  public void recordQueueLatency(CallLabels labels, Duration duration) {}

  @Override
  public void recordRequestSize(CallLabels labels, long size) {}

  @Override
  public void recordResponseSize(CallLabels labels, long size) {}

  @Override
  public void recordGfeLatency(CallLabels labels, Duration duration) {}

  @Override
  public void recordGfeHeaderMissing(CallLabels labels) {}

  @Override
  public void recordCallLatency(CallLabels labels, Status status, Duration duration) {}
}
