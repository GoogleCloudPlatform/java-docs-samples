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


import java.net.URL;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class JWTClient {


// [START endpoints_generate_jwt_sa]
  /*
   * Generates a signed JSON Web Token using a Google API Service Account
   * utilizes com.auth0.jwt
   */
  public static String generateJWT(String saKeyfile, String saEmail, String audience,
      int expiryLength) throws FileNotFoundException, IOException {

    Date now = new Date();
    Date expiration = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiryLength));

    // build jwt
    JWTCreator.Builder token = JWT.create()
                                  .withIssuedAt(now)
                                  // expires after 'expirary_length' seconds.
                                  .withExpiresAt(expiration)
                                  // must match 'issuer' in the security configuration in your
                                  // swagger spec (e.g. service account email). It can be any string
                                  .withIssuer(saEmail)
                                  // must be either your Endpoints service name, or match the value
                                  // specified as the 'x-google-audience' in the OpenAPI document.
                                  .withAudience(audience)
                                  // subject and email should match the service account's email
                                  .withSubject(saEmail)
                                  .withClaim("email", saEmail);

    // sign jwt
    GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(saKeyfile));
    RSAPrivateKey privateKey = (RSAPrivateKey) credential.getServiceAccountPrivateKey();
    Algorithm algorithm = Algorithm.RSA256(null, privateKey);
    return token.sign(algorithm);
  }
// [END endpoints_generate_jwt_sa]


// [START endpoints_jwt_request]
  /*
   * Makes an authorized request to the endpoint
   */
  public static String makeJWTRequest(String singedJWT, URL url) throws IOException, ProtocolException{
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");
    con.setRequestProperty("Authorization", "Bearer " + singedJWT);

    BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String line;
    StringBuilder result = new StringBuilder();
    while ((line = rd.readLine()) != null) {
       result.append(line);
    }
    rd.close();
    return result.toString();
  }
// [END endpoints_jwt_request]

  public static void main(String[] args) throws Exception {
    String keyPath = args[0];
    URL host = new URL(args[1]);
    String audience = args[2];
    String saEmail = args[3];

    String jwt = generateJWT(keyPath, saEmail, audience, 3600);
    System.out.println(jwt);

    String response = makeJWTRequest(jwt, host);
    System.out.println(response);
  }

}
