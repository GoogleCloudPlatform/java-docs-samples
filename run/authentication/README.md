# Authenticating service-to-service

This sample shows how to make an authenticated request by retrieving a JSON Web Tokens (JWT) from the [metadata server](https://cloud.google.com/run/docs/securing/service-identity#identity_tokens).

For more details on how to work with this sample read [Authenticating service-to-service](https://cloud.google.com/run/docs/authenticating/service-to-service).

**Note** You cannot query an instance's metadata from another instance or directly from your local computer. For testing purposes, this sample uses the environment variable, `"GOOGLE_CLOUD_PROJECT"`, to determine local or instance environment. To run tests locally, make sure environment variable, `"GOOGLE_CLOUD_PROJECT"`, is not set.
