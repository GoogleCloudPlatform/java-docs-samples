package com.google.storagetransfer.samples;

import java.io.IOException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.storagetransfer_googleapis.StoragetransferGoogleapis;
import com.google.api.services.storagetransfer_googleapis.StoragetransferGoogleapisScopes;
import com.google.common.base.Preconditions;

public class TransferClientCreator {
  public static StoragetransferGoogleapis createStorageTransferClient() throws IOException {
    return createStorageTransferClient(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory());
  }

  public static StoragetransferGoogleapis createStorageTransferClient(HttpTransport httpTransport,
    JsonFactory jsonFactory) throws IOException {
    Preconditions.checkNotNull(httpTransport);
    Preconditions.checkNotNull(jsonFactory);
    GoogleCredential credential = GoogleCredential
      .getApplicationDefault(httpTransport, jsonFactory);
    // In some cases, you need to add the scope explicitly.
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(StoragetransferGoogleapisScopes.all());
    }
    // Please use custom HttpRequestInitializer for automatic
    // retry upon failures. We provide a simple reference
    // implementation in the "Retry Handling" section.
    HttpRequestInitializer initializer = new RetryHttpInitializerWrapper(credential);
    return new StoragetransferGoogleapis.Builder(httpTransport, jsonFactory, initializer)
      .setApplicationName("storagetransfer-sample").build();
  }
}
