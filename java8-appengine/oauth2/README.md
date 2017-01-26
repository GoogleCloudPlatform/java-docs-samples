# Google App Engine Standard Environment
## Oauth2 Sample

This sample demonstrates using the Oauth2 apis to create an authenticaion filter.

See the [Google App Engine standard environment documentation][ae-docs] for more
detailed instructions.


## Setup
1. In the [Cloud Developers Console](https://cloud.google.com/console) > API Manager > Credentials,
create a Oauth Client ID for a Web Application.  You will need to provide an authroized JavaScript
origin.  Typically, https://projectID.appspot.com.
1. Edit `src/main/webapp/index.html` and change `YOUR_CLIENT_ID_HERE.apps.googleusercontent.com` to
Client ID from the prior step.

## Running locally
NOTE: The app can be run locally, but the Oauth2 APIs do not work with the development server.

    $ mvn appengine:devserver

## Deploying
    $ mvn appengine:update -Dappengine.appId=YOUR-PROJECT-ID -Dappengine.version=SOME-VERSION

1. Using your browser, visit `https://YOUR-PROJECT-ID.appspot.com`, click Sign In.

1. The Sign In process will then request some text from your app, and then display it, if
the id matches the list in `src/main/java/com/example/appengine/Oauth2Filter.java`.

## Adding you to the list of valid users
NOTE: Typically, you would use this for Service Accounts, but user accounts work as well.

1. Enable logging by uncommenting the context.log line in
`src/main/java/com/example/appengine/Oauth2Filter.java`, redeploy, and visit the page
1. Look at the logs in [Cloud Developers Console](https://cloud.google.com/console) > Logs.

1. Add the `tokenAudience` to the `allowedClients`.

1. Deploy and visit the page again.

[ae-docs]: https://cloud.google.com/appengine/docs/java/
