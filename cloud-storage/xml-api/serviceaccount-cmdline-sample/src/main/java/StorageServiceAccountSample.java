//[START all]
/*
 * Copyright (c) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Preconditions;
import com.google.common.io.Files;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.net.URLEncoder;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class StorageServiceAccountSample {

  /** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
  private static final String STORAGE_SCOPE =
      "https://www.googleapis.com/auth/devstorage.read_write";

  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  public static void main(String[] args) {
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      // Check for valid setup.
      Preconditions.checkArgument(args.length == 1,
          "Please pass in the Google Cloud Storage bucket name to display");
      String bucketName = args[0];

      //[START snippet]
      // Build a service account credential.
      GoogleCredential credential = GoogleCredential.getApplicationDefault()
          .createScoped(Collections.singleton(STORAGE_SCOPE));

      // Set up and execute a Google Cloud Storage request.
      String URI = "https://storage.googleapis.com/" + URLEncoder.encode(bucketName, "UTF-8");
      HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
      GenericUrl url = new GenericUrl(URI);
      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse response = request.execute();
      String content = response.parseAsString();
     //[END snippet]

      // Instantiate transformer input.
      Source xmlInput = new StreamSource(new StringReader(content));
      StreamResult xmlOutput = new StreamResult(new StringWriter());

      // Configure transformer.
      Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity
                                                                                   // transformer
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(xmlInput, xmlOutput);

      // Pretty print the output XML.
      System.out.println("\nBucket listing for " + bucketName + ":\n");
      System.out.println(xmlOutput.getWriter().toString());
      System.exit(0);

    } catch (IOException e) {
        System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.exit(1);
  }
}
//[END all]
