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

import com.google.api.server.spi.config.ApiTransformer;
//CHECKSTYLE.OFF: MemberName - readability
// [START all]

@ApiTransformer(BarTransformer.class)
public class Bar {
  private final int x;
  private final int y;

  public Bar(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}

// [END all]
//CHECKSTYLE.ON: MemberName