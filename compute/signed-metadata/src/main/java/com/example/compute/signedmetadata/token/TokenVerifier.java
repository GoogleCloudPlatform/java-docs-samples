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

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

public class TokenVerifier {

  private Algorithm algorithm = Algorithm.RSA256(new GoogleRSAKeyProvider());

  public DecodedGoogleJWTWrapper verifyWithAudience(String audience, String token) {
    JWTVerifier verifier = JWT.require(algorithm)
        .withAudience(audience)
        .build();
    return new DecodedGoogleJWTWrapper(verifier.verify(token));
  }
}
