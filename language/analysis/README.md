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
#######################################
# Performs a language operation on the given text or GCS object.
# Globals:
#   None
# Arguments:
#   $1 The operation to perform, either entities, sentiment, or syntax.
#   $2 The text or GCS object to operate on.
# Returns:
#   None
#######################################
function run_nl() {
  local main_class=com.google.cloud.language.samples.Analyze
  local jar_file=target/language-entities-1.0-jar-with-dependencies.jar
  java -cp ${jar_file} ${main_class} $1 "$2"
}
run_nl entities "The quick brown fox jumped over the lazy dog."
run_nl sentiment "The quick brown fox jumped over the lazy dog."
run_nl syntax "The quick brown fox jumped over the lazy dog."
```

Additional examples:
```
#######################################
# Exercises the sample code on various example text and GCS objects.
# Globals:
#   None
# Arguments:
#   None
# Returns:
#   None
#######################################
function run_nl_all() {
  local main_class=com.google.cloud.language.samples.Analyze
  local jar_file=target/language-entities-1.0-jar-with-dependencies.jar
  local quote="Larry Page, Google's co-founder, once described the 'perfect search
      engine' as something that 'understands exactly what you mean and gives you
      back exactly what you want.' Since he spoke those words Google has grown to
      offer products beyond search, but the spirit of what he said remains."
  local gs_path="gs://bucket/file"

  java -cp ${jar_file} ${main_class} entities "${quote}"
  java -cp ${jar_file} ${main_class} entities "${gs_path}"
  java -cp ${jar_file} ${main_class} sentiment "${quote}"
  java -cp ${jar_file} ${main_class} sentiment "${gs_path}"
  java -cp ${jar_file} ${main_class} syntax "${quote}"
  java -cp ${jar_file} ${main_class} syntax "${gs_path}"
}

run_nl_all
```
