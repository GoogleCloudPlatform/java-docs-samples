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

package com.example.compute.signedmetadata;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.compute.signedmetadata.token.DecodedGoogleJWTWrapper;
import com.example.compute.signedmetadata.token.TokenVerifier;

class VerifyingInstance {

  private String audience;

  VerifyingInstance(String audience) {
    this.audience = audience;
  }

  void verifyToken(String token) {
    TokenVerifier gtv = new TokenVerifier();
    // JWTVerificationException is runtime exception, we don't need to catch it if we want to exit
    // process in case of verification problem. However, to handle verification problems
    // programmatically we can can JWTVerificationException or specific subclass.
    // Following are examples how to handle verification failure.
    try {
      DecodedGoogleJWTWrapper decodedJwt = gtv.verifyWithAudience(audience, token);
      System.out.println("Project id : " + decodedJwt.getProjectId());
      System.out.println("Project number : " + decodedJwt.getProjectNumber());
      // This are examples how to handle exceptions, which indicate verification failure.
    } catch (AlgorithmMismatchException e) {
      // We assume that downloaded certs are RSA256, this exception will happen if this changes.
      throw e;
    } catch (SignatureVerificationException e) {
      // Could not verify signature of a token, possibly someone provided forged token.
      throw e;
    } catch (TokenExpiredException e) {
      // We encountered old token, possibly replay attack.
      throw e;
    } catch (InvalidClaimException e) {
      // Different Audience for token and for verification, possibly token for other verifier.
      throw e;
    } catch (JWTVerificationException e) {
      // Some other problem during verification
      // JWTVerificationException is super-class to:
      //  - SignatureVerificationException
      //  - TokenExpiredException
      //  - InvalidClaimException
      throw e;
    }

  }
}
