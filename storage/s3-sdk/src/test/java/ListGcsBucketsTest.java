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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.amazonaws.services.s3.model.Bucket;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ListGcsBucketsTest {
  private static final String BUCKET = System.getenv("GOOGLE_CLOUD_PROJECT_S3_SDK");
  private static final String KEY_ID = System.getenv("STORAGE_HMAC_ACCESS_KEY_ID");
  private static final String SECRET_KEY = System.getenv("STORAGE_HMAC_ACCESS_SECRET_KEY");
  private ByteArrayOutputStream bout;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        System.getenv(varName),
        "Environment variable '%s' is required to perform these tests.".format(varName)
    );
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT_S3_SDK");
    requireEnvVar("STORAGE_HMAC_ACCESS_KEY_ID");
    requireEnvVar("STORAGE_HMAC_ACCESS_SECRET_KEY");
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testListBucket() throws Exception {
    ListGcsBuckets.listGcsBuckets(KEY_ID, SECRET_KEY);
    String output = bout.toString();
    assertThat(output, CoreMatchers.containsString("Buckets:"));
  }
}
