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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.cloud.bigtable.examples.proxy.commands.Endpoint.ArgConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EndpointTest {
  @Test
  public void testOk() throws Exception {
    ArgConverter argConverter = new ArgConverter();
    Endpoint result = argConverter.convert("some-endpoint:1234");
    assertThat(result).isEqualTo(Endpoint.create("some-endpoint", 1234));
  }

  @Test
  public void testMissingPort() throws Exception {
    ArgConverter argConverter = new ArgConverter();
    assertThrows(IllegalArgumentException.class, () -> argConverter.convert("some-endpoint:"));
    assertThrows(IllegalArgumentException.class, () -> argConverter.convert("some-endpoint"));
  }

  @Test
  public void testMissingName() throws Exception {
    ArgConverter argConverter = new ArgConverter();
    assertThrows(IllegalArgumentException.class, () -> argConverter.convert(":1234"));
  }

  @Test
  public void testIpv6() throws Exception {
    ArgConverter argConverter = new ArgConverter();
    Endpoint result = argConverter.convert("[2561:1900:4545:0003:0200:F8FF:FE21:67CF]:1234");
    assertThat(result)
        .isEqualTo(Endpoint.create("[2561:1900:4545:0003:0200:F8FF:FE21:67CF]", 1234));
  }
}
