/*
 * Copyright 2015 Google Inc.
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

// [START all]

package com.google.cloud.storage.storagetransfer.samples;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.storagetransfer.v1.Storagetransfer;
import com.google.api.services.storagetransfer.v1.StoragetransferScopes;
import com.google.common.base.Preconditions;
import java.io.IOException;

/**
 * Create a client to make calls to Storage Transfer API.
 */
public final class TransferClientCreator {

  /**
   * Create a Storage Transfer client using application default credentials and other default
   * settings.
   *
   * @return a Storage Transfer client
   * @throws IOException
   *           there was an error obtaining application default credentials
   */
  public static Storagetransfer createStorageTransferClient() throws IOException {
    HttpTransport httpTransport = Utils.getDefaultTransport();
    JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault(httpTransport, jsonFactory);
    return createStorageTransferClient(httpTransport, jsonFactory, credential);
  }

  /**
   * Create a Storage Transfer client using user-supplied credentials and other settings.
   *
   * @param httpTransport
   *          a user-supplied HttpTransport
   * @param jsonFactory
   *          a user-supplied JsonFactory
   * @param credential
   *          a user-supplied Google credential
   * @return a Storage Transfer client
   */
  public static Storagetransfer createStorageTransferClient(
      HttpTransport httpTransport, JsonFactory jsonFactory, GoogleCredential credential) {
    Preconditions.checkNotNull(httpTransport);
    Preconditions.checkNotNull(jsonFactory);
    Preconditions.checkNotNull(credential);

    // In some cases, you need to add the scope explicitly.
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(StoragetransferScopes.all());
    }
    // Please use custom HttpRequestInitializer for automatic
    // retry upon failures. We provide a simple reference
    // implementation in the "Retry Handling" section.
    HttpRequestInitializer initializer = new RetryHttpInitializerWrapper(credential);
    return new Storagetransfer.Builder(httpTransport, jsonFactory, initializer)
        .setApplicationName("storagetransfer-sample")
        .build();
  }
}
//[END all]
