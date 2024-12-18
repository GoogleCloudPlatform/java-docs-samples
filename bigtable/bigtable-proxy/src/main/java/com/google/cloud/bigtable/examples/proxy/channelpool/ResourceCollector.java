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

import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.ParsingException;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.PrimingKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceCollector {
  private static final Logger LOG = LoggerFactory.getLogger(ResourceCollector.class);

  private final Cache<PrimingKey, Boolean> primingKeys =
      CacheBuilder.newBuilder().expireAfterWrite(Duration.ofHours(1)).maximumSize(100).build();

  public void collect(CallLabels labels) {
    try {
      PrimingKey.from(labels).ifPresent(k -> primingKeys.put(k, true));
    } catch (ParsingException e) {
      LOG.warn("Failed to collect priming request for {}", labels, e);
    }
  }

  public List<PrimingKey> getPrimingKeys() {
    return ImmutableList.copyOf(primingKeys.asMap().keySet());
  }

  public void evict(PrimingKey request) {
    primingKeys.invalidate(request);
  }
}
