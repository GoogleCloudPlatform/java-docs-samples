package com.example.cloudrun;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RenderController {

  private static final Logger logger = LoggerFactory.getLogger(RenderController.class);

  // Instantiate OkHttpClient
  private static final OkHttpClient ok =
      new OkHttpClient.Builder()
          .readTimeout(500, TimeUnit.MILLISECONDS)
          .writeTimeout(500, TimeUnit.MILLISECONDS)
          .build();

  // render expects a JSON body payload with a 'data' property holding plain text for rendering.
  @PostMapping(value = "/render", consumes = "application/json")
  public String render(@RequestBody Content data) {
    String markdown = data.getData();

    String url = System.getenv("EDITOR_UPSTREAM_RENDER_URL");
    if (url == null) {
      logger.error(
          "No configuration for upstream render service: add EDITOR_UPSTREAM_RENDER_URL environment variable");
      throw new IllegalStateException();
    }

    String html = makeAuthenticatedRequest(url, markdown);
    return html;
  }

  public String makeAuthenticatedRequest(String url, String markdown) {
    Request.Builder serviceRequest = new Request.Builder().url(url);
    Boolean unauthenticated = Boolean.valueOf(System.getenv("EDITOR_UPSTREAM_UNAUTHENTICATED"));
    if (!unauthenticated) {
      // Set up metadata server request
      // https://cloud.google.com/compute/docs/instances/verifying-instance-identity#request_signature
      String tokenUrl =
          String.format("http://metadata/computeMetadata/v1/instance/service-accounts/default/identity?audience=%s", url);
      Request tokenRequest =
          new Request.Builder().url(tokenUrl).addHeader("Metadata-Flavor", "Google").get().build();

      try {
        // Fetch the token
        Response tokenResponse = ok.newCall(tokenRequest).execute();
        String token = tokenResponse.body().string();
        // Provide the token in the request to the receiving service
        serviceRequest.addHeader("Authorization", "Bearer " + token);
      } catch (IOException e) {
        logger.error("Unable to get token");
      }
    }
    MediaType contentType = MediaType.get("text/plain; charset=utf-8");
    okhttp3.RequestBody body = okhttp3.RequestBody.create(markdown, contentType);
    String response = "";
    try {
      Response serviceResponse = ok.newCall(serviceRequest.post(body).build()).execute();
      response = serviceResponse.body().string();
    } catch (IOException e) {
      logger.error("Unable to get rendered data");
    }

    return response;
  }

  public static class Content {
    private String data;

    public String getData() {
      return data;
    }
  }
}
