# Cloud Speech API gRPC samples for Java

This is a sample repo for accessing the [Google Cloud Speech API](http://cloud.google.com/speech) with
[gRPC](http://www.grpc.io/) client library. Note that these samples are for `advanced users` and is in
BETA. Please see [Google Cloud Platform Launch Stages](https://cloud.google.com/terms/launch-stages).

## Prerequisites

### Enable the Speech API

If you have not already done so, [enable the Google Cloud Speech API for your project](https://console.developers.google.com/apis/api/speech.googleapis.com/overview).
You must be whitelisted to do this.


### Download and install Java and Maven

Install [Java7 or
higher](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html).

This sample uses the [Apache Maven][maven] build system. Before getting started, be
sure to [download][maven-download] and [install][maven-install] it. When you use
Maven as described here, it will automatically download the needed client
libraries.

[maven]: https://maven.apache.org
[maven-download]: https://maven.apache.org/download.cgi
[maven-install]: https://maven.apache.org/install.html


### Set Up to Authenticate With Your Project's Credentials

The example uses a service account for OAuth2 authentication.
So next, set up to authenticate with the Speech API using your project's
service account credentials.

Visit the [Cloud Console](https://console.developers.google.com), and navigate to:
`API Manager > Credentials > Create credentials >
Service account key > New service account`.
Create a new service account, and download the json credentials file.

Then, set
the `GOOGLE_APPLICATION_CREDENTIALS` environment variable to point to your
downloaded service account credentials before running this example:

    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/credentials-key.json

If you do not do this, you will see an error that looks something like this when
you run the example scripts:
`WARNING: RPC failed: Status{code=PERMISSION_DENIED, description=Request had insufficient authentication scopes., cause=null}`.
See the
[Cloud Platform Auth Guide](https://cloud.google.com/docs/authentication#developer_workflow)
for more information.

## Build the application

Then, build the program:

```sh
$ mvn package
```

or

```sh
$ mvn compile
$ mvn assembly:single
```

## Run the clients

These programs return the transcription of the audio file you provided.  Please
note that the audio file must be in RAW format.  You can use `sox`
(available, e.g. via [http://sox.sourceforge.net/](http://sox.sourceforge.net/)
or [homebrew](http://brew.sh/)) to convert audio files to raw format.

### Run the sync client

To use the sync method to transcribe a 16,000Hz sample audio file, run the following:

```sh
$ bin/speech-sample-sync.sh --host=speech.googleapis.com --port=443 \
--uri=resources/audio.raw --sampling=16000
```

### Run the async client

To use the async method to transcribe a 16,000Hz sample audio file, run the following:

```sh
$ bin/speech-sample-async.sh --host=speech.googleapis.com --port=443 \
--uri=resources/audio.raw --sampling=16000
```

### Run the streaming client

To use the streaming method to transcribe a 16,000Hz sample audio file, run the following:

```sh
$ bin/speech-sample-streaming.sh --host=speech.googleapis.com --port=443 \
--file=resources/audio.raw --sampling=16000
```

