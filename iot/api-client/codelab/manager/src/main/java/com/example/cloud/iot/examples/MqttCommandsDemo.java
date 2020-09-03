/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cloud.iot.examples;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Properties;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MqttCommandsDemo {

  static MqttCallback mCallback;
  static Thread mGUIthread;
  static final String APP_NAME = "MqttCommandsDemo";

  /** Create a Cloud IoT Core JWT for the given project id, signed with the given RSA key. */
  private static String createJwtRsa(String projectId, String privateKeyFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
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

  /** Create a Cloud IoT Core JWT for the given project id, signed with the given ES key. */
  private static String createJwtEs(String projectId, String privateKeyFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
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
    KeyFactory kf = KeyFactory.getInstance("EC");

    return jwtBuilder.signWith(SignatureAlgorithm.ES256, kf.generatePrivate(spec)).compact();
  }

  /** Attaches the callback used when configuration changes occur. */
  public static void attachCallback(MqttClient client, String deviceId, Screen mainScreen)
      throws MqttException {
    mCallback =
        new MqttCallback() {
          private TextColor mainBgColor = new TextColor.ANSI.RGB(255, 255, 255);
          private TextColor myColor;

          @Override
          public void connectionLost(Throwable cause) {
            // Do nothing...
          }

          @Override
          public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = new String(message.getPayload());
            System.out.println("Payload : " + payload);
            // The device will receive its latest config when it subscribes to the
            // config topic. If there is no configuration for the device, the device
            // will receive a config with an empty payload.
            if (payload == null || payload.length() == 0) {
              return;
            }
            if (isJsonValid(payload)) {
              JSONObject data = null;
              data = new JSONObject(payload);

              // [begin command respond code]

              // [end command respond code]
            }
          }

          @Override
          public void deliveryComplete(IMqttDeliveryToken token) {
            // Do nothing;
          }

          /**
           * Get the color from a string name
           *
           * @param col name of the color
           * @return White if no color is given, otherwise the Color object
           */
          TextColor getColor(String col) {
            switch (col.toLowerCase()) {
              case "black":
                myColor = TextColor.ANSI.BLACK;
                break;
              case "blue":
                myColor = TextColor.ANSI.BLUE;
                break;
              case "cyan":
                myColor = TextColor.ANSI.CYAN;
                break;
              case "green":
                myColor = TextColor.ANSI.GREEN;
                break;
              case "yellow":
                myColor = TextColor.ANSI.YELLOW;
                break;
              case "magneta":
                myColor = TextColor.ANSI.MAGENTA;
                break;
              case "red":
                myColor = TextColor.ANSI.RED;
                break;
              case "white":
                myColor = TextColor.ANSI.WHITE;
                break;
              default:
                myColor = TextColor.ANSI.BLACK;
                break;
            }
            return myColor;
          }

          public boolean isValidColor(JSONObject data, TextColor mainBgColor) throws JSONException {
            return data.get("color") instanceof String
                && !mainBgColor.toColor().equals(getColor((String) data.get("color")));
          }
        };

    // [begin code section]

    // [end code section]

    String configTopic = String.format("/devices/%s/config", deviceId);
    System.out.println(String.format("Listening on %s", configTopic));

    client.subscribe(configTopic, 1);
    client.setCallback(mCallback);
  }

  public static void mqttDeviceDemo(
      String projectId,
      String cloudRegion,
      String registryId,
      String deviceId,
      String privateKeyFile,
      String algorithm,
      String mqttBridgeHostname,
      short mqttBridgePort,
      String messageType,
      int waitTime)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, MqttException,
          InterruptedException {

    // Build the connection string for Google's Cloud IoT Core MQTT server. Only SSL
    // connections are accepted. For server authentication, the JVM's root certificates
    // are used.
    final String mqttServerAddress =
        String.format("ssl://%s:%s", mqttBridgeHostname, mqttBridgePort);

    // Create our MQTT client. The mqttClientId is a unique string that identifies this device. For
    // Google Cloud IoT Core, it must be in the format below.
    final String mqttClientId =
        String.format(
            "projects/%s/locations/%s/registries/%s/devices/%s",
            projectId, cloudRegion, registryId, deviceId);

    MqttConnectOptions connectOptions = new MqttConnectOptions();
    // Note that the Google Cloud IoT Core only supports MQTT 3.1.1, and Paho requires that we
    // explictly set this. If you don't set MQTT version, the server will immediately close its
    // connection to your device.
    connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

    Properties sslProps = new Properties();
    sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
    connectOptions.setSSLProperties(sslProps);

    // With Google Cloud IoT Core, the username field is ignored, however it must be set for the
    // Paho client library to send the password field. The password field is used to transmit a JWT
    // to authorize the device.
    connectOptions.setUserName("unused");

    DateTime iat = new DateTime();
    if (algorithm.equals("RS256")) {
      connectOptions.setPassword(createJwtRsa(projectId, privateKeyFile).toCharArray());
    } else if (algorithm.equals("ES256")) {
      connectOptions.setPassword(createJwtEs(projectId, privateKeyFile).toCharArray());
    } else {
      throw new IllegalArgumentException(
          "Invalid algorithm " + algorithm + ". Should be one of 'RS256' or 'ES256'.");
    }

    // [START iot_mqtt_publish]
    // Create a client, and connect to the Google MQTT bridge.
    MqttClient client = new MqttClient(mqttServerAddress, mqttClientId, new MemoryPersistence());

    // Both connect and publish operations may fail. If they do, allow retries but with an
    // exponential backoff time period.
    long initialConnectIntervalMillis = 500L;
    long maxConnectIntervalMillis = 6000L;
    long maxConnectRetryTimeElapsedMillis = 900000L;
    float intervalMultiplier = 1.5f;

    long retryIntervalMs = initialConnectIntervalMillis;
    long totalRetryTimeMs = 0;

    while (!client.isConnected() && totalRetryTimeMs < maxConnectRetryTimeElapsedMillis) {
      try {
        client.connect(connectOptions);
      } catch (MqttException e) {
        int reason = e.getReasonCode();

        // If the connection is lost or if the server cannot be connected, allow retries, but with
        // exponential backoff.
        System.out.println("An error occurred: " + e.getMessage());
        if (reason == MqttException.REASON_CODE_CONNECTION_LOST
            || reason == MqttException.REASON_CODE_SERVER_CONNECT_ERROR) {
          System.out.println("Retrying in " + retryIntervalMs / 1000.0 + " seconds.");
          Thread.sleep(retryIntervalMs);
          totalRetryTimeMs += retryIntervalMs;
          retryIntervalMs *= intervalMultiplier;
          if (retryIntervalMs > maxConnectIntervalMillis) {
            retryIntervalMs = maxConnectIntervalMillis;
          }
        } else {
          throw e;
        }
      }
    }

    // Publish to the events or state topic based on the flag.
    String subTopic = messageType.equals("event") ? "events" : messageType;

    // The MQTT topic that this device will publish telemetry data to. The MQTT topic name is
    // required to be in the format below. Note that this is not the same as the device registry's
    // Cloud Pub/Sub topic.
    String mqttTopic = String.format("/devices/%s/%s", deviceId, subTopic);

    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    Screen screen = null;
    Terminal terminal = defaultTerminalFactory.createTerminal();
    screen = new TerminalScreen(terminal);

    attachCallback(client, deviceId, screen);

    // Wait for commands to arrive for about two minutes.
    for (int i = 1; i <= waitTime; ++i) {
      System.out.print(".");
      Thread.sleep(1000);
    }
    System.out.println("");

    // Disconnect the client if still connected, and finish the run.
    if (client.isConnected()) {
      client.disconnect();
    }

    System.out.println("Finished loop successfully. Goodbye!");
    client.close();
    System.exit(0);
    // [END iot_mqtt_publish]
  }

  public static void startGui(Screen screen, TextColor theColor) throws IOException {

    try {
      /*
      You can use the DefaultTerminalFactory to create a Screen, this will generally give you the
      TerminalScreen implementation that is probably what you want to use. Please see
      VirtualScreen for more details on a separate implementation that allows you to create a
      terminal surface that is bigger than the physical size of the terminal emulator the software
      is running in. Just to demonstrate that a Screen sits on top of a Terminal,
      we are going to create one manually instead of using DefaultTerminalFactory.
       */

      /*
      Screens will only work in private mode and while you can call methods to mutate its state,
      before you can make any of these changes visible, you'll need to call startScreen()
      which will prepare and setup the terminal.
       */
      screen.startScreen();
      System.out.println("Starting the terminal...");
      /*
      Let's turn off the cursor for this tutorial
       */
      screen.setCursorPosition(null);

      /*
      Now let's draw some random content in the screen buffer
       */

      TerminalSize terminalSize = screen.getTerminalSize();
      for (int column = 0; column < terminalSize.getColumns(); column++) {
        for (int row = 0; row < terminalSize.getRows(); row++) {
          screen.setCharacter(
              column,
              row,
              new TextCharacter(
                  ' ',
                  TextColor.ANSI.DEFAULT,
                  // This will pick a random background color
                  theColor));
        }
      }

      /*
      So at this point, we've only modified the back buffer in the screen, nothing is visible yet.
      In order to move the content from the back buffer to the front buffer and refresh the
      screen, we need to call refresh()
       */
      screen.refresh();
      System.out.println("Starting the terminal...");
      /*
      Ok, now we loop and keep modifying the screen until the user exits by pressing escape on
      the keyboard or the input stream is closed. When using the Swing/AWT bundled emulator,
      if the user closes the window this will result in an EOF KeyStroke.
       */
      while (true) {
        KeyStroke keyStroke = screen.pollInput();
        if (keyStroke != null
            && (keyStroke.getKeyType() == KeyType.Escape
                || keyStroke.getKeyType() == KeyType.EOF)) {
          break;
        }

        /*
        Screens will automatically listen and record size changes, but you have to let the Screen
        know when is a good time to update its internal buffers. Usually you should do this at the
        start of your "drawing" loop, if you have one. This ensures that the dimensions of the
        buffers stays constant and doesn't change while you are drawing content. The method
        doReizeIfNecessary() will check if the terminal has been resized since last time it
        was called (or since the screen was created if this is the first time calling) and
        update the buffer dimensions accordingly. It returns null if the terminal
        has not changed size since last time.
         */
        TerminalSize newSize = screen.doResizeIfNecessary();
        if (newSize != null) {
          terminalSize = newSize;
        }
        /*
        Just like with Terminal, it's probably easier to draw using TextGraphics.
        Let's do that to put a little box with information on the size of the terminal window
         */
        String sizeLabel = "Terminal Size: " + terminalSize;
        TerminalPosition labelBoxTopLeft = new TerminalPosition(1, 1);
        TerminalSize labelBoxSize = new TerminalSize(sizeLabel.length() + 2, 3);
        TerminalPosition labelBoxTopRightCorner =
            labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 1);
        TextGraphics textGraphics = screen.newTextGraphics();
        // This isn't really needed as we are overwriting everything below anyway, but just for
        // demonstrative purpose
        textGraphics.fillRectangle(labelBoxTopLeft, labelBoxSize, ' ');

        /*
        Draw horizontal lines, first upper then lower
         */
        textGraphics.drawLine(
            labelBoxTopLeft.withRelativeColumn(1),
            labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 2),
            Symbols.DOUBLE_LINE_HORIZONTAL);
        textGraphics.drawLine(
            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(1),
            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(labelBoxSize.getColumns() - 2),
            Symbols.DOUBLE_LINE_HORIZONTAL);

        /*
        Manually do the edges and (since it's only one) the vertical lines,
        first on the left then on the right
         */
        textGraphics.setCharacter(labelBoxTopLeft, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
        textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
        textGraphics.setCharacter(
            labelBoxTopLeft.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
        textGraphics.setCharacter(labelBoxTopRightCorner, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
        textGraphics.setCharacter(
            labelBoxTopRightCorner.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
        textGraphics.setCharacter(
            labelBoxTopRightCorner.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);

        /*
        Finally put the text inside the box
         */
        textGraphics.putString(labelBoxTopLeft.withRelative(1, 1), sizeLabel);

        /*
        Ok, we are done and can display the change. Let's also be nice and allow the OS
        to schedule other threads so we don't clog up the core completely.
         */
        screen.refresh();
        Thread.yield();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (screen != null) {
        try {
          /*
          The close() call here will restore the terminal by exiting from private mode which
          was done in the call to startScreen()
           */
          screen.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    screen.stopScreen();
  }

  public static boolean isJsonValid(String data) {
    try {
      new JSONObject(data);
    } catch (JSONException ex) {
      // edited, to include @Arthur's comment
      // e.g. in case JSONArray is valid as well...
      try {
        new JSONArray(data);
      } catch (JSONException ex1) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) throws Exception {
    MqttExampleOptions options = MqttExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      System.exit(1);
    }
    System.out.println("Starting mqtt demo:");
    mqttDeviceDemo(
        options.projectId,
        options.cloudRegion,
        options.registryId,
        options.deviceId,
        options.privateKeyFile,
        options.algorithm,
        options.mqttBridgeHostname,
        options.mqttBridgePort,
        "state",
        options.waitTime);
  }
}
