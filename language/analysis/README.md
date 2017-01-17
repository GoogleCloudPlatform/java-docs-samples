# Google Cloud Natural Language API Entity Recognition Sample

This sample demonstrates the use of the [Google Cloud Natural Language API][NL-Docs]
for entity recognition.

[NL-Docs]: https://cloud.google.com/natural-language/docs/

## Java Version

This sample requires you to have
[Java8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html).

**Note** The Natural Language client is not supported by App Engine Standard.

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
three values `entities`, `sentiment`, or `syntax`.

## Basic usage:

```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
    com.google.cloud.language.samples.Analyze \
    <entities | sentiment | syntax> \
    <text | GCS path>
```

### Usage Examples
Analyze entities
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
    com.google.cloud.language.samples.Analyze \
    entities \
    "The quick brown fox jumped over the lazy dog."
```

Analyze sentiment
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
    com.google.cloud.language.samples.Analyze \
    sentiment \
    "The quick brown fox jumped over the lazy dog."
```

Analyze syntax
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
    com.google.cloud.language.samples.Analyze \
    syntax \
    "The quick brown fox jumped over the lazy dog."
```

Included with the sample are `demo.sh` and `demo.bat` which show additional
examples of usage.

Run demo from *nix or OSX
```
demo.sh
```

Run demo from Windows
```
demo
```
