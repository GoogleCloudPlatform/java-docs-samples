# Embedded Jetty Server for Google App Engine Standard with Java 11

To migrate to the Java 17 or latest runtime, your application must have a
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
  <groupId>com.example.appengine</groupId>
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
entrypoint: 'java -cp "*" com.example.appengine.jetty.Main helloworld.war'
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

[google-cloud-doc](https://cloud.google.com/appengine/docs/flexible/java/war-packaging)

[3rd-party](https://qiita.com/Yamane@github/items/fe057e48bb64ffc7fca6)

[Stack-overflow](https://stackoverflow.com/questions/13030675/could-not-find-or-load-main-class-with-a-jar-file)

## 1. Manage Jetty

```sh
.
├─src
│  └─main
│      └─java
│          └─jetty
│              └─Main.java
└─pom.xml

```

## New POM file

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example.appengine</groupId>
  <artifactId>simple-jetty-main</artifactId>
  <name>simplejettymain-j17</name>
  <version>1</version>
  <packaging>jar</packaging>

  <!--
    The parent pom defines common style checks and testing strategies for our samples.
    Removing or replacing it should not effect the execution of the samples in anyway.
  -->
  <parent>
    <groupId>com.google.cloud.samples</groupId>
    <artifactId>shared-configuration</artifactId>
    <version>1.2.0</version>
  </parent>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <jetty.version>9.4.56.v20240826</jetty.version>
  </properties>

  <!-- [START gae_java11_server_dependencies] -->
  <dependencies>
    <!-- Embedded Jetty dependencies -->
    <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-server</artifactId>
      <version>${jetty.version}</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-webapp</artifactId>
      <version>${jetty.version}</version>
      <type>jar</type>
    </dependency>
    <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-util</artifactId>
      <version>${jetty.version}</version>
    </dependency>
    <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-annotations</artifactId>
      <version>${jetty.version}</version>
    </dependency>
    <!-- extra explicit dependency needed because there is a JSP in the sample-->
    <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>apache-jsp</artifactId>
      <version>${jetty.version}</version>
    </dependency>
  </dependencies>
  <!-- [END gae_java11_server_dependencies] -->

  <build>
    <plugins>
      <!-- Exec Maven Plugin provides goals to help execute Main class locally -->
      <!-- [START gae_java11_exec_plugin] -->

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-assembly-plugin</artifactId>
        <version>3.0.0</version>
        <configuration>
          <finalName>jetty</finalName>
          <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
          </descriptorRefs>
          <archive>
            <manifest>
              <mainClass>com.example.appengine.jetty.Main</mainClass>
            </manifest>
          </archive>
        </configuration>
        <executions>
          <execution>
            <id>make-assembly</id>
            <phase>package</phase>
            <goals>
              <goal>single</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

<!--      <plugin>-->
<!--        <groupId>org.codehaus.mojo</groupId>-->
<!--        <artifactId>exec-maven-plugin</artifactId>-->
<!--        <version>3.1.1</version>-->
<!--        <executions>-->
<!--          <execution>-->
<!--            <goals>-->
<!--              <goal>java</goal>-->
<!--            </goals>-->
<!--          </execution>-->
<!--        </executions>-->
<!--        <configuration>-->
<!--          <mainClass>com.example.appengine.jetty.Main</mainClass>-->
<!--        </configuration>-->
<!--      </plugin>-->

<!--      <plugin>-->
<!--        <groupId>org.apache.maven.plugins</groupId>-->
<!--        <artifactId>maven-dependency-plugin</artifactId>-->
<!--        <version>3.1.1</version>-->
<!--        <executions>-->
<!--          <execution>-->
<!--            <id>copy</id>-->
<!--            <phase>prepare-package</phase>-->
<!--            <goals>-->
<!--              <goal>copy-dependencies</goal>-->
<!--            </goals>-->
<!--            <configuration>-->
<!--              <outputDirectory>-->
<!--                ${project.build.directory}/appengine-staging-->
<!--              </outputDirectory>-->
<!--            </configuration>-->
<!--          </execution>-->
<!--        </executions>-->
<!--      </plugin>-->
      <!-- [END gae_java11_exec_plugin] -->

    </plugins>
  </build>
</project>

```

## Check the operation

Place the two files we created, "jetty-jar-with-dependencies.jar" and "sample.war", in the same directory and run it.

```sh
java -jar jetty-jar-with-dependencies.jar sample.war
```

## Next, create “app.yaml”

```yaml
runtime: java17

env: standard

entrypoint: "java -jar jetty-jar-with-dependencies.jar sample.war"

handlers:
  - url: /.*
    script: this field is required, but ignored
```

## Finally, deploy

Put the three files "jetty-jar-with-dependencies.jar", "sample.war" and "app.yaml" in the same directory and then

```sh
gcloud app deploy
```
