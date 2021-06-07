/*
 * Copyright 2021 Google LLC
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

import static org.junit.Assert.assertNotNull;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.HmacKey;
import com.google.cloud.storage.HmacKey.HmacKeyMetadata;
import com.google.cloud.storage.HmacKey.HmacKeyState;
import com.google.cloud.storage.ServiceAccount;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link org.junit.ClassRule} which will create and cleanup an HMAC Key for use during the scope
 * of a test.
 */
public final class TestHmacKeyRule implements TestRule {

  private String accessKeyId;
  private String accessSecretKey;

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        assertNotNull(
            "GOOGLE_APPLICATION_CREDENTIALS is not set",
            System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        StorageOptions options = StorageOptions.getDefaultInstance();
        Storage storage = options.getService();
        Credentials credentials = options.getCredentials();
        if (credentials instanceof ServiceAccountCredentials) {
          ServiceAccountCredentials serviceAccountCredentials =
              (ServiceAccountCredentials) credentials;

          ServiceAccount serviceAccount =
              ServiceAccount.of(serviceAccountCredentials.getClientEmail());
          HmacKey hmacKey = storage.createHmacKey(serviceAccount);
          HmacKeyMetadata metadata = hmacKey.getMetadata();
          accessKeyId = metadata.getAccessId();
          accessSecretKey = hmacKey.getSecretKey();
          try {
            base.evaluate();
          } finally {
            storage.updateHmacKeyState(metadata, HmacKeyState.INACTIVE);
            storage.deleteHmacKey(metadata);
          }
        }
      }
    };
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public String getAccessSecretKey() {
    return accessSecretKey;
  }
}
