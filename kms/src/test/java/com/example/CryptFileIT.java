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

package com.example;

import static com.google.common.truth.Truth.assertThat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link Quickstart}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class CryptFileIT {

  @BeforeClass
  public static void setUpClass() throws Exception {
    SnippetsIT.setUpClass();
  }

  /**
   * Destroys all the keys created during this test run.
   */
  @AfterClass
  public static void tearDownClass() throws Exception {
    SnippetsIT.tearDownClass();
  }

  @Before
  public void setUp() throws Exception {
    Snippets.createCryptoKeyVersion(
        SnippetsIT.PROJECT_ID, SnippetsIT.LOCATION_ID, SnippetsIT.KEY_RING_ID,
        SnippetsIT.CRYPTO_KEY_ID);
  }

  @Test
  public void encryptDecrypt_encryptsAndDecrypts() throws Exception {
    // Encrypt ENCRYPT_STRING with the current primary version.
    byte[] ciphertext = CryptFile.encrypt(
        SnippetsIT.PROJECT_ID, SnippetsIT.LOCATION_ID, SnippetsIT.KEY_RING_ID,
        SnippetsIT.CRYPTO_KEY_ID, SnippetsIT.ENCRYPT_STRING.getBytes());

    assertThat(new String(ciphertext)).isNotEqualTo(SnippetsIT.ENCRYPT_STRING);

    byte[] plaintext = CryptFile.decrypt(
        SnippetsIT.PROJECT_ID, SnippetsIT.LOCATION_ID, SnippetsIT.KEY_RING_ID,
        SnippetsIT.CRYPTO_KEY_ID, ciphertext);

    assertThat(new String(plaintext)).isEqualTo(SnippetsIT.ENCRYPT_STRING);
  }
}
