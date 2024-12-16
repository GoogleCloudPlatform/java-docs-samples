# Cloud Identity-Aware Proxy sample for Google App Engine

This sample demonstrates how to use the [Cloud Identity-Aware Proxy][iap-docs] on [Google App
Engine][ae-docs].

[iap-docs]: https://cloud.google.com/iap/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Setup

Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run:
```
   gcloud init
```
If this is your first time creating an App engine application:
```
   gcloud app create
```

## Running locally

This application depends on being enabled behind an IAP, so this program should not be run locally.

## Deploying

- Deploy the application to the project
  ```
       mvn clean package appengine:deploy
    ```
- [Enable](https://cloud.google.com/iap/docs/app-engine-quickstart) Identity-Aware Proxy on the App Engine app.
- Add the email account you'll be running the test as to the Identity-Aware Proxy access list for the project.

## Test

Once deployed, access `https://your-project-id.appspot.com` . This should now prompt you to sign in for access.
Sign in with the email account that was added to the Identity-Aware proxy access list.
You should now see the jwt token that was received from the IAP server.
