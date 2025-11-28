# NotebookLM Desktop (JavaFX)

This module is a JavaFX desktop refactor of the project using a clean MVC structure without Spring.

- Model: database-only classes (connection, DAOs, entities)
- Controller: application logic (e.g., auth, documents, flashcards)
- View: JavaFX UI elements (scenes, controls)

Currently implemented:
- Minimal JavaFX app with a Login screen (no autofill) and a placeholder Main screen.
- Database pool wiring via HikariCP (`Database`), reading env vars `DB_URL`, `DB_USER`, `DB_PASS` (falls back to local defaults).

## Build & Run (Windows)

```bash
cd d:/IT/NotebookLM-Java/desktop
mvn -q -DskipTests javafx:run
```

If JavaFX runtime is missing, ensure you use JDK 21 and Maven downloads OpenJFX per the POM.

## Next Steps
- Port existing database DAOs and entities into `desktop`'s `model` package (no Spring).
- Extract business logic from Spring services into `controller` classes (pure Java).
- Flesh out views (`MainView`) for chat, flashcards, and document library.
- Optionally, embed the existing web UI inside a JavaFX `WebView`.

## Decommission Spring
This desktop module is designed to run independently without Spring. As features are ported:
- Stop running the Spring Boot backend.
- Keep the MySQL schema intact; the desktop app reads and writes directly via JDBC/Hikari.
- RAG uses database-stored document chunks (`document_chunks` table) to assemble context for Gemini.

To run only desktop:
```bash
cd d:/IT/NotebookLM-Java/desktop
set GEMINI_API_KEY=YOUR_KEY_HERE
mvn -q -DskipTests javafx:run
```
