# Google Cloud Translate Sample

This sample demonstrates the use of [Google Cloud Translate
API][Translate-Docs] for translating and detecting language text.

[Translate-Docs]: https://cloud.google.com/translate/docs/

## Java Version

This sample requires you to have
[Java8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html).

## Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started,
be
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

We can then run the assembled JAR file with the `java` command. The variable
$COMMAND takes two values `detect' and `translate'.

```
MAIN_CLASS=com.google.cloud.translate.samples.TranslateText
JAR_FILE=target/translate-1.0-SNAPSHOT-jar-with-dependencies.jar
java -cp $JAR_FILE $MAIN_CLASS <detect|translate|langsupport> <text>
<optional_source> <optional_target>
```

Example Usage:

```
INPUT="A quick brown fox jumped over a lazy dog."
SOURCE_LANG="en"
TARGET_LANG="fr"
```

Translate API Features:

 * Languages supported by the API
   ```
   java -cp $JAR_FILE $MAIN_CLASS langsupport
   ```

 * Detect input text language
   ```
   java -cp $JAR_FILE $MAIN_CLASS detect "$INPUT"
  ```

 * Translate input text (with options)
   ```
   java -cp $JAR_FILE $MAIN_CLASS translate "$INPUT"
   java -cp $JAR_FILE $MAIN_CLASS translate "$INPUT" $SOURCE_LANG $TARGET_LANG
   ```
