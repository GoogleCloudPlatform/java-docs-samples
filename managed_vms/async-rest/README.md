
# Example Web App Using Asynchronous Servlets #

This web app demonstrates using asynchronous servlet techniques to reduce server resources.

The code for this tutorial is here: [https://github.com/GoogleCloudPlatform/java-docs-samples/managed_vms/async-rest](https://github.com/GoogleCloudPlatform/java-docs-samples/managed_vms/async-rest).


## Initial Setup ##

First, complete the following steps:

- [Create your project](https://developers.google.com/appengine/docs/managed-vms/) and have it enabled for Managed VMs.
- Obtain an app key for the Google Places WebService API.
- Download and install [the Beta build of the Google Cloud SDK](https://developers.google.com/cloud/sdk/#Quick_Start).
- Install the Cloud SDK `app-engine-java` component.
- Authenticate wth the gcloud SDK: gcloud auth login.
- Install [Maven](http://maven.apache.org/download.cgi) if you haven't already.

   

## Providing your Google Places API key ##

You will need to edit the pom.xml file and replace YOUR_PLACES_APP_KEY with the value of your key:

    <places.appkey>YOUR_PLACES_APP_KEY</places.appkey>

You then have several options of how to run it:

## Running locally without the AppEngine environment ##

The application does not use any AppEngine specific services, so you can run it simply on your local machine by doing: 

     mvn jetty:run 

Go to  http://localhost:8080 to see the webapp.
 

## Running locally using Docker ##

The project also  can build a docker image based on the jetty9 image for [Google Container Engine](https://cloud.google.com/container-engine/). 
First uncomment the maven plugin section for docker-maven-plugin in the pom.xml.
The WAR file is installed in the webapps directory and the resulting image can be run locally with:

    docker run --rm -it -p 8080:8080 jetty9-async-rest --exec -Dcom.google.appengine.demos.asyncrest.appKey=YOUR_PLACES_APP_KEY

Where you replace YOUR_PLACES_APP_KEY with the key you obtained in the initial setup.


## Deploying to the cloud as an AppEngine ManagedVM ##

To automatically stage and deploy the webapp to your project in the cloud do:  

    mvn gcloud:deploy  

See here for more information on the [GCloud Maven Plugin](https://github.com/GoogleCloudPlatform/gcloud-maven-plugin).

