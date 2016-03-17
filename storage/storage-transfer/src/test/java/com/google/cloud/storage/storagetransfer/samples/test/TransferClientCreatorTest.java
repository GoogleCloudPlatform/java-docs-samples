/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.cloud.storage.storagetransfer.samples.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.storagetransfer.Storagetransfer.Builder;
import com.google.cloud.storage.storagetransfer.samples.RetryHttpInitializerWrapper;
import com.google.cloud.storage.storagetransfer.samples.TransferClientCreator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TransferClientCreator.class, Builder.class })
public class TransferClientCreatorTest {

  private Builder mockBuilder = PowerMockito.mock(Builder.class);
  private GoogleCredential mockCredential = Mockito.mock(GoogleCredential.class);
  private RetryHttpInitializerWrapper mockInitializer =
      Mockito.mock(RetryHttpInitializerWrapper.class);
  private HttpTransport httpTransport = Utils.getDefaultTransport();
  private JsonFactory jsonFactory = Utils.getDefaultJsonFactory();

  @Before
  public void setUp() throws Exception {
    PowerMockito.whenNew(RetryHttpInitializerWrapper.class).withArguments(mockCredential)
        .thenReturn(mockInitializer);
    PowerMockito.mockStatic(Builder.class);
    PowerMockito.whenNew(Builder.class).withArguments(httpTransport, jsonFactory, mockInitializer)
        .thenReturn(mockBuilder);
    when(mockBuilder.setApplicationName(Matchers.anyString())).thenReturn(mockBuilder);
    PowerMockito.mockStatic(GoogleCredential.class);
  }

  /**
   * Tests whether createStorageTransferClient() makes the API call for building clients when the
   * credential does not require a Scope.
   */
  @Test
  public void testCreateStorageTransferClientScopedRequiredFalse() throws Exception {
    when(mockCredential.createScopedRequired()).thenReturn(false);

    TransferClientCreator.createStorageTransferClient(
        Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), mockCredential);

    verify(mockBuilder).build();
  }

  /**
   * Tests whether createStorageTransferClient() makes the API call for building clients when the
   * credential requires a Scope.
   */
  @Test
  public void testCreateStorageTransferClientScopedRequiredTrue() throws Exception {
    when(mockCredential.createScopedRequired()).thenReturn(true);
    when(mockCredential.createScoped(Matchers.anyCollectionOf(String.class)))
        .thenReturn(mockCredential);

    TransferClientCreator.createStorageTransferClient(
        Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(), mockCredential);

    verify(mockBuilder).build();
  }
}
