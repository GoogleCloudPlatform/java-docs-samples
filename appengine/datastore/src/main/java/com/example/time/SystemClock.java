/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.example.time;

import org.joda.time.Instant;

/**
 * Clock implementation that returns the "real" system time.
 *
 * <p>This class exists so that we can use a fake implementation for unit
 * testing classes that need the current time value. See {@link Clock} for
 * general information about clocks.
 */
public class SystemClock implements Clock {
  /**
   * Creates a new instance. All {@code SystemClock} instances function identically.
   */
  public SystemClock() {}

  @Override
  public Instant now() {
    return new Instant();
  }
}
