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

import com.google.pubsub.v1.Topic;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for iot "Management" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ManagerIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private MqttCommandsDemo app;

  @Before
  public void setUp() throws Exception {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(null);
  }

  @Test
  public void testTerminal() throws Exception {
    // Set up
    Screen screen = null;
    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    try (Terminal terminal = defaultTerminalFactory.createTerminal()) {
      screen = new TerminalScreen(terminal);

      Screen finalScreen = screen;
      Thread deviceThread =
          new Thread(
              () -> {
                try {
                  MqttCommandsDemo.startGui(finalScreen, new TextColor.RGB(255, 255, 255));
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });

      deviceThread.join(3000);
      System.out.println(terminal.getTerminalSize().toString());
      // Assertions
      Assert.assertTrue(terminal.getTerminalSize().toString().contains("x"));
      Assert.assertTrue(terminal.getTerminalSize().toString().contains("{"));
      Assert.assertTrue(terminal.getTerminalSize().toString().contains("}"));
    } catch (FileNotFoundException e) {
      // Don't fail when GUI not available: "/dev/tty (No such device or address)"
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void testJsonValid() {
    // Assertions
    Assert.assertTrue(MqttCommandsDemo.isJsonValid("{test:true}"));
    Assert.assertFalse(MqttCommandsDemo.isJsonValid("{test:false"));
  }
}
