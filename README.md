# NotebookLM Desktop (JavaFX)

## Overview

NotebookLM Desktop is a JavaFX application that ingests PDF documents, splits them into semantic chunks, embeds them, and performs Retrieval Augmented Generation (RAG) queries against a MySQL-backed embedding store using LangChain4j and Google Vertex AI (Gemini + Embeddings). It also supports authentication and flashcard generation.

## Core Features

- PDF ingestion and parsing (Apache PDFBox via LangChain4j)
- Custom hierarchical/paragraph/sentence/word splitters for chunking
- Embedding storage in MySQL (DAO layer + HikariCP)
- RAG querying pipeline (LangChain4j + Vertex AI Gemini)
- Simple authentication and user/session handling
- Flashcard generation from document chunks
- JavaFX UI with Ikonli icons

## Technology Stack

- Java 21
- JavaFX 21.0.3 (controls, FXML, graphics, web)
- LangChain4j 0.35.0 (core, Vertex AI Gemini + Embeddings, PDF parser)
- Google Vertex AI (Gemini + Embeddings)
- MySQL 8.x + HikariCP 5.1.0
- Gson 2.10.1 (JSON)
- SLF4J Simple 2.0.13 (logging)
- Ikonli 12.3.1 (icons)
- Maven (javafx-maven-plugin)

## Prerequisites

1. JDK 21 installed.
2. Maven 3.9+ installed.
3. MySQL server and schema (default schema: `chatbotjava`).
4. Google Cloud project with Vertex AI enabled.
5. (Optional) Service account JSON or Application Default Credentials for Vertex AI.
6. Environment variables (override defaults):
   - `DB_URL` (default `jdbc:mysql://localhost:3306/chatbotjava`)
   - `DB_USER` (default `root`)
   - `DB_PASS` (default `Nick@5439` — change this!)
   - `VERTEX_AI_PROJECT_ID` (optional)
   - `VERTEX_AI_LOCATION` (e.g. `us-central1`)

## Environment Setup (Windows bash.exe)

Set session variables:

```bash
export DB_URL="jdbc:mysql://localhost:3306/chatbotjava"
export DB_USER="root"
export DB_PASS="yourStrongPassword"
export VERTEX_AI_PROJECT_ID="your-gcp-project"
export VERTEX_AI_LOCATION="us-central1"
```

Authenticate to Google Cloud:

```bash
gcloud auth application-default login
# OR
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
```

## Build & Run

Build:

```bash
mvn clean package
```

Run (JavaFX plugin):

```bash
mvn javafx:run
```

Run manually (after copying runtime deps):

```bash
mvn dependency:copy-dependencies -DincludeScope=runtime
java -cp target/classes;target/dependency/* com.khoa.notebooklm.desktop.MainApp
```

Clean:

```bash
mvn clean
```
