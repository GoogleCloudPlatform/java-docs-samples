# AutoML Translate Sample

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=vision/beta/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>


[Google Cloud AutoML Translation API][translate] provides feature AutoML.

This API is part of the larger collection of Cloud Machine Learning APIs.

This sample Java application demonstrates how to access the Cloud Translate AutoML API
using the [Google Cloud Client Library for Java][google-cloud-java].


[translate]: https://cloud.google.com/translate/automl/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

## Set the environment variables

PROJECT_ID = [Id of the project]
REGION_NAME = [Region name]

## Build the sample

Install [Maven](http://maven.apache.org/).

Build your project with:

```
mvn clean package
```

### Dataset API

#### Create a new dataset
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.DatasetApi" -Dexec.args="create_dataset test_dataset"
```

#### List datasets
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.DatasetApi" -Dexec.args="list_datasets"
```

#### Get dataset
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.DatasetApi" -Dexec.args="get_dataset [dataset-id]"
```

#### Import data
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.DatasetApi" -Dexec.args="import_data [dataset-id] gs://java-docs-samples-testing/en-ja.csv"
```

### Model API

#### Create Model
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.ModelApi" -Dexec.args="create_model test_model"
```

#### List Models
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.ModelApi" -Dexec.args="list_models"
```

#### Get Model
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.ModelApi" -Dexec.args="get_model [model-id]"
```

#### List Model Evaluations
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.ModelApi" -Dexec.args="list_model_evaluation [model-id]"
```

#### Get Model Evaluation
```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.ModelApi" -Dexec.args="get_model_evaluation [model-id] [model-evaluation-id]"
```

#### Delete Model
```
mvn exec:java-Dexec.mainClass="com.google.cloud.translate.samples.ModelApi" -Dexec.args="delete_model [model-id]"
```
### Predict API

```
mvn exec:java -Dexec.mainClass="com.google.cloud.translate.samples.PredictApi" -Dexec.args="predict [model-id] ./resources/input.txt"
```


