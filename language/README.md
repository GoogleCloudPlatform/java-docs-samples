# Google Cloud Natural Language API Samples

These samples demonstrate the use of the [Google Cloud Natural Language API][NL-Docs].

[NL-Docs]: https://cloud.google.com/language/docs/

## Prerequisites

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

## Samples

- [Analyze](analysis) is a command line tool to show case the features of the API.

