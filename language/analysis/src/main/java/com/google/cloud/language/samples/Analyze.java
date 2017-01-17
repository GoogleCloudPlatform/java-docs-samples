/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.cloud.language.samples;

import com.google.cloud.language.spi.v1.LanguageServiceClient;

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.AnalyzeSyntaxRequest;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Token;
import com.google.protobuf.Descriptors;

import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

/**
 * A sample application that uses the Natural Language API to perform
 * entity, sentiment and syntax analysis.
 */
public class Analyze {
  /**
   * Detects entities,sentiment and syntax in a document using the Natural Language API.
   */
  public static void main(String[] args) throws IOException, GeneralSecurityException {
    if (args.length != 2) {
      System.err.println("Usage:");
      System.err.printf(
          "\tjava %s \"command\" \"text to analyze\"\n",
          Analyze.class.getCanonicalName());
      System.exit(1);
    }
    String command = args[0];
    String text = args[1];

    Analyze app = new Analyze(LanguageServiceClient.create());

    if (command.equals("entities")) {
      if (text.startsWith("gs://")) {
        printEntities(System.out, app.analyzeEntitiesFile(text));
      } else {
        printEntities(System.out, app.analyzeEntitiesText(text));
      }
    } else if (command.equals("sentiment")) {
      if (text.startsWith("gs://")) {
        printSentiment(System.out, app.analyzeSentimentFile(text));
      } else {
        printSentiment(System.out, app.analyzeSentimentText(text));
      }
    } else if (command.equals("syntax")) {
      if (text.startsWith("gs://")) {
        printSyntax(System.out, app.analyzeSyntaxFile(text));
      } else {
        printSyntax(System.out, app.analyzeSyntaxText(text));
      }
    }
  }

  /**
   * Print a list of {@code entities}.
   */
  public static void printEntities(PrintStream out, List<Entity> entities) {
    if (entities == null || entities.size() == 0) {
      out.println("No entities found.");
      return;
    }
    out.printf("Found %d entit%s.\n", entities.size(), entities.size() == 1 ? "y" : "ies");
    for (Entity entity : entities) {
      out.printf("%s\n", entity.getName());
      out.printf("\tSalience: %.3f\n", entity.getSalience());
      out.printf("\tType: %s\n", entity.getType());
      if (entity.getMetadataMap() != null) {
        for (Map.Entry<String, String> metadata : entity.getMetadataMap().entrySet()) {
          out.printf("\tMetadata: %s = %s\n", metadata.getKey(), metadata.getValue());
        }
      }
      if (entity.getMentionsList() != null) {
        for (EntityMention mention : entity.getMentionsList()) {
          for (Map.Entry<Descriptors.FieldDescriptor, Object> mentionSetMember :
              mention.getAllFields().entrySet()) {
            out.printf("\tMention: %s = %s\n", mentionSetMember.getKey(),
                mentionSetMember.getValue());
          }
        }
      }
    }
  }

  /**
   * Print the Sentiment {@code sentiment}.
   */
  public static void printSentiment(PrintStream out, Sentiment sentiment) {
    if (sentiment == null) {
      out.println("No sentiment found");
      return;
    }
    out.println("Found sentiment.");
    out.printf("\tMagnitude: %.3f\n", sentiment.getMagnitude());
    out.printf("\tScore: %.3f\n", sentiment.getScore());
  }

  /**
   * Prints the Syntax for the {@code tokens}.
   */
  public static void printSyntax(PrintStream out, List<Token> tokens) {
    if (tokens == null || tokens.size() == 0) {
      out.println("No syntax found");
      return;
    }
    out.printf("Found %d token%s.\n", tokens.size(), tokens.size() == 1 ? "" : "s");
    for (Token token : tokens) {
      out.println("TextSpan");
      out.printf("\tText: %s\n", token.getText().getContent());
      out.printf("\tBeginOffset: %d\n", token.getText().getBeginOffset());
      out.printf("Lemma: %s\n", token.getLemma());
      out.printf("PartOfSpeechTag: %s\n", token.getPartOfSpeech().getTag());
      out.printf("\tAspect: %s\n",token.getPartOfSpeech().getAspect());
      out.printf("\tCase: %s\n", token.getPartOfSpeech().getCase());
      out.printf("\tForm: %s\n", token.getPartOfSpeech().getForm());
      out.printf("\tGender: %s\n",token.getPartOfSpeech().getGender());
      out.printf("\tMood: %s\n", token.getPartOfSpeech().getMood());
      out.printf("\tNumber: %s\n", token.getPartOfSpeech().getNumber());
      out.printf("\tPerson: %s\n", token.getPartOfSpeech().getPerson());
      out.printf("\tProper: %s\n", token.getPartOfSpeech().getProper());
      out.printf("\tReciprocity: %s\n", token.getPartOfSpeech().getReciprocity());
      out.printf("\tTense: %s\n", token.getPartOfSpeech().getTense());
      out.printf("\tVoice: %s\n", token.getPartOfSpeech().getVoice());
      out.println("DependencyEdge");
      out.printf("\tHeadTokenIndex: %d\n", token.getDependencyEdge().getHeadTokenIndex());
      out.printf("\tLabel: %s\n", token.getDependencyEdge().getLabel());
    }
  }


