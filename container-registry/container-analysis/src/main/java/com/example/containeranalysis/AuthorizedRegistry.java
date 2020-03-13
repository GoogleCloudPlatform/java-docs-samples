package com.example.containeranalysis;

// [START containeranalysis_check_digest]
import com.google.auth.oauth2.AccessToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Vector;

public class AuthorizedRegistry {
  String registry;
  AccessToken token;

  public AuthorizedRegistry(String registry, AccessToken token) {
    // String registry = "gcr.io";
    // AccessToken = GoogleCredentials.getAccessToken();

    this.registry = registry;
    this.token = token;
  }

  String executeRequest(URI uri) throws IOException, InterruptedException {
    // URI uri = URI.create("https://uri/to/access");

    HttpClient client = HttpClient.newHttpClient();

    // Authenticate with the given access token
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .headers("Authorization", String.format("Bearer %s", this.token.getTokenValue()))
            .build();

    HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

    return response.body();
  }

  public List<String> getImages(String repository) throws IOException, InterruptedException {
    // String repository = "[PROJECT-ID]";

    // The resource is available at URL in form of https://gcr.io/v2/my-project/tags/list
    URI uri = URI.create(String.format("https://%s/v2/%s/tags/list", this.registry, repository));
    String response = executeRequest(uri);

    List<String> ret = new Vector<String>();

    // Image name is in response["child"] array
    JsonArray images = JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("child");

    for (JsonElement imageName : images) {
      ret.add(imageName.getAsString());
    }

    return ret;
  }

  public String getDigestForImage(String repository, String image, String tag)
      throws IOException, InterruptedException {
    // String repository = "[PROJECT-ID]";
    // String image = "vulnerabilities-tutorial-image";
    // String tag = "tag1";

    // The resource is available at URL in form of https://gcr.io/v2/my-project/my-image/tags/list
    URI uri =
        URI.create(
            String.format("https://%s/v2/%s/%s/tags/list", this.registry, repository, image));

    String response = executeRequest(uri);

    // Digests are keys in response["manifest"] object.
    JsonObject manifest =
        JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("manifest");

    // However, we are only interested in digests that are tagged as the requested tag
    // in response["manifest"][digest]["tag"] array
    for (Entry<String, JsonElement> e : manifest.entrySet()) {
      for (JsonElement imageTag : e.getValue().getAsJsonObject().getAsJsonArray("tag")) {
        if (imageTag.getAsString().equals(tag)) {
          return e.getKey();
        }
      }
    }

    throw new NoSuchElementException(String.format("%s was not found in image's tags", tag));
  }
}
// [END containeranalysis_check_digest]
