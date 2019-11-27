/*
 * Copyright 2016 Google Inc.
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

package com.example.helloworld;

import com.google.api.server.spi.config.Transformer;

// [START all]
public class BarTransformer implements Transformer<Bar, String> {
  public String transformTo(Bar in) {
    return in.getX() + "," + in.getY();
  }

  public Bar transformFrom(String in) {
    String[] xy = in.split(",");
    return new Bar(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
  }
}
// [END all]
