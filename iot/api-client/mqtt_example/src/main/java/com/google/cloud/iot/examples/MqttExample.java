package com.google.cloud.iot.examples;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.joda.time.DateTime;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Java sample of connecting to Google Cloud IoT Core vice via MQTT, using JWT.
 *
 * <p>This example connects to Google Cloud IoT Core via MQTT, using a JWT for device
 * authentication. After connecting, by default the device publishes 100 messages to
 * the device's MQTT topic at a rate of one per second, and then exits.
 *
 * <p>To run this example, first create your credentials and register your device as
 * described in the README located in the sample's parent folder.
 *
 * <p>After you have registered your device and generated your credentials, compile and
 * run with the corresponding algorithm flag, for example:
 *
 * <pre>
 *   $ mvn compile
 *   $ mvn exec:java -Dexec.mainClass="com.google.cloud.iot.examples.MqttExample" \
 *       -Dexec.args="-project_id=my-project-id \
 *       -registry_id=my-registry-id \
 *       -device_id=my-device-id \
 *       -private_key_file=/path/to/private_pkcs8 \
 *       -algorithm=RS256"
 * </pre>
 */
public class MqttExample {
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
    MqttExampleOptions options = MqttExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      System.exit(1);
    }

    // Build the connection string for Google's Cloud IoT Core MQTT server. Only SSL
    // connections are accepted. For server authentication, the JVM's root certificates
    // are used.
    String mqttServerAddress =
        String.format("ssl://%s:%s", options.mqttBridgeHostname, options.mqttBridgePort);

    // Create our MQTT client. The mqttClientId is a unique string that identifies this device. For
    // Google Cloud IoT Core, it must be in the format below.
    String mqttClientId =
        String.format(
            "projects/%s/locations/%s/registries/%s/devices/%s",
            options.projectId, options.cloudRegion, options.registryId, options.deviceId);

    MqttConnectOptions connectOptions = new MqttConnectOptions();
    // Note that the the Google Cloud IoT Core only supports MQTT 3.1.1, and Paho requires that we
    // explictly set this. If you don't set MQTT version, the server will immediately close its
    // connection to your device.
    connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

    // With Google Cloud IoT Core, the username field is ignored, however it must be set for the
    // Paho client library to send the password field. The password field is used to transmit a JWT
    // to authorize the device.
    connectOptions.setUserName("unused");

    System.out.println(options.algorithm);
    if (options.algorithm.equals("RS256")) {
      connectOptions.setPassword(
          createJwtRsa(options.projectId, options.privateKeyFile).toCharArray());
    } else if (options.algorithm.equals("ES256")) {
      connectOptions.setPassword(
          createJwtEs(options.projectId, options.privateKeyFile).toCharArray());
    } else {
      throw new IllegalArgumentException(
          "Invalid algorithm " + options.algorithm + ". Should be one of 'RS256' or 'ES256'.");
    }

    // Create a client, and connect to the Google MQTT bridge.
    MqttClient client = new MqttClient(mqttServerAddress, mqttClientId, new MemoryPersistence());
    client.connect(connectOptions);

    // The MQTT topic that this device will publish telemetry data to. The MQTT topic name is
    // required to be in the format below. Note that this is not the same as the device registry's
    // Cloud Pub/Sub topic.
    String mqttTopic = String.format("/devices/%s/events", options.deviceId);

    // Publish numMessages messages to the MQTT bridge, at a rate of 1 per second.
    for (int i = 1; i <= options.numMessages; ++i) {
      String payload = String.format("%s/%s-payload-%d", options.registryId, options.deviceId, i);
      System.out.format("Publishing message %d/%d: '%s'\n", i, options.numMessages, payload);

      // Publish "payload" to the MQTT topic. qos=1 means at least once delivery. Cloud IoT Core
      // also supports qos=0 for at most once delivery.
      MqttMessage message = new MqttMessage(payload.getBytes());
      message.setQos(1);
      client.publish(mqttTopic, message);
      Thread.sleep(1000);
    }
    // Disconnect the client and finish the run.
    client.disconnect();
    System.out.println("Finished loop successfully. Goodbye!");
  }
}
