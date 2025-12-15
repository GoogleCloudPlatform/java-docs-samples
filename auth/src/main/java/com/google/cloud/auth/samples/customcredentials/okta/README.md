# Running the Custom Okta Credential Supplier Sample

This sample demonstrates how to use a custom subject token supplier to authenticate with Google Cloud using Okta as an external identity provider. It uses the Client Credentials flow for machine-to-machine (M2M) authentication.

## Prerequisites

*   An Okta developer account.
*   A Google Cloud project with the IAM API enabled.
*   A Google Cloud Storage bucket. Ensure that the authenticated user has access to this bucket.
*   Java 11 or later installed.
*   Maven installed.

## Okta Configuration

Before running the sample, you need to configure an Okta application for Machine-to-Machine (M2M) communication.

### Create an M2M Application in Okta

1.  Log in to your Okta developer console.
2.  Navigate to **Applications** > **Applications** and click **Create App Integration**.
3.  Select **API Services** as the sign-on method and click **Next**.
4.  Give your application a name and click **Save**.

### Obtain Okta Credentials

Once the application is created, you will find the following information in the **General** tab:

*   **Okta Domain**: Your Okta developer domain (e.g., `https://dev-123456.okta.com`).
*   **Client ID**: The client ID for your application.
*   **Client Secret**: The client secret for your application.

You will need these values to configure the sample.

## Google Cloud Configuration

You need to configure a Workload Identity Pool in Google Cloud to trust the Okta application.

### Set up Workload Identity Federation

1.  In the Google Cloud Console, navigate to **IAM & Admin** > **Workload Identity Federation**.
2.  Click **Create Pool** to create a new Workload Identity Pool.
3.  Add a new **OIDC provider** to the pool.
4.  Configure the provider with your Okta domain as the issuer URL.
5.  Map the Okta `sub` (subject) assertion to a GCP principal.

For detailed instructions, refer to the [Workload Identity Federation documentation](https://cloud.google.com/iam/docs/workload-identity-federation).

## Running the Sample

To run the sample on your local system, you need to build the project and configure your credentials as environment variables.

### 1. Build the Project

This command compiles your code and downloads all dependencies.
```bash
mvn clean package
```

### 2. Configure Credentials

This sample reads its configuration from environment variables. Set the following variables in your shell:

*   `OKTA_DOMAIN`: Your Okta developer domain (for example `https://dev-123456.okta.com`).
*   `OKTA_CLIENT_ID`: The client ID for your application.
*   `OKTA_CLIENT_SECRET`: The client secret for your application.
*   `GCP_WORKLOAD_AUDIENCE`: The audience for the Google Cloud Workload Identity Pool. This is the full identifier of the Workload Identity Pool provider.
*   `GCS_BUCKET_NAME`: The name of the Google Cloud Storage bucket to access.
*   `GCP_SERVICE_ACCOUNT_IMPERSONATION_URL`: (Optional) The URL for service account impersonation.

Example:
```bash
export OKTA_DOMAIN="https://dev-123456.okta.com"
export OKTA_CLIENT_ID="your-client-id"
# ... and so on for the other variables
```

### 3. Run the Application

First, generate the classpath file:
```bash
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```

Now, run the application, providing the generated classpath.
```bash
java -cp "target/auth-1.0.jar:$(cat cp.txt)" com.google.cloud.auth.samples.customcredentials.okta.CustomCredentialSupplierOktaWorkload
```

The script authenticates with Okta to get an OIDC token, exchanges that token for a Google Cloud federated token, and uses it to list metadata for the specified Google Cloud Storage bucket.

## Testing

This sample is not continuously tested. It is provided for instructional purposes and may require modifications to work in your environment.
