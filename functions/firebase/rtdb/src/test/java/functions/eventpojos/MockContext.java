/*
 * Copyright 2020 Google LLC
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

package functions.eventpojos;

import com.google.cloud.functions.Context;

// Class that mocks Cloud Functions "context" objects
// Used to create fake context objects for function tests
public class MockContext implements Context {
  public String eventId;
  public String eventType;
  public String timestamp;
  public String resource;

  @Override
  public String eventId() {
    return this.eventId;
  }

  @Override
  public String timestamp() {
    return this.timestamp;
  }

  @Override
  public String eventType() {
    return this.eventType;
  }

  @Override
  public String resource() {
    return this.resource;
  }
}
