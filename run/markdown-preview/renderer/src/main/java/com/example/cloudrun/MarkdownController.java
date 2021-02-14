/*
 * Copyright 2020 Google LLC
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

package com.example.cloudrun;

import java.util.Arrays;
import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarkdownController {

  @PostMapping("/")
  public String markdownRenderer(@RequestBody String payload) {
    // Set up HTML renderer
    // https://github.com/atlassian/commonmark-java#extensions
    List<Extension> extensions =
        Arrays.asList(TablesExtension.create(), StrikethroughExtension.create());
    Parser parser = Parser.builder().build();
    Node document = parser.parse(payload);
    HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
    // Convert Markdown to HTML
    String converted = renderer.render(document);

    // Use prepackaged policies to sanitize HTML. Cusomized and tighter standards
    // are recommended.
    PolicyFactory policy =
        Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES);
    String safeHtml = policy.sanitize(converted);

    return safeHtml;
  }
}
