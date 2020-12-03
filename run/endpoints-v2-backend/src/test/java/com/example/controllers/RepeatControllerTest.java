/*
 * Copyright 2020 Google LLC
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

package com.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.endpoints.controllers.RepeatController;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class RepeatControllerTest {

  private RepeatController repeatController;

  @Before
  public void setUp() {
    this.repeatController = new RepeatController();
  }

  @Test
  public void testRepeat_oneTime_shouldReturnExpected() {
    //Given
    String text = "Hello World";
    int times = 1;
    String expected = "Hello World!";

    //When
    ResponseEntity<String> actual = this.repeatController.repeat(text, times);

    //Then
    assertEquals(expected, actual.getBody());
  }

  @Test
  public void testRepeat_multipleTimes_shouldReturnExpected() {
    //Given
    String text = "Hello World";
    int times = 3;
    String expected = "Hello World, Hello World, Hello World!";

    //When
    ResponseEntity<String> actual = this.repeatController.repeat(text, times);

    //Then
    assertEquals(expected, actual.getBody());
  }
}
