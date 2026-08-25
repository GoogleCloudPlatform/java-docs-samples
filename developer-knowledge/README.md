# Google Developer Knowledge API Java Samples

This directory contains Java code samples demonstrating how to use the [Google Developer Knowledge API](https://developers.google.com/knowledge) client library (`com.google.cloud:google-cloud-developer-knowledge`).

## Setup

1. Enable the Developer Knowledge API on your Google Cloud project:
   ```bash
   gcloud services enable developerknowledge.googleapis.com
   ```

2. Build with Maven:
   ```bash
   mvn clean compile
   ```

## Samples

* **[Search Document Chunks](src/main/java/developerknowledge/SearchDocumentChunks.java)**: Search public developer documentation chunks by query (`developerknowledge_search_document_chunks`).
* **[Get Document](src/main/java/developerknowledge/GetDocument.java)**: Retrieve a single documentation page with full markdown content (`developerknowledge_get_document`).
* **[Batch Get Documents](src/main/java/developerknowledge/BatchGetDocuments.java)**: Fetch multiple documentation pages in one call (`developerknowledge_batch_get_documents`).
* **[Answer Query](src/main/java/developerknowledge/AnswerQuery.java)**: Get a grounded, cited answer to a technical question (`developerknowledge_answer_query`).

## Running Tests

```bash
mvn test
```
