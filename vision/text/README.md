# Text Detection using the Vision API

This sample requires Java 8.

## Download Maven

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html

## Introduction

This example uses the [Cloud Vision API](https://cloud.google.com/vision/) to
detect text within images, stores this text in an index, and then lets you
query this index.

## Initial Setup

### Install and Start Up a Redis server

This example uses a [redis](http://redis.io/) server, which must be up and
running before you start the indexing.  To install Redis, follow the
instructions on the [download page](http://redis.io/download), or install via a
package manager like [homebrew](http://brew.sh/) or `apt-get` as appropriate
for your OS.

The example assumes that the server is running on `localhost`, on the default
port, and it uses [redis
dbs](http://www.rediscookbook.org/multiple_databases.html) 0 and 1 for its data.
Edit the example code before you start if your redis settings are different.

### Set up OpenNLP

Download Tokenizer data and save it to this directory.

    wget http://opennlp.sourceforge.net/models-1.5/en-token.bin

## Run the sample

To build and run the sample, run the jar from this directory. You can provide a
directory to index the text in all the images it contains.

    mvn clean compile assembly:single
    java -cp target/vision-text-1.0-SNAPSHOT-jar-with-dependencies.jar com.google.cloud.vision.samples.text.TextApp data/

Once this builds the index, you can run the same command without the input path
to query the index.

    java -cp target/vision-text-1.0-SNAPSHOT-jar-with-dependencies.jar com.google.cloud.vision.samples.text.TextApp

