/*
 * Copyright 2016 Google Inc.
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

package com.example.appengine.appidentity;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.PublicCertificate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SignForAppServlet extends HttpServlet {
  private final AppIdentityService appIdentity;

  public SignForAppServlet() {
    appIdentity = AppIdentityServiceFactory.getAppIdentityService();
  }

  // [START gae_java_app_identity_other_services]
  // Note that the algorithm used by AppIdentity.signForApp() and
  // getPublicCertificatesForApp() is "SHA256withRSA"

  private byte[] signBlob(byte[] blob) {
    AppIdentityService.SigningResult result = appIdentity.signForApp(blob);
    return result.getSignature();
  }

  private byte[] getPublicCertificate() throws UnsupportedEncodingException {
    Collection<PublicCertificate> certs = appIdentity.getPublicCertificatesForApp();
    PublicCertificate publicCert = certs.iterator().next();
    return publicCert.getX509CertificateInPemFormat().getBytes("UTF-8");
  }

  private Certificate parsePublicCertificate(byte[] publicCert)
      throws CertificateException, NoSuchAlgorithmException {
    InputStream stream = new ByteArrayInputStream(publicCert);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return cf.generateCertificate(stream);
  }

  private boolean verifySignature(byte[] blob, byte[] blobSignature, PublicKey pk)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initVerify(pk);
    signature.update(blob);
    return signature.verify(blobSignature);
  }

  private String simulateIdentityAssertion()
      throws CertificateException, UnsupportedEncodingException, NoSuchAlgorithmException,
             InvalidKeyException, SignatureException {
    // Simulate the sending app.
    String message = "abcdefg";
    byte[] blob = message.getBytes();
    byte[] blobSignature = signBlob(blob);
    byte[] publicCert = getPublicCertificate();

    // Simulate the receiving app, which gets the certificate, blob, and signature.
    Certificate cert = parsePublicCertificate(publicCert);
    PublicKey pk = cert.getPublicKey();
    boolean isValid = verifySignature(blob, blobSignature, pk);

    return String.format(
        "isValid=%b for message: %s\n\tsignature: %s\n\tpublic cert: %s",
        isValid,
        message,
        Arrays.toString(blobSignature),
        Arrays.toString(publicCert));
  }
  // [END gae_java_app_identity_other_services]

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    try {
      resp.getWriter().println(simulateIdentityAssertion());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
