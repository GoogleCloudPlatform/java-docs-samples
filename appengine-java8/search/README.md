# Google App Engine Standard Environment Search API Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=appengine-java8//README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


This sample demonstrates how to use App Engine Search API.

See the [Google App Engine Search API documentation][search-api-docs] for more
detailed instructions.

[search-api-docs]: https://cloud.google.com/appengine/docs/java/search/

## Setup
* `gcloud init`

## Running locally
    $ mvn appengine:run

## Deploying
    $ mvn appengine:deploy
    
## URI's

`/search` | Create a Search API Document
`/search/index` | Index a document 
`/search/delete` | Delete a search document
`/search/option` | Search w/ query options
`/search/search` | Search for a document
`/search/schema` | Display the schema of a document