  private final LanguageServiceClient languageApi;

  /**
   * Constructs a {@link Analyze} which connects to the Cloud Natural Language API.
   */
  public Analyze(LanguageServiceClient languageApi) {
    this.languageApi = languageApi;
  }

  /**
   * Gets {@link Entity}s from the string {@code text}.
   */
  public List<Entity> analyzeEntitiesText(String text) throws IOException {
    // Note: This does not work on App Engine standard.
    Document doc = Document.newBuilder()
        .setContent(text).setType(Type.PLAIN_TEXT).build();
    AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
        .setDocument(doc)
        .setEncodingType(EncodingType.UTF16).build();
    AnalyzeEntitiesResponse response = languageApi.analyzeEntities(request);
    return response.getEntitiesList();
  }

  /**
   * Gets {@link Entity}s from the contents of the object at the given GCS {@code path}.
   */
  public List<Entity> analyzeEntitiesFile(String path) throws IOException {
    // Note: This does not work on App Engine standard.
    Document doc = Document.newBuilder()
        .setGcsContentUri(path).setType(Type.PLAIN_TEXT).build();
    AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
        .setDocument(doc)
        .setEncodingType(EncodingType.UTF16).build();
    AnalyzeEntitiesResponse response = languageApi.analyzeEntities(request);
    return response.getEntitiesList();
  }

  /**
   * Gets {@link Sentiment} from the string {@code text}.
   */
  public Sentiment analyzeSentimentText(String text) throws IOException {
    // Note: This does not work on App Engine standard.
    Document doc = Document.newBuilder()
        .setContent(text).setType(Type.PLAIN_TEXT).build();
    AnalyzeSentimentResponse response = languageApi.analyzeSentiment(doc);
    return response.getDocumentSentiment();
  }

  /**
   * Gets {@link Sentiment} from the contents of the object at the given GCS {@code path}.
   */
  public Sentiment analyzeSentimentFile(String path) throws IOException {
    // Note: This does not work on App Engine standard.
    Document doc = Document.newBuilder()
        .setGcsContentUri(path).setType(Type.PLAIN_TEXT).build();
    AnalyzeSentimentResponse response = languageApi.analyzeSentiment(doc);
    return response.getDocumentSentiment();
  }

  /**
   * Gets {@link Token}s from the string {@code text}.
   */
  public List<Token> analyzeSyntaxText(String text) throws IOException {
    // Note: This does not work on App Engine standard.
    Document doc = Document.newBuilder()
        .setContent(text).setType(Type.PLAIN_TEXT).build();
    AnalyzeSyntaxRequest request = AnalyzeSyntaxRequest.newBuilder()
        .setDocument(doc)
        .setEncodingType(EncodingType.UTF16).build();
    AnalyzeSyntaxResponse response = languageApi.analyzeSyntax(request);
    return response.getTokensList();
  }

  /**
   * Gets {@link Token}s from the contents of the object at the given GCS {@code path}.
   */
  public List<Token> analyzeSyntaxFile(String path) throws IOException {
    // Note: This does not work on App Engine standard.
    Document doc = Document.newBuilder()
        .setGcsContentUri(path).setType(Type.PLAIN_TEXT).build();
    AnalyzeSyntaxRequest request = AnalyzeSyntaxRequest.newBuilder()
        .setDocument(doc)
        .setEncodingType(EncodingType.UTF16).build();
    AnalyzeSyntaxResponse response = languageApi.analyzeSyntax(request);
    return response.getTokensList();
  }
}
