# Cloud Identity-Aware Proxy Java Samples
Cloud Identity-Aware Proxy (Cloud IAP) lets you manage access to applications running in Compute Engine, App Engine standard environment, and Container Engine.
Cloud IAP establishes a central authorization layer for applications accessed by HTTPS,
enabling you to adopt an application-level access control model instead of relying on network-level firewalls.
 When you enable Cloud IAP, you must also use signed headers or the App Engine standard environment Users API to secure your app.

## Setup
- A Google Cloud project with billing enabled
- A service account with private key credentials is required to create signed bearer tokens.
  - [Create an App engine service account](https://cloud.google.com/docs/authentication#getting_credentials_for_server-centric_flow) and download the credentials file as JSON.
  - Set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` to point to the service account credentials file.
- Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run:
```
   gcloud init
```

## Description
- [BuildIapRequest.java](src/main/java/com/example/iap/BuildIapRequest.java) demonstrates how to set the
`Authorization : Bearer` header with a signed JWT token to authorize access to an IAP protected URL.
- [VerifyIapRequestHeader.java](src/main/java/com/example/iap/VerifyIapRequestHeader.java) demonstrates how to
verify the JWT token in an incoming request to an IAP protected resource.

## Testing
- Deploy the [demo app engine application](../appengine/iap/README.md). This application will return the JWT token to an authorized incoming request.
It will be used to test both the authorization of an incoming request to an IAP protected resource and the JWT token returned from IAP.

- [Enable](https://cloud.google.com/iap/docs/app-engine-quickstart) Identity-Aware Proxy on the App Engine app.

- Add the service account email to the Identity-Aware Proxy access list for the project.

- Set the following environment variables to test sending a request to an IAP protected resource:
  - `IAP_PROTECTED_URL` : URL of your IAP protected resource . eg. `https://your-project-id.appspot.com`

  - `IAP_CLIENT_ID` to point to the [OAuth 2.0 Client ID](https://console.cloud.google.com/apis/credentials) of your IAP protected App Engine Application.

- Set the following environment variables to test verifying a JWT issued for an App Engine protected application:
  - `GOOGLE_CLOUD_PROJECT`: Google Cloud Project ID

  - `IAP_PROJECT_NUMBER` : [Project number](https://console.cloud.google.com/home/dashboard) of the IAP protected resource.
  Also available via `gcloud` using:
    ```
       gcloud projects describe PROJECT_ID
    ```

- Run the integration test:
```
    mvn -Dtest=com.example.iap.BuildAndVerifyIapRequestIT verify
```

## References
- [JWT library for Java (jjwt)](https://github.com/jwtk/jjwt)
- [Cloud IAP docs](https://cloud.google.com/iap/docs/)
- [Service account credentials](https://cloud.google.com/docs/authentication#getting_credentials_for_server-centric_flow)

## Known issues
- [Auth0 JWT library](https://github.com/auth0/java-jwt) has intermittent IAP token verification issues on OpenJDK.
