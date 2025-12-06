# TIC TAC TOE THROUGH XMPP

A small Java CLI multiplayer Tic-Tac-Toe built over XMPP (Smack).  

## Table of Contents
- [How to play](#how-to-play)
- [Packages & responsibilities](#packages--responsibilities)
- [How the pieces interact](#how-the-pieces-interact)
- [Quick pointers](#quick-pointers)
- [Testing](#testing)


## How to play

- Clone the repository on your local machine
- Double-click on the `run.bat` and follow the instructions, or
- Open a terminal in the main directory and input the following command `make run`
* Alternatively run the provided fat JAR by hand: `java -jar target/xmpp-TicTacToe-1.0-SNAPSHOT-shaded.jar`
---

The project separates concerns into three focused packages so UI, core logic, and networking remain easy to maintain and test.

---

## Packages & responsibilities

### `com.harutyun.cli` — Console UI / rendering
**Responsibility:** Terminal-facing presentation and user interaction.

- Renders the board.
- Key file: `BoardRender.java`.

### `com.harutyun.game.core` — Game domain (core logic)
**Responsibility:** Game model, rules, and in-memory state.

- Domain classes and logic: `TicTacToeBoard`, `Move`, `Player`, `Participant`, `GameOffer`, etc.
- Enforces game rules (turn-order, bounds checks, winner detection).
- Pure business logic — no XMPP or I/O, making it deterministic.

### `com.harutyun.xmppclient` — Networking & protocol
**Responsibility:** XMPP connectivity, stanza extension handling, and message plumbing.

- Contains the custom extension and provider (`GameExtension`, `GameExtensionProvider`) that serialize/parse `<game>` stanzas and `<move/>` children.
- Translates network events into domain actions.

---

## How the pieces interact

**Flow (high level):**

`Main.java` → CLI prompt → `TicTacToe` → `game.core` updates → `xmppclient` sends stanzas → remote `xmppclient` parses and forwards to remote `TicTacToe`.

---

## Quick pointers
- **Entry point:** `Main.java` — reads credentials and starts the app.
- **Renderer:** `com.harutyun.cli.BoardRender` — board output.
- **Game engine:** `com.harutyun.game.core.TicTacToeBoard` — all game rules and state.
- **Protocol:** `com.harutyun.xmppclient.GameExtension` / `GameExtensionProvider` — stanza format and parsing.

---

## Testing
- For testig purposes you can use the following xmpp.jp accounts:
  - **Username 1:** `har8@xmpp.jp`
  - **Username 2:** `har9@xmpp.jp`
  - **Password for both:** `TryingSomething100!`
  - **Note:** These are test accounts. For production use, please create your own XMPP accounts.
---