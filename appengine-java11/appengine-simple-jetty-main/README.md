# Embedded Jetty Server for Google App Engine Standard with Java 11

To migrate to the Java 11 runtime, your application must have a
`Main` class that starts a web server. This sample is a shared artifact that
provides a `Main` class to instantiate an HTTP server to run an embedded web
application `WAR` file.

For more information on the Java 11 runtime, see
[Migrating your App Engine app from Java 8 to Java 11](https://cloud.google.com/appengine/docs/standard/java11/java-differences).

## Install the dependency

This sample is used as a dependency and must be installed locally:

```
mvn install
```

## Using the dependency

See [`helloworld-servlet`](../helloworld-servlet) for a complete example.

Your project's `pom.xml` needs to be updated accordingly:

- Add the `appengine-simple-jetty-main` dependency:

```
<dependency>
  <groupId>com.example.appengine.demo</groupId>
  <artifactId>simple-jetty-main</artifactId>
  <version>1</version>
  <scope>provided</scope>
</dependency>
```

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

To use the dependency, add the entrypoint to your `app.yaml` file. The
entrypoint field will start the Jetty server and load your `WAR` file.

```
runtime: java11
entrypoint: 'java -cp "*" com.example.appengine.demo.jettymain.Main helloworld.war'
```

## Running locally

The [Exec Maven Plugin][exec-plugin] has been added so you can run your
application locally. It is possible to use the [Jetty Maven Plugin][jetty-plugin]
for rapid development and testing, but using the Exec Maven Plugin will ensure
the provided server is running your application as expected.

- Start the server with your `WAR` file as an argument:

```
mvn exec:java -Dexec.args="../sample/target/sample.war"
```

[jetty-plugin]: https://www.eclipse.org/jetty/documentation/9.4.x/jetty-maven-plugin.html
