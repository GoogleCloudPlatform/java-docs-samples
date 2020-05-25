# Helidon MP Example for Google App Engine

This example implements a simple REST service using [Helidon MP](https://helidon.io/#/) and deployed to [Google App Engine](https://cloud.google.com/appengine) and on it's [Java 11 runtime](https://cloud.google.com/appengine/docs/standard/java11/quickstart).

# Part 1: Build a new Helidon MP Project
## Step 1 - Build a new Helidon MP Project
This project was built with this command:
```
mvn archetype:generate -DinteractiveMode=false \
    -DarchetypeGroupId=io.helidon.archetypes \
    -DarchetypeArtifactId=helidon-quickstart-mp \
    -DarchetypeVersion=2.0.0-M3 \
    -DgroupId=io.helidon.examples \
    -DartifactId=helidon-mp-gae-greet \
    -Dpackage=io.helidon.examples.quickstart.mp
```

## Anatomy of Folder Structure (listing only important files/folder):

```
helidon-mp-gae-greet
|_README.md
|_app.yaml
|_pom.xml [primary maven build file]
|_src
  |_main
  | |_java/io/helidon/examples/quickstart/mp
  |   |_GreetingProvider.java 
  |   |_GreetingResource.java [JAX-RS resource with api paths]
  |   |_package-info.java [list java packages used in the project] 
  | |_resources
  |   |_META-INF
  |     |_microprofile-config.properties [to manage IP and PORT]
  |   |_logging.properties [manage logs settings]
  |_test
```

## Step 2 - Build
The above command creates a Helidon MP example project with necessary boilerplate code.

```
$ cd helidon-mp-gae-greet/
$ mvn clean package
```

## Step 3 - Test/run

`$ java -jar ./target/helidon-mp-gae-greet.jar`

[The last line should show something like this..]

```
2020.05.21 12:09:35 INFO io.helidon.microprofile.server.ServerCdiExtension Thread[main,5,main]: Server started on http://localhost:8080 (and all other host addresses) in 2647 milliseconds (since JVM startup).
```

Open a new browser tab and http://localhost:8080/greet (i'm using a different port). *You may see a message "No handler found for path: /" if you go to http://localhost:8080, which is okay. we have not defined that.*

![helidon-mp-example-gae-localhost](https://user-images.githubusercontent.com/902972/82602591-f1005b00-9b76-11ea-872a-266392d73ca1.png "Sample page for 'greet'resource")

# Part 2: Preparing for Google App Engine

Helidon app may run only on [Java 11 runtime](https://cloud.google.com/appengine/docs/standard/java11) (i haven't tried it on Java 8 runtime).

The primary requirement for the application to be deployed to Google app engine is a yaml file.

## 1 - Remove the default `app.yaml` under the project root.

## 2 - Add `helidon-mp-app.yaml` in a new folder, like gae under `src/main`

```
helidon-mp-gae-greet
|_src
  |_main
    |_gae
      |_helidon-mp-app.yaml
```

Add the below to `helidon-mp-app.yaml`
```
runtime: java11
entrypoint: java -Xmx64m -jar ${project.artifactId}.jar
```

## 3 - The below is added to `pom.xml` (i know this maybe frustrating). Don't worry about all the details, this allows the helion-mp-gae.yaml file to be copied to `/target` folder.

```
...
<build>
  <plugins>
    <plugin>
     ...
    <plugin> 
    
    # --add from here..
    <plugin>                    
      <groupId>org.apache.maven.plugins</groupId>                  
      <artifactId>maven-resources-plugin</artifactId>                 
      <executions>
        <execution>
          <id>copy-gae-file</id>
          <phase>process-resources</phase>                    
          <goals>
            <goal>copy-resources</goal>
          </goals>
          <configuration>                            
            <outputDirectory>${project.build.directory}</outputDirectory>
            <resources>
              <resource>                                    
                <filtering>true</filtering>
                <directory>src/main/gae</directory>
                <includes>                                         
                  <include>helidon-mp-app.yaml</include>
                  <include>.gcloudignore</include>
                </includes>
              </resource>
            </resources>
          </configuration>
        </execution>
      </executions>
    </plugin>
    # --up to here 
</build>
```

## 4 - Rebuild the project
```
$ mvn clean package
```

## 5 - Setup Google App Engine

Create a new Project on Google Cloud. Create New App, Choose `Java` as Language and `Standard Environment`.



## 6 - Deploy to Google App Engine

Go to your project root and run this command.

*Tip: You will need Google Cloud SDK installed on your machine. I'm running this on my Mac. Run gcloud -v to make sure gcloud is installed.*

```
$ gcloud app deploy ./target/helidon-mp-app.yaml --project=<gcp-project-name>
```

You will see a confirmation..
```
$ gcloud app deploy ./target/helidon-mp-app.yaml --project=<gcp-project-name>
Services to deploy:
descriptor:      [/Projects/java/helidon/helidon-mp-gae-greet/target/helidon-mp-app.yaml]
source:          [/Projects/research/java/helidon/helidon-mp-gae-greet/target]
target project:  [<gcp-project-name>]
target service:  [default]
target version:  [20200521t141143]
target url:      [https://<gcp-project-name>.uc.r.appspot.com]
Do you want to continue (Y/n)?
```
and

```
Beginning deployment of service [default]...
Created .gcloudignore file. See `gcloud topic gcloudignore` for details.
╔════════════════════════════════════════════════════════════╗
╠═ Uploading 161 files to Google Cloud Storage              ═╣
╚════════════════════════════════════════════════════════════╝
File upload done.
Updating service [default]...done.
Setting traffic split for service [default]...done.
Deployed service [default] to [https://<gcp-project-name>.uc.r.appspot.com]
You can stream logs from the command line by running:
$ gcloud app logs tail -s default
To view your application in the web browser run:
$ gcloud app browse --project=<gcp-project-name>
```

## 7 - Test

Follow the url that shows up in confirmation, like `https://<project-id-on-gce>.uc.r.appspot.com/greet`

![helidon-mp-example-gae-verify-live-app](https://user-images.githubusercontent.com/902972/82605056-eea00000-9b7a-11ea-8055-435228a8f060.png)


## 8 - You may also verify the application on Google App Engine

<img width="1396" alt="helidon-mp-example-gae-gcp-verify-app" src="https://user-images.githubusercontent.com/902972/82605057-eea00000-9b7a-11ea-840b-69c15a4afedf.png">
