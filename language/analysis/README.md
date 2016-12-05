# Google Cloud Natural Language API Entity Recognition Sample

This sample demonstrates the use of the [Google Cloud Natural Language API][NL-Docs]
for entity recognition.

[NL-Docs]: https://cloud.google.com/natural-language/docs/

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
three values `entities`, `sentiment`, or `syntax`.

Basic usage:

```
function run_nl {
  local MAIN_CLASS=com.google.cloud.language.samples.Analyze
  local JAR_FILE=target/language-entities-1.0-jar-with-dependencies.jar
  java -cp ${JAR_FILE} ${MAIN_CLASS} $1 "$2"
}
run_nl entities "The quick fox jumped over the lazy dog."
run_nl sentiment "The quick fox jumped over the lazy dog."
run_nl syntax "The quick fox jumped over the lazy dog."
```

Additional examples:
```
function run_nl_all {
  local MAIN_CLASS=com.google.cloud.language.samples.Analyze
  local JAR_FILE=target/language-entities-1.0-jar-with-dependencies.jar
  local QUOTE="Larry Page, Google's co-founder, once described the 'perfect search
      engine' as something that 'understands exactly what you mean and gives you
      back exactly what you want.' Since he spoke those words Google has grown to
      offer products beyond search, but the spirit of what he said remains."
  local GS_PATH="gs://bucket/file"

  java -cp ${JAR_FILE} ${MAIN_CLASS} entities "${QUOTE}"
  java -cp ${JAR_FILE} ${MAIN_CLASS} entities "${GS_PATH}"
  java -cp ${JAR_FILE} ${MAIN_CLASS} sentiment "${QUOTE}"
  java -cp ${JAR_FILE} ${MAIN_CLASS} sentiment "${GS_PATH}"
  java -cp ${JAR_FILE} ${MAIN_CLASS} syntax "${QUOTE}"
  java -cp ${JAR_FILE} ${MAIN_CLASS} syntax "${GS_PATH}"
}

run_nl_all
```
