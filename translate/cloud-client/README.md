# Getting Started with Google Translate API and the Google Cloud Client libraries

<a href="https://console.cloud.google.com/cloudshell/open?git_repo=https://github.com/GoogleCloudPlatform/java-docs-samples&page=editor&open_in_editor=translate/cloud-client/README.md">
<img alt="Open in Cloud Shell" src ="http://gstatic.com/cloudssh/images/open-btn.png"></a>

[Google Translate API][translate] provides a simple programmatic interface for translating an
arbitrary string into any supported language.
These sample Java applications demonstrate how to access the Google Translate API using
the [Google Cloud Client Library for Java][google-cloud-java].

[translate]: https://cloud.google.com/translate/docs/
[google-cloud-java]: https://github.com/GoogleCloudPlatform/google-cloud-java

Translate has two API versions: basic and advanced. For more info on the difference, see the
[editions](https://cloud.google.com/translate/docs/editions) documentation page.
- [Introducing Translation Advanced](https://cloud.google.com/translate/docs/intro-to-v3)
- [Migrating to Advanced](https://cloud.google.com/translate/docs/migrate-to-v3)

## Translate a string (using the quickstart sample)

Install [Maven](http://maven.apache.org/).

Build your project with:

	mvn clean package -DskipTests
    mvn exec:java -Dexec.mainClass=com.example.translate.QuickstartSample

## Samples for the Basic API Version
 - [Translating text](https://cloud.google.com/translate/docs/basic/translating-text)
 - [Discovering supported languages](https://cloud.google.com/translate/docs/basic/discovering-supported-languages)
 - [Detecting language](https://cloud.google.com/translate/docs/basic/detecting-language)

## Samples for the Advanced API Version

#### Translating text - [Documentation](https://cloud.google.com/translate/docs/advanced/translating-text-v3)
Related samples:
 - [TranslateText.java](src/main/java/com/example/translate/TranslateText.java)
 - [TranslateTextWithModel.java](src/main/java/com/example/translate/TranslateTextWithModel.java)

#### Discovering supported languages - [Documentation](https://cloud.google.com/translate/docs/advanced/discovering-supported-languages-v3)
Related samples:
 - [GetSupportedLanguages.java](src/main/java/com/example/translate/GetSupportedLanguages.java)
 - [GetSupportedLanguagesForTarget.java](src/main/java/com/example/translate/GetSupportedLanguagesForTarget.java) 

#### Detecting language - [Documentation](https://cloud.google.com/translate/docs/advanced/detecting-language-v3)
Related samples:
 - [DetectLangauge.java](src/main/java/com/example/translate/DetectLanguage.java)

#### Creating and using glossaries - [Documentation](https://cloud.google.com/translate/docs/advanced/glossary)
Related samples:
 - [CreateGlossary.java](src/main/java/com/example/translate/CreateGlossary.java)
 - [TranslateTextWithGlossary.java](src/main/java/com/example/translate/TranslateTextWithGlossary.java)
 - [GetGlossary.java](src/main/java/com/example/translate/GetGlossary.java)
 - [ListGlossaries.java](src/main/java/com/example/translate/ListGlossaries.java)
 - [DeleteGlossary.java](src/main/java/com/example/translate/DeleteGlossary.java)

#### Making batch requests - [Documentation](https://cloud.google.com/translate/docs/advanced/batch-translation)
Related samples:
 - [BatchTranslateText.java](src/main/java/com/example/translate/BatchTranslateText.java)
 - [BatchTranslateTextWithModel.java](src/main/java/com/example/translate/BatchTranslateTextWithModel.java)
 - [BatchTranslateTextWithGlossary.java](src/main/java/com/example/translate/BatchTranslateTextWithGlossary.java)
 - [BatchTranslateTextWithGlossaryAndModel](src/main/java/com/example/translate/BatchTranslateTextWithGlossaryAndModel.java)