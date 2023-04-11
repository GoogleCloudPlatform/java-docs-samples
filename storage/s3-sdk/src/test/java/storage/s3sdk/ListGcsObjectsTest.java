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
import java.util.Optional;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class ListGcsObjectsTest {
  private static final String BUCKET_ENV_VAR = "GOOGLE_CLOUD_PROJECT_S3_SDK_BUCKET_NAME";
  private static final String BUCKET = System.getenv(BUCKET_ENV_VAR);

  @ClassRule public static final TestHmacKeyRule hmacKey = new TestHmacKeyRule();

  /**
   * Hmac Keys can take a little bit of time to propagate. Run our test multiple times with some
   * backoff to try and allow for the propagation.
   */
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3, 2_000);

  @Rule public final StdOutCaptureRule stdOut = new StdOutCaptureRule();

  @Test
  public void testListObjects() {
    ListGcsObjects.listGcsObjects(
        hmacKey.getAccessKeyId(),
        hmacKey.getAccessSecretKey(),
        Optional.ofNullable(BUCKET).orElse(hmacKey.getProjectId()));
    String output = stdOut.getCapturedOutputAsUtf8String();
    MatcherAssert.assertThat(output, CoreMatchers.containsString("Objects:"));
  }
}
