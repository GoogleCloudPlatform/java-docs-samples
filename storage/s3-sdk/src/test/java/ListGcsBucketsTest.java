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

import static com.google.common.truth.Truth.assertThat;

import com.amazonaws.services.s3.model.Bucket;
import java.util.List;
import org.junit.Test;

public class ListGcsBucketsTest {
  private static final String BUCKET = System.getenv("GOOGLE_CLOUD_PROJECT_S3_SDK");
  private static final String KEY_ID = System.getenv("STORAGE_HMAC_ACCESS_KEY_ID");
  private static final String SECRET_KEY = System.getenv("STORAGE_HMAC_ACCESS_SECRET_KEY");

  @Test
  public void testListBucket() throws Exception {
    List<Bucket> buckets = ListGcsBuckets.listGcsBuckets(KEY_ID, SECRET_KEY);
    assertThat(buckets.toString()).contains(BUCKET);
  }
}
