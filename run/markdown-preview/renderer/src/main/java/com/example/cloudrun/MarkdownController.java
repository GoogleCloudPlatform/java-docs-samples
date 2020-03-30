package com.example.cloudrun;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Arrays;
import java.util.List;

import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
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
        List<Extension> extensions = Arrays.asList(TablesExtension.create(), StrikethroughExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(payload);
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        // Convert Markdown to HTML
        String converted = renderer.render(document);

        // Use prepackaged policies to sanitize HTML. Cusomized and tighter standards
        // are recommended.
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS).and(Sanitizers.LINKS).and(Sanitizers.IMAGES)
                .and(Sanitizers.TABLES);
        String safeHTML = policy.sanitize(converted);

        return safeHTML;
    }
}
