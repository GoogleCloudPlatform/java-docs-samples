# Markdown Templates for Google Cloud Platform Samples for Java

The `.mdpp` files in this directory are templates used in READMEs relating to
Google Cloud samples for Java.

## Before you begin

Install [MarkdownPP](https://github.com/jreese/markdown-pp), a preprocessor for
Markdown files. (Requires Python and PIP)

    pip install MarkdownPP

## Rendering the templates

    for readme in **/README.mdpp; do
      (
        cd $(dirname "$readme")
        markdown-pp README.mdpp -o README.md
      )
    done

Java is a registered trademark of Oracle Corporation and/or its affiliates.

