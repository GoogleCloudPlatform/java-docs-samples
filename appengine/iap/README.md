# Identity-Aware Proxy sample for Google App Engine

This sample demonstrates how to use the [Identity-Aware Proxy][iap-docs] on [Google App
Engine][ae-docs].

[iap-docs]: https://cloud.google.com/iap/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/

## Running locally

This application depends on being enabled behind an IAP, so this program should not be run locally.

## Deploying

- Update [appengine-web.xml](src/main/test/app/src/main/webapp/WEB-INF/appengine-web.xml) with your project-id
- Deploy the application to the project
  ```
       mvn clean appengine:update
    ```
- [Enable](https://cloud.google.com/iap/docs/app-engine-quickstart) Identity-Aware Proxy on the App Engine app.
- Add the email account you'll be running the test as to the Identity-Aware Proxy access list for the project.

## Test

Once deployed, access `https://your-project-id.appspot.com` . This should now prompt you to sign in for access.
Sign in with the email account that was added to the Identity-Aware proxy access list.
You should now see the jwt token that was received from the IAP server.
