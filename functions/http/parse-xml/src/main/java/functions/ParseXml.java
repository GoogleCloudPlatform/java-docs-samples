/*
 * Copyright 2022 Google LLC
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

package functions;

// [START functions_http_xml]
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ParseXml implements HttpFunction {
  private static final DocumentBuilderFactory dbFactory;

  static {
    dbFactory = DocumentBuilderFactory.newInstance();
    try {
      // Prevent XXE attacks (see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html)
      dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      dbFactory.setXIncludeAware(false);
      dbFactory.setExpandEntityReferences(false);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  // Parses a HTTP request in XML format
  // (Responds with a 400 error if the HTTP request isn't valid XML.)
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException, ParserConfigurationException {

    try {
      DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
      var writer = new PrintWriter(response.getWriter());

      // Get request body
      InputStream bodyStream = new ByteArrayInputStream(
          request.getInputStream().readAllBytes());

      // Parse + process XML
      Document doc = docBuilder.parse(bodyStream);
      writer.printf("Root element: %s", doc.getDocumentElement().getNodeName());
    } catch (SAXException e) {
      response.setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
      return;
    }
  }
}
// [END functions_http_xml]
