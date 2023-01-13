/*
 * Copyright 2018 Google Inc.
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

 package com.example.speech;

 import static com.google.common.truth.Truth.assertThat;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.util.UUID;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;

 import com.google.cloud.ServiceOptions;
 import com.google.cloud.speech.v2.SpeechClient;
 
 /** Tests for quickstart sample. */
 @RunWith(JUnit4.class)
 @SuppressWarnings("checkstyle:abbreviationaswordinname")
 public class QuickstartSampleV2Test {
   private String recognitionAudioFile = "./resources/commercial_mono.wav";
   private String recognizerId = String.format("rec-%s", UUID.randomUUID());
   private String projectId;
   private ByteArrayOutputStream bout;
   private PrintStream out;
 
   @Before
   public void setUp() {
     bout = new ByteArrayOutputStream();
     out = new PrintStream(bout);
     System.setOut(out);
     projectId = ServiceOptions.getDefaultProjectId();
   }
 
   @After
   public void tearDown() {
     System.setOut(null);
   }
 
   @Test
   public void testQuickstart() throws Exception {
     // Act
     QuickstartSampleV2.quickstartSampleV2(projectId, recognitionAudioFile, recognizerId);
 
     // Assert
     String got = bout.toString();
     assertThat(got).contains("Chromecast");
   }
 }
 