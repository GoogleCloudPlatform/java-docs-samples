package com.google.cloud.reseller.subscription.samples;

import static com.google.cloud.reseller.subscription.samples.Constants.TARGET_SERVICE_ACCOUNT_EMAIL;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;

import com.google.auth.oauth2.OAuth2Utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** A utility wrapper to impersonate a target service account for a list of provided oauth scopes. */
public class ImpersonateServiceAccount {
  public static final String USER_HOME = System.getProperty("user.home");
  private ImpersonatedCredentials impersonatedCredentials;

  public ImpersonateServiceAccount(List<String> oauthScopes, String targetServiceAccount) {
    try {
      // File name output from: $gcloud auth application-default login
      GoogleCredentials sourceCredentials = GoogleCredentials.fromStream(new FileInputStream(
          USER_HOME.concat("/.config/gcloud/application_default_credentials.json")));

      impersonatedCredentials = ImpersonatedCredentials.newBuilder()
          .setSourceCredentials(sourceCredentials)
          .setTargetPrincipal(targetServiceAccount)
          .setScopes(oauthScopes)
          .setDelegates(null) //Optional delegation
          .setHttpTransportFactory(OAuth2Utils.HTTP_TRANSPORT_FACTORY)
          .build();

    } catch (IOException e) {
      System.err.println("Failed to get impersonated credentials: " + e.getMessage());
      System.err.println(
          "Ensure your source credentials have the 'Service Account Token Creator' role on the target SA and that ADC is set up correctly.");
    }
  }

  public void refreshCredentials() throws IOException {
    impersonatedCredentials.refresh();
  }

  public String getAccessToken() {
    return  impersonatedCredentials.getAccessToken().getTokenValue();
  }

  public ImpersonatedCredentials getImpersonatedCredentials() {
    return impersonatedCredentials;
  }

  public static void main(String[] args) throws IOException {
    List<String> scopes = Collections.singletonList(
        "https://www.googleapis.com/auth/youtube.commerce.partnership.integrated-billing");

    ImpersonateServiceAccount impersonateServiceAccount = new ImpersonateServiceAccount(scopes, TARGET_SERVICE_ACCOUNT_EMAIL);
    impersonateServiceAccount.refreshCredentials();
    System.out.printf("AccessToken:: [%s]\n", impersonateServiceAccount.getAccessToken());
  }
}

// mvn clean compile
// mvn exec:java -Dexec.mainClass="com.google.cloud.reseller.subscription.samples.ImpersonateServiceAccount"
