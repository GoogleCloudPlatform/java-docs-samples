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

package com.google.cloud.bigtable.examples.proxy.channelpool;

import com.google.bigtable.v2.PingAndWarmRequest;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;

public class ResourceCollector {
  private final Cache<PingAndWarmRequest, Boolean> warmingRequests =
      CacheBuilder.newBuilder().expireAfterWrite(Duration.ofHours(1)).maximumSize(100).build();

  public void collect(CallLabels labels) {
    String[] splits = labels.getResourceName().orElse("").split("/", 5);
    if (splits.length <= 4) {
      return;
    }
    if (!"projects".equals(splits[0])) {
      return;
    }
    if (!"instances".equals(splits[2])) {
      return;
    }
    String appProfile = labels.getAppProfileId().orElse("");

    PingAndWarmRequest req =
        PingAndWarmRequest.newBuilder()
            .setName("projects/" + splits[1] + "/instances/" + splits[3])
            .setAppProfileId(appProfile)
            .build();
    warmingRequests.put(req, true);
  }

  public List<PingAndWarmRequest> getRequests() {
    return ImmutableList.copyOf(warmingRequests.asMap().keySet());
  }

  public void evict(PingAndWarmRequest request) {
    warmingRequests.invalidate(request);
  }
}
