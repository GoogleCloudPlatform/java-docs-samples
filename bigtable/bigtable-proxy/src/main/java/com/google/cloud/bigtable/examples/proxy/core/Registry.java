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

package com.google.cloud.bigtable.examples.proxy.core;

import com.google.common.collect.ImmutableMap;
import io.grpc.HandlerRegistry;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import java.util.Map;

/**
 * Contains the service name -> handler mapping. This acts as an aggregate service.
 *
 * <p>The handlers treat requests and responses as raw byte arrays.
 */
public class Registry extends HandlerRegistry {
  private final MethodDescriptor.Marshaller<byte[]> byteMarshaller = new ByteMarshaller();
  private final Map<String, ServerCallHandler<byte[], byte[]>> serviceMap;

  public Registry(Map<String, ServerCallHandler<byte[], byte[]>> serviceMap) {
    this.serviceMap = ImmutableMap.copyOf(serviceMap);
  }

  @Override
  public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
    MethodDescriptor<byte[], byte[]> methodDescriptor =
        MethodDescriptor.newBuilder(byteMarshaller, byteMarshaller)
            .setFullMethodName(methodName)
            .setType(MethodDescriptor.MethodType.UNKNOWN)
            .build();

    ServerCallHandler<byte[], byte[]> handler = serviceMap.get(methodDescriptor.getServiceName());
    if (handler == null) {
      return null;
    }

    return ServerMethodDefinition.create(methodDescriptor, handler);
  }
}
