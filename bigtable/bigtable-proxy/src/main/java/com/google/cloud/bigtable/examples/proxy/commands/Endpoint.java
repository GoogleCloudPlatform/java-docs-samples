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

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import picocli.CommandLine.ITypeConverter;

@AutoValue
abstract class Endpoint {
  abstract String getName();

  abstract int getPort();

  @Override
  public String toString() {
    return String.format("%s:%d", getName(), getPort());
  }

  static Endpoint create(String name, int port) {
    return new AutoValue_Endpoint(name, port);
  }

  static class ArgConverter implements ITypeConverter<Endpoint> {
    @Override
    public Endpoint convert(String s) throws Exception {
      int i = s.lastIndexOf(":");
      Preconditions.checkArgument(i > 0, "endpoint must of the form `name:port`");

      String name = s.substring(0, i);
      int port = Integer.parseInt(s.substring(i + 1));
      return Endpoint.create(name, port);
    }
  }
}
