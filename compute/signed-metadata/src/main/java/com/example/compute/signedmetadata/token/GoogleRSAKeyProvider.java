/*
 * Copyright 2017 Google Inc.
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

package com.example.compute.signedmetadata.token;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

//CHECKSTYLE OFF: AbbreviationAsWordInName
class GoogleRSAKeyProvider implements RSAKeyProvider {
  //CHECKSTYLE ON: AbbreviationAsWordInName

  private static final String GOOGLEAPIS_CERTS = "https://www.googleapis.com/oauth2/v1/certs";

  private final Supplier<Map<String, String>> cachedSignedCertificates
      = Suppliers.memoizeWithExpiration(this::getNewCertificate,1, TimeUnit.HOURS);

  @SuppressWarnings("unchecked")
  private Map<String, String> getNewCertificate() {
    Gson gson = new Gson();
    String result;
    try {
      result = new Downloader().download(GOOGLEAPIS_CERTS);
    } catch (IOException e) {
      throw new JWTVerificationException("Could not download public Googleapis certs.",e);
    }
    return (Map<String, String>) gson.fromJson(result, HashMap.class);
  }

  @Override
  public RSAPublicKey getPublicKeyById(String kid) {
    // Received 'kid' value might be null if it wasn't defined in the Token's header
    if (kid == null) {
      throw new JWTVerificationException(
          "Cannot verify without kid, we need to know which certificate should we use.");
    }
    String certificate = cachedSignedCertificates.get().get(kid);
    return transformPemCertificateToRsaKey(certificate);
  }

  @Override
  public RSAPrivateKey getPrivateKey() {
    throw new UnsupportedOperationException("This class is used to decode certificates only.");
  }

  @Override
  public String getPrivateKeyId() {
    throw new UnsupportedOperationException("This class is used to decode certificates only.");
  }

  private RSAPublicKey transformPemCertificateToRsaKey(String cert) {
    try {
      InputStream is = new ByteArrayInputStream(cert.getBytes());
      Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(is);
      is.close();
      return safelyCastToRsaPublicKey(certificate.getPublicKey());
    } catch (CertificateException e) {
      throw new JWTVerificationException("Could not extract RSA key from certificate String.",e);
    } catch (IOException e) {
      //Thrown when closing input stream. Built on in-memory array. From immutable String.
      throw new RuntimeException(e);
    }
  }

  private RSAPublicKey safelyCastToRsaPublicKey(PublicKey publicKey) {
    if (publicKey instanceof RSAPublicKey) {
      return (RSAPublicKey) publicKey;
    } else {
      throw new AlgorithmMismatchException("We expected RSAPublicKey from certificate");
    }
  }
}
