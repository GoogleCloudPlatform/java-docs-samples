/*
 * Copyright 2019 Google LLC
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

package storage.s3sdk;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.cloud.testing.junit4.StdOutCaptureRule;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class ListGcsBucketsTest {

  @ClassRule public static final TestHmacKeyRule hmacKey = new TestHmacKeyRule();

  /**
   * Hmac Keys can take a little bit of time to propagate. Run our test multiple times with some
   * backoff to try and allow for the propagation.
   */
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3, 5_000);

  @Rule public final StdOutCaptureRule stdOut = new StdOutCaptureRule();

  @Test
  public void testListBucket() {
    ListGcsBuckets.listGcsBuckets(hmacKey.getAccessKeyId(), hmacKey.getAccessSecretKey());
    String output = stdOut.getCapturedOutputAsUtf8String();
    MatcherAssert.assertThat(output, CoreMatchers.containsString("Buckets:"));
  }
}
