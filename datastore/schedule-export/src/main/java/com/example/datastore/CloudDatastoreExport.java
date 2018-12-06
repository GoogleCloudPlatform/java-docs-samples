package com.google.example.datastore;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

@WebServlet(name = "CloudDatastoreExport", value = "/cloud-datastore-export")
public class CloudDatastoreExport extends HttpServlet {

  private static final Logger log = Logger.getLogger(CloudDatastoreExport.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Verify outputURL parameter
    String outputUrlPrefix = request.getParameter("output_url_prefix");

    if (outputUrlPrefix == null || !outputUrlPrefix.matches("^gs://.*")) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      response.setContentType("text/plain");
      response.getWriter().println("Error: Must provide a valid output_url_prefix.");

    } else {

      // Get project ID
      String projectId = ApiProxy.getCurrentEnvironment().getAppId();
      // Remove partition information to get plain app ID
      String appId = projectId.replaceFirst("(.*~)", "");

      // Get access token
      ArrayList<String> scopes = new ArrayList<String>();
      scopes.add("https://www.googleapis.com/auth/datastore");
      final AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
      final AppIdentityService.GetAccessTokenResult accessToken =
          AppIdentityServiceFactory.getAppIdentityService().getAccessToken(scopes);

      // Read export parameters
      // If output prefix does not end with slash, add a timestamp
      if (!outputUrlPrefix.substring(outputUrlPrefix.length() - 1).contentEquals("/")) {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        outputUrlPrefix = outputUrlPrefix + "/" + timeStamp + "/";
      }

      String[] namespaces = request.getParameterValues("namespace_id");
      String[] kinds = request.getParameterValues("kind");

      // Build export request
      JSONObject exportRequest = new JSONObject();
      exportRequest.put("output_url_prefix", outputUrlPrefix);

      JSONObject entityFilter = new JSONObject();

      if (kinds != null) {
        JSONArray kindsJSON = new JSONArray(kinds);
        entityFilter.put("kinds", kinds);
      }

      if (namespaces != null) {
        JSONArray namespacesJSON = new JSONArray(namespaces);
        entityFilter.put("namespaceIds", namespacesJSON);
      }

      exportRequest.put("entityFilter", entityFilter);

      URL url = new URL("https://datastore.googleapis.com/v1/projects/" + appId + ":export");
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.addRequestProperty("Content-Type", "application/json");
      connection.addRequestProperty("Authorization", "Bearer " + accessToken.getAccessToken());

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      exportRequest.write(writer);
      writer.close();

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

        JSONTokener exportResponseTokens = new JSONTokener(connection.getInputStream());
        JSONObject exportResponse = new JSONObject(exportResponseTokens);

        response.setContentType("text/plain");
        response.getWriter().println("Export started:\n" + exportResponse.toString(4));

      } else {
        InputStream s = connection.getErrorStream();
        InputStreamReader r = new InputStreamReader(s, StandardCharsets.UTF_8);
        String errorMessage =
            String.format(
                "got error (%d) response %s from %s",
                connection.getResponseCode(), CharStreams.toString(r), connection.toString());
        log.warning(errorMessage);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("text/plain");
        response.getWriter().println("Failed to initiate export.");
      }
    }
  }
}
