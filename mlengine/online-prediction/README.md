# Cloud Machine Learning Engine - Online Prediction with Java

## Setup
This sample demonstrates how to send online prediction requests to your deployed 
model on CMLE. 
Follow the [tutorial](https://cloud.google.com/ml-engine/docs/deploying-models)
to deploy your model first.

This sample is using the [Application Default Credential](https://developers.google.com/identity/protocols/application-default-credentials). You can install the Google Cloud SDK and run:
<pre>gcloud auth application-default login</pre>

## Run
Modify the OnlinePredictionSample.java with your project/model/version information.

Compile the sample code using Maven by running the following command:
<pre>mvn compile</pre>
Execute the sample code using Maven by running the following command:
<pre>mvn -q exec:java</pre>
