/*
 *  Copyright 2023 Google LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package token;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.util.encoders.Hex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import token.DualToken.Header;

@RunWith(JUnit4.class)
public class DualTokenIT {

  private static Instant START_TIME;
  private static Instant EXPIRES_TIME;
  private static String SESSION_ID;
  private static String DATA;
  private static String IP_RANGES;
  private static List<Header> HEADERS;
  private static final Optional<String> EMPTY_STR = Optional.empty();
  private static final Optional<Instant> EMPTY_INSTANT = Optional.empty();
  private static final Optional<List<Header>> EMPTY_HEADER = Optional.empty();

  private ByteArrayOutputStream stdOut;
  private static final PrintStream OUT = System.out;

  @BeforeClass
  public static void setUp() {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
    String startTimeString = "2022-09-13T00:00:00Z";
    START_TIME = Instant.from(formatter.parse(startTimeString));

    String expiresTimeString = "2022-09-13T12:00:00Z";
    EXPIRES_TIME = Instant.from(formatter.parse(expiresTimeString));

    SESSION_ID = "test-id";
    DATA = "test-data";
    IP_RANGES = "203.0.113.0/24,2001:db8:4a7f:a732/64";

    HEADERS = new ArrayList<>();
    HEADERS.add(new Header("Foo", "bar"));
    HEADERS.add(new Header("BAZ", "quux"));

    try (FileOutputStream exampleKeyFos = new FileOutputStream("/tmp/example.key");
        FileOutputStream publicKeyFos = new FileOutputStream("/tmp/example.pub");
        FileOutputStream sharedSecretFos = new FileOutputStream("/tmp/shared.secret")) {
      String exampleHexString =
          "0c951c9cb82e5452a6542177586b9b1b531983b7d6027c5a70c8ca0e155930629fb9f0be1cda"
              + "d750b44ae52d6b6e5a30d27f31fe099201817c6a23f98977d4";
      byte[] byteArray = Hex.decode(exampleHexString);
      exampleKeyFos.write(byteArray);

      String publicHexString =
          "9fb9f0be1cdaD750b44ae55d2d6e6e5a30d27f31fe0a9201817c6a233f9877d4";
      byteArray = Hex.decode(publicHexString);
      publicKeyFos.write(byteArray);

      String sharedSecretString =
          "83f4a53082e22162aab02e99d8bee0cb4b117833aab52ac9ac4ec25cdaef9365";
      byteArray = Hex.decode(sharedSecretString);
      sharedSecretFos.write(byteArray);

    } catch (IOException e) {
      throw new Error("IOException: Unable to write key(s)\n" + e);
    }
  }

  @AfterClass
  public static void cleanup() {
    System.setOut(OUT);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  public void testSignTokenForEd25519UrlPrefix()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "URLPrefix=aHR0cDovLzEwLjIwLjMwLjQwLw~Expires=1663070400~Signature"
        + "=OQLXEjnApFGJaGZ_jvp2R7VY5q3ic-HT3igFpi9iPsJRXtQuvPF4cxZUT-rtCqzteXx3vSRhk09FxgDQauO_DA";
    DualToken.signToken(
        "DJUcnLguVFKmVCFnWGubG1MZg7fWAnxacMjKDhVZMGI=".getBytes(),
        "ed25519",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        Optional.of("http://10.20.30.40/"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }


  @Test
  public void testSignTokenForEd25519PathGlob()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "PathGlobs=/*~Expires=1663070400~Signature=9pBdD_6O6LB-4V67HZ_SO"
        + "c2G_jIkSZ_tMsKnVqElmPlwKB_xDiW7DKAnv8L8CmweeZquaLFlnLogbMcIV8bNCQ";
    DualToken.signToken(
        "DJUcnLguVFKmVCFnWGubG1MZg7fWAnxacMjKDhVZMGI=".getBytes(),
        "ed25519",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        EMPTY_STR,
        EMPTY_STR,
        Optional.of("/*"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForEd25519FullPath()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "FullPath~Expires=1663070400~Signature=X74OTNjtseIUmsab-YiOTZ8jy"
        + "X_KG7v4YQWwcFpfFmjhzaX8NdweMc9Wglj8wxEsEW85g3_MBG3T9jzLZFQDCw";
    DualToken.signToken(
        "DJUcnLguVFKmVCFnWGubG1MZg7fWAnxacMjKDhVZMGI=".getBytes(),
        "ed25519",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        EMPTY_STR,
        Optional.of("/example.m3u8"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }


  @Test
  public void testSignTokenForSha1UrlPrefix()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "URLPrefix=aHR0cDovLzEwLjIwLjMwLjQwLw~Expires=1663070400~hmac=6f"
        + "5b4bb82536810d5ee111cba3e534d49c6ac3cb";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha1",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        Optional.of("http://10.20.30.40/"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha1PathGlob()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "PathGlobs=/*~Expires=1663070400~hmac=c1c446eea24faa31392519f97"
        + "5fea7eefb945625";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha1",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        EMPTY_STR,
        EMPTY_STR,
        Optional.of("/*"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha1FullPath()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "FullPath~Expires=1663070400~hmac=7af78177d6bc001d5626eefe387b"
        + "1774a4a99ca2";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha1",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        EMPTY_STR,
        Optional.of("/example.m3u8"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha256UrlPrefix()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "URLPrefix=aHR0cDovLzEwLjIwLjMwLjQwLw~Expires=1663070400~hmac=40"
        + "9722313cf6d987da44bb360e60dccc3d79764520fc5e3b57654e1d4d2c862e";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha256",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        Optional.of("http://10.20.30.40/"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha256PathGlob()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "PathGlobs=/*~Expires=1663070400~hmac=9439ecdd5c4919f76f915dea72a"
        + "fa85a045579794e63d8cda664f5a1140c8d93";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha256",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        EMPTY_STR,
        EMPTY_STR,
        Optional.of("/*"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha256FullPath()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "FullPath~Expires=1663070400~hmac=365b41fd77297371d890fc9a56e4e3d3b"
        + "aa4c7afbd230a0e9a81c8e1bcab9420";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha256",
        EMPTY_INSTANT,
        EXPIRES_TIME,
        EMPTY_STR,
        Optional.of("/example.m3u8"),
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_STR,
        EMPTY_HEADER,
        EMPTY_STR
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForEd25519AllParams()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "PathGlobs=/*~Starts=1663027200~Expires=1663070400~SessionID=test-id"
        + "~Data=test-data~Headers=Foo,BAZ~IPRanges=MjAzLjAuMTEzLjAvMjQsMjAwMTpkYjg6NGE3Zj"
        + "phNzMyLzY0~Signature=A7u67hveGxGvP8KBWZlUuH0IsqhS4a2lcsXwy3uc4X3zaVuw7LY-2FQT1Z"
        + "F8UxkSFAsDS3_0LYnXwXB2XdepDg";
    DualToken.signToken(
        "DJUcnLguVFKmVCFnWGubG1MZg7fWAnxacMjKDhVZMGI=".getBytes(),
        "ed25519",
        Optional.of(START_TIME),
        EXPIRES_TIME,
        EMPTY_STR,
        EMPTY_STR,
        Optional.of("/*"),
        Optional.of(SESSION_ID),
        Optional.of(DATA),
        Optional.of(HEADERS),
        Optional.of(IP_RANGES)
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha1AllParams()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "PathGlobs=/*~Starts=1663027200~Expires=1663070400~SessionID=test-id"
        + "~Data=test-data~Headers=Foo,BAZ~IPRanges=MjAzLjAuMTEzLjAvMjQsMjAwMTpkYjg6NGE3Zj"
        + "phNzMyLzY0~hmac=b8242e8b76cbfbbd61b3540ed0eb60a2ec2fdbdb";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha1",
        Optional.of(START_TIME),
        EXPIRES_TIME,
        EMPTY_STR,
        EMPTY_STR,
        Optional.of("/*"),
        Optional.of(SESSION_ID),
        Optional.of(DATA),
        Optional.of(HEADERS),
        Optional.of(IP_RANGES)
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

  @Test
  public void testSignTokenForSha256AllParams()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String expected = "PathGlobs=/*~Starts=1663027200~Expires=1663070400~SessionID=test-id"
        + "~Data=test-data~Headers=Foo,BAZ~IPRanges=MjAzLjAuMTEzLjAvMjQsMjAwMTpkYjg6NGE3Zj"
        + "phNzMyLzY0~hmac=dda9c3d6f3b2e867a09fbb76209ea138dd81f8512210f970d1e92f90927bef4b";
    DualToken.signToken(
        "g_SlMILiIWKqsC6Z2L7gy0sReDOqtSrJrE7CXNr5Nl8=".getBytes(),
        "sha256",
        Optional.of(START_TIME),
        EXPIRES_TIME,
        EMPTY_STR,
        EMPTY_STR,
        Optional.of("/*"),
        Optional.of(SESSION_ID),
        Optional.of(DATA),
        Optional.of(HEADERS),
        Optional.of(IP_RANGES)
    );
    Assert.assertEquals(stdOut.toString().trim(), expected);
  }

}
