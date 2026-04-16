# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start the MySQL database (required before running)
docker compose up -d

# Run the application
mvn javafx:run

# Build a self-contained fat JAR (output: target/pdsa-cw-1.0-SNAPSHOT-all.jar)
mvn package

# Run the fat JAR
java -jar target/pdsa-cw-1.0-SNAPSHOT-all.jar

# Run all tests
mvn test

# Run tests for a single game (e.g. game1)
mvn test -Dtest=MinimumCostTest

# Compile without running
mvn compile
```

## Architecture

The application is a JavaFX desktop app with a main menu that launches five independent puzzle/algorithm games, each in their own Stage window. Entry point: `Launcher` → `Main` (extends `Application`) → loads `MainMenu.fxml` → `MainMenuController`.

**Package structure** — every game follows the same layout under `com.pdsa.gameN`:
```
gameN/
  GameNApp.java          # Opens a new Stage from GameN.fxml
  ui/GameNController.java  # FXML controller (@FXML-annotated)
  algorithm/             # Two algorithm implementations per game
  db/GameNDB.java        # Standalone JDBC class (no shared connection util)
  model/                 # Domain model classes
```

FXML files live in `src/main/resources/com/pdsa/gameN/`. There is one shared stylesheet at `src/main/resources/com/pdsa/style.css`.

**Database** — MySQL 8.3 via Docker (`docker-compose.yml`). Credentials: `root`/`root`, database `pdsa_cw`, port 3306. Schema initialised from `database/schema.sql`. Each `GameNDB` class opens its own `DriverManager.getConnection()` independently — there is no shared DB utility.

**Games and their algorithms:**

| Game | Problem | Algorithms compared |
|------|---------|---------------------|
| 1 — Minimum Cost | Assignment problem (N×N cost matrix, N=50–100) | Hungarian (O(N³)) vs Branch & Bound |
| 2 — Snake & Ladder | Min dice throws on N×N board | BFS vs Dynamic Programming |
| 3 — Traffic Simulation | Max flow from source A to sink T | Ford-Fulkerson vs Edmonds-Karp |
| 4 — Knight's Tour | Full board coverage from a chosen start | Warnsdorff's heuristic vs Backtracking |
| 5 — Sixteen Queens | All solutions to the 16-queens problem | Sequential solver vs multi-threaded solver |

Each game records algorithm timing in milliseconds to its own DB table (`gameN_rounds`). Games 2–4 also record winners (`gameN_winners`) where players guess the answer.

**Key constraints:**
- Each game is a fully standalone package — no shared code between games.
- The fat JAR uses `Launcher` (not `Main`) as its entry point to avoid the "JavaFX runtime missing" error when running outside the Maven plugin.
- `module-info.java` opens each `gameN.ui` package to `javafx.fxml` so `FXMLLoader` can instantiate controllers.
