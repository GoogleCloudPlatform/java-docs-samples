# App Engine Firebase Event Proxy

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8/firebase-event-proxy-README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

An example app that illustrates how to create a Java App Engine Standard Environment
app that proxies Firebase events to another App Engine app.

# Java Firebase Event Proxy
Illustrates how to authenticate and subscribe to Firebase from Java App Engine.

# Python App Engine Listener
Illustrates how to authenticate messages received from the proxy app.

## Setup

### Java Firebase Event Proxy
Firebase Secret
Put your Firebase secret in the file:
gae-firebase-event-proxy/src/main/webapp/firebase-secret.properties
```
firebaseSecret=<Your Firebase secret>
```

* Billing must be enabled from Cloud console.
* Manual scaling should turned on and configured to 1 instance in appengine-web.xml

## Running locally
### Java Firebase Event Proxy
```
cd gae-firebase-event-proxy
mvn appengine:run
```

### Python App Engine Listener
```
cd gae-firebase-listener-python
dev_appserver .
```

## Deploying

### Java Firebase Event Proxy
```
cd gae-firebase-event-proxy
mvn appengine:deploy
```

### Python App Engine Listener
```
appcfg.py -A <your app id> -V v1 update gae-firebase-listener-python
```
