# Google Cloud Spanner Sample

This sample demonstrates how to use [Google Cloud Spanner][spanner-docs]
from [Google App Engine standard environment][ae-docs].

[spanner-docs]: https://cloud.google.com/spanner/docs/
[ae-docs]: https://cloud.google.com/appengine/docs/java/


## Setup
- Install the [Google Cloud SDK](https://cloud.google.com/sdk/) and run:
```
   gcloud init
```
If this is your first time creating an App engine application:
```
   gcloud app create
```
- [Create a Spanner instance](https://cloud.google.com/spanner/docs/quickstart-console#create_an_instance).

- Update system properties in `[appengine-web.xml](src/main/webapp/WEB-INF/appengine-web.xml):
    - Required : `SPANNER_INSTANCE`
    - Optional : `SPANNER_DATABASE`,
      A database will created using the `SPANNER_DATABASE` name if provided, else will be auto-generated.

## Endpoints
- `/run` : will run sample operations against the spanner instance in order. Individual tasks can be run
using the `task` query parameter. See [SpannerTasks](src/main/java/com/example/appengine/spanner/SpannerTasks.java)
for supported set of tasks.
Note : by default all the spanner example operations run in order, this operation may take a while to return.

## Running locally
There are known IAM permission issues running this sample locally.

## Deploying

    $ mvn clean appengine:deploy

To see the results of the deployed sample application, open
`https://spanner-dot-PROJECTID.appspot.com/run` in a web browser.

Note : by default all the spanner example operations run in order, this operation may take a while to return.

