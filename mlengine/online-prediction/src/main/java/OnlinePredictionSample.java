/*
 * Sample code for doing Cloud Machine Learning Engine online prediction in Java.
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.discovery.Discovery;
import com.google.api.services.discovery.model.JsonSchema;
import com.google.api.services.discovery.model.RestDescription;
import com.google.api.services.discovery.model.RestMethod;
import java.io.File;

public class OnlinePredictionSample {
  public static void main(String[] args) throws Exception {
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    Discovery discovery = new Discovery.Builder(httpTransport, jsonFactory, null).build();

    RestDescription api = discovery.apis().getRest("ml", "v1").execute();
    RestMethod method = api.getResources().get("projects").getMethods().get("predict");

    JsonSchema param = new JsonSchema();
    String projectId = "YOUR_PROJECT_ID";
    String modelId = "YOUR_MODEL_ID";
    String versionId = "YOUR_VERSION_ID";
    param.set(
        "name", String.format("projects/%s/models/%s/versions/%s", projectId, modelId, versionId));

    GenericUrl url =
        new GenericUrl(UriTemplate.expand(api.getBaseUrl() + method.getPath(), param, true));
    System.out.println(url);

    String contentType = "application/json";
    File requestBodyFile = new File("input.txt");
    HttpContent content = new FileContent(contentType, requestBodyFile);
    System.out.println(content.getLength());

    GoogleCredential credential = GoogleCredential.getApplicationDefault();
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildRequest(method.getHttpMethod(), url, content);

    String response = request.execute().parseAsString();
    System.out.println(response);
  }
}

