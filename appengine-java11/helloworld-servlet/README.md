# Hello World Servlet on Google App Engine Standard with Java 11

This sample demonstrates migrating a `WAR` packaged servlet
to Java 11. To migrate to the Java 11 runtime, your application must have a
`Main` class that starts a web server. This sample is dependent on artifact
[`appengine-simple-jetty-main`](../appengine-simple-jetty-main) to provide a
`Main` class that starts an embedded Jetty server.

## Setup

The `pom.xml` has been updated accordingly:
- Update maven compiler to use Java version 11:
```
<properties>
  <maven.compiler.source>11</maven.compiler.source>
  <maven.compiler.target>11</maven.compiler.target>
  <failOnMissingWebXml>false</failOnMissingWebXml>
</properties>
```

- Add the `appengine-simple-jetty-main` dependency:
```
<dependency>
  <groupId>com.example.appengine.demo</groupId>
  <artifactId>simple-jetty-main</artifactId>
  <version>1</version>
  <scope>provided</scope>
</dependency>
```
**Note: this dependency needs to be installed locally.**

- On deployment, the App Engine runtime uploads files located in
`${build.directory}/appengine-staging`. Add the `maven-dependency-plugin` to
the build in order to copy dependencies to the correct folder:
```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-dependency-plugin</artifactId>
  <version>3.1.1</version>
  <executions>
    <execution>
      <id>copy</id>
      <phase>prepare-package</phase>
      <goals>
        <goal>copy-dependencies</goal>
      </goals>
      <configuration>
        <outputDirectory>
          ${project.build.directory}/appengine-staging
        </outputDirectory>
      </configuration>
    </execution>
  </executions>
</plugin>
```

The `appengine-web.xml` file has been removed and an
[`app.yaml`](src/main/appengine/app.yaml) has been added to manage your
application settings:
- The entrypoint field will start the Jetty server and load your `WAR` file.
```
runtime: java11
entrypoint: 'java -cp "*" com.example.appengine.demo.jettymain.Main helloworld.war'
```

## Running locally
The `exec-maven-plugin` has been added to `appengine-simple-jetty-main` so you
can run your application locally.

- Package your app:
```
mvn clean package
```

- Move into the directory:
```
cd ../appengine-simple-jetty-main
```

- Install the dependency:
```
mvn install
```

- Start the server with your `WAR` file as an argument:
```
mvn exec:java -Dexec.args="../helloworld-servlet/target/helloworld.war"
```

Then visit: http://localhost:8080/hello

## Deploying
While in the `helloworld-servlet` directory, use the `appengine-maven-plugin` to
deploy your app:
```
mvn clean package appengine:deploy
```
Then visit:  https://YOUR-PROJECT-ID.appspot.com/hello
