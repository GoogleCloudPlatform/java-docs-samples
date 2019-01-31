package com.example.app;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.io.FileInputStream;
import java.security.interfaces.RSAPrivateKey;


import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;


import java.io.FileNotFoundException;
import java.io.IOException;

public class Sample {


// [START endpoints_generate_jwt_sa]
  public static String generateJWT(String saKeyfile, String saEmail, String audience,
      int expiryLength) throws FileNotFoundException, IOException {

    Date now = new Date();
    Date expiration = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiryLength));

    JWTCreator.Builder token = JWT.create()
                                  .withIssuedAt(now)
                                  .withExpiresAt(expiration)
                                  .withIssuer(saEmail)
                                  .withAudience(audience)
                                  .withSubject(saEmail)
                                  .withClaim("email", saEmail);

    // sign jwt
    GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(saKeyfile));
    RSAPrivateKey privateKey = (RSAPrivateKey) credential.getServiceAccountPrivateKey();
    Algorithm algorithm = Algorithm.RSA256(null, privateKey);
    return token.sign(algorithm);
  }
// [END endpoints_generate_jwt_sa]

  public static void main(String[] args) throws Exception {
    String jwt = generateJWT("/app/key.json", "iss", "sub", 100);
    System.out.println(jwt);
  }

}
