package com.google.cloud.iot.examples;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * Java sample of connecting to Google Cloud IoT Core vice via HTTP, using JWT.
 *
 * <p>This example connects to Google Cloud IoT Core via HTTP Bridge, using a JWT for device
 * authentication. After connecting, by default the device publishes 100 messages at a rate of one
 * per second, and then exits. You can change The behavior to set state instead of events by using
 * flag -message_type to 'state'.
 *
 * <p>To run this example, follow the instructions in the README located in the sample's parent
 * folder.
 */
public class HttpExample {
  /** Create a Cloud IoT Core JWT for the given project id, signed with the given private key. */
  private static String createJwtRsa(String projectId, String privateKeyFile) throws Exception {
    DateTime now = new DateTime();
    // Create a JWT to authenticate this device. The device will be disconnected after the token
    // expires, and will have to reconnect with a new token. The audience field should always be set
    // to the GCP project id.
    JwtBuilder jwtBuilder =
        Jwts.builder()
            .setIssuedAt(now.toDate())
            .setExpiration(now.plusMinutes(20).toDate())
            .setAudience(projectId);

    byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");

    return jwtBuilder.signWith(SignatureAlgorithm.RS256, kf.generatePrivate(spec)).compact();
  }

  private static String createJwtEs(String projectId, String privateKeyFile) throws Exception {
    DateTime now = new DateTime();
    // Create a JWT to authenticate this device. The device will be disconnected after the token
    // expires, and will have to reconnect with a new token. The audience field should always be set
    // to the GCP project id.
    JwtBuilder jwtBuilder =
        Jwts.builder()
            .setIssuedAt(now.toDate())
            .setExpiration(now.plusMinutes(20).toDate())
            .setAudience(projectId);

    byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("ES256");

    return jwtBuilder.signWith(SignatureAlgorithm.ES256, kf.generatePrivate(spec)).compact();
  }

  public static void main(String[] args) throws Exception {
    HttpExampleOptions options = HttpExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse the flags.
      System.exit(1);
    }

    // Build the resource path of the device that is going to be authenticated.
    String devicePath =
        String.format(
            "projects/%s/locations/%s/registries/%s/devices/%s",
            options.projectId, options.cloudRegion, options.registryId, options.deviceId);

    // This describes the operation that is going to be perform with the device.
    String urlSuffix = options.messageType.equals("event") ? "publishEvent" : "setState";

    String urlPath =
        String.format(
            "%s/%s/%s:%s", options.httpBridgeAddress, options.apiVersion, devicePath, urlSuffix);
    URL url = new URL(urlPath);
    System.out.format("Using URL: '%s'\n", urlPath);

    // Create the corresponding JWT depending on the selected algorithm.
    String token;
    if (options.algorithm.equals("RS256")) {
      token = createJwtRsa(options.projectId, options.privateKeyFile);
    } else if (options.algorithm.equals("ES256")) {
      token = createJwtEs(options.projectId, options.privateKeyFile);
    } else {
      throw new IllegalArgumentException(
          "Invalid algorithm " + options.algorithm + ". Should be one of 'RS256' or 'ES256'.");
    }

    // Data sent through the wire has to be base64 encoded.
    Base64.Encoder encoder = Base64.getEncoder();

    // Publish numMessages messages to the HTTP bridge.
    for (int i = 1; i <= options.numMessages; ++i) {
      String payload = String.format("%s/%s-payload-%d", options.registryId, options.deviceId, i);
      System.out.format(
          "Publishing %s message %d/%d: '%s'\n",
          options.messageType, i, options.numMessages, payload);
      String encPayload = encoder.encodeToString(payload.getBytes("UTF-8"));

      HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
      httpCon.setDoOutput(true);
      httpCon.setRequestMethod("POST");

      // Adding headers.
      httpCon.setRequestProperty("Authorization", String.format("Bearer %s", token));
      httpCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

      // Adding the post data. The structure of the data send depends on whether it is event or a
      // state message.
      JSONObject data = new JSONObject();
      if (options.messageType.equals("event")) {
        data.put("binary_data", encPayload);
      } else {
        JSONObject state = new JSONObject();
        state.put("binary_data", encPayload);
        data.put("state", state);
      }
      httpCon.getOutputStream().write(data.toString().getBytes("UTF-8"));
      httpCon.getOutputStream().close();

      // This will perform the connection as well.
      System.out.println(httpCon.getResponseCode());
      System.out.println(httpCon.getResponseMessage());

      // Send events every second; states, every 5.
      Thread.sleep(options.messageType.equals("event") ? 1000 : 5000);
    }
    System.out.println("Finished loop successfully. Goodbye!");
  }
}
