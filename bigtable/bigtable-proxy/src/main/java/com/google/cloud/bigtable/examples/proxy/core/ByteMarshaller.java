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

import com.google.common.io.ByteStreams;
import io.grpc.MethodDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class ByteMarshaller implements MethodDescriptor.Marshaller<byte[]> {

  @Override
  public byte[] parse(InputStream stream) {
    try {
      return ByteStreams.toByteArray(stream);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public InputStream stream(byte[] value) {
    return new ByteArrayInputStream(value);
  }
}
