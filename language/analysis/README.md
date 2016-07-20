# Google Cloud Natural Language API Entity Recognition Sample

This sample demonstrates the use of the [Google Cloud Natural Language API][NL-Docs]
for entity recognition.

[NL-Docs]: https://cloud.google.com/language/docs/

## Java Version

This sample requires you to have
[Java8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html).

## Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

## Run the sample

To build the sample, we use Maven.

```bash
mvn clean compile assembly:single
```

We can then run the assembled JAR file with the `java` command. The variable $COMMAND takes
three values `entities`, `sentiment` or `syntax`.

```
MAIN_CLASS=com.google.cloud.language.samples.Analyze
JAR_FILE=target/entities-1.0-SNAPSHOT-jar-with-dependencies.jar
java -cp $JAR_FILE $MAIN_CLASS <sentiment|entities|syntax> <text>
```

Example usage:

```
QUOTE="Larry Page, Google's co-founder, once described the 'perfect search
    engine' as something that 'understands exactly what you mean and gives you
    back exactly what you want.' Since he spoke those words Google has grown to
    offer products beyond search, but the spirit of what he said remains."

java -cp $JAR_FILE $MAIN_CLASS entities "$QUOTE"
java -cp $JAR_FILE $MAIN_CLASS sentiment "$QUOTE"
java -cp $JAR_FILE $MAIN_CLASS syntax "$QUOTE"
```

