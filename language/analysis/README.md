# Google Cloud Natural Language API Entity Recognition Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=language/analysis/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


This sample demonstrates the use of the [Google Cloud Natural Language API][NL-Docs]
for entity recognition.

[NL-Docs]: https://cloud.google.com/natural-language/docs/

## Prerequisites

### Java Version

This sample requires you to have
[Java8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html).

**Note** The Natural Language client is not supported by App Engine Standard.


### Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

### Set Up to Authenticate With Your Project's Credentials

Please follow the [Set Up Your Project](https://cloud.google.com/natural-language/docs/getting-started#set_up_your_project)
steps in the Quickstart doc to create a project and enable the
Cloud Natural Language API. Following those steps, make sure that you
[Set Up a Service Account](https://cloud.google.com/natural-language/docs/common/auth#set_up_a_service_account),
and export the following environment variable:

```
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-project-credentials.json
```

[cloud-console]: https://console.cloud.google.com
[language-api]: https://console.cloud.google.com/apis/api/language.googleapis.com/overview?project=_
[adc]: https://cloud.google.com/docs/authentication#developer_workflow

## Run the sample

To build the sample, we use Maven.

```bash
mvn clean compile assembly:single
```

We can then run the assembled JAR file with the `java` command. The variable $COMMAND takes
three values `entities`, `entities-sentiment`, `sentiment`, or `syntax`.

## Basic usage:

```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
    com.google.cloud.language.samples.Analyze \
    <entities | entities-sentiment | sentiment | syntax> \
    <text | GCS path>
```

### Usage Examples (stable)

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

Analyze entity sentiment
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
  com.google.cloud.language.samples.Analyze entities-sentiment \
  "There's nothing better than searching for ice cream on Google."
```

Analyze syntax
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
    com.google.cloud.language.samples.Analyze \
    syntax \
    "The quick brown fox jumped over the lazy dog."
```

Analyze categories in text
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
  com.google.cloud.language.samples.Analyze classify \
  "Android is a mobile operating system developed by Google, based on the Linux kernel and designed primarily for touchscreen mobile devices such as smartphones and tablets."
```

Analyze categories in GCS file
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
  com.google.cloud.language.samples.Analyze classify \
  "gs://cloud-samples-tests/natural-language/android-text.txt"
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

### Usage Examples (beta)

Analyze sentiment beta
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
  com.google.cloud.language.samples.AnalyzeBeta \
  sentiment \
  "Der schnelle braune Fuchs sprang über den faulen Hund."
```

Analyze categories in text Beta
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
  com.google.cloud.language.samples.AnalyzeBeta classify \
  "Android is a mobile operating system developed by Google, based on the Linux kernel and designed primarily for touchscreen mobile devices such as smartphones and tablets."
```

Analyze categories in GCS file Beta
```
java -cp target/language-entities-1.0-jar-with-dependencies.jar \
  com.google.cloud.language.samples.AnalyzeBeta classify \
  "gs://cloud-samples-tests/natural-language/android-text.txt"
```

Run beta demo from *nix or OSX
```
demo-beta.sh
```
