
### Running locally

    mvn exec:java

To use vist: http://localhost:8080/hello

### Deploying

    mvn clean package appengine:deploy -Dapp.deploy.projectId=<your-project-id>

To use vist:  https://YOUR-PROJECT-ID.appspot.com/hello
