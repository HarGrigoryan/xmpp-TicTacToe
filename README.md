# TIC TAC TOE THROUGH XMPP


A small Java CLI multiplayer Tic-Tac-Toe built over XMPP (Smack).  
The project separates concerns into three focused packages so UI, domain logic, and networking remain easy to maintain and test.

---

## Packages & responsibilities

### `com.harutyun.cli` — Console UI / rendering
**Responsibility:** Terminal-facing presentation and user interaction.

- Renders the board and prompts (ASCII art).
- Handles CLI-only presentation (help text, input prompts, optional ANSI colors).
- Key file: `BoardRender.java`.

### `com.harutyun.game.core` — Game domain (core logic)
**Responsibility:** Game model, rules, and in-memory state.

- Domain classes and logic: `TicTacToeBoard`, `Move`, `Player`, `Participant`, `GameOffer`, etc.
- Enforces game rules (turn-order, bounds checks, winner detection).
- Pure business logic — no XMPP or I/O, making it deterministic and unit-testable.

### `com.harutyun.xmppclient` / `com.harutyun.xmppclient.xmpp` — Networking & protocol
**Responsibility:** XMPP connectivity, stanza extension handling, and message plumbing.

- Manages XMPP connection and chat lifecycle (`XMPPClient.java`).
- Contains the custom extension and provider (`GameExtension`, `GameExtensionProvider`) that serialize/parse `<game>` stanzas and `<move/>` children.
- Sends/receives game offers, moves and acknowledgements; translates network events into domain actions.
- **Note:** register the extension provider before connecting.

---

## How the pieces interact (brief)
- The **CLI** reads user commands and prints the board; it creates `Move` objects and queries/updates the domain.
- The **domain** (`game.core`) applies rules and updates state.
- The **xmppclient** serializes domain actions into XMPP stanzas for the network and parses incoming stanzas back into domain actions.

Flow (high level):  
`Main.java` → CLI prompt → `TicTacToe` (controller) → `game.core` updates → `xmppclient` sends stanzas → remote `xmppclient` parses and forwards to remote `TicTacToe`.

---

## Quick pointers (files & flow)
- **Entry point:** `Main.java` — reads credentials and starts the app.
- **Renderer:** `com.harutyun.cli.BoardRender` — ASCII board output.
- **Game engine:** `com.harutyun.game.core.TicTacToeBoard` — all game rules and state.
- **Protocol:** `com.harutyun.xmppclient.xmpp.GameExtension` / `GameExtensionProvider` — stanza format and parsing.

---

## Suggestions & next steps
- Keep domain logic (`game.core`) free of I/O and networking so it remains easy to unit test.
- Keep CLI rendering simple; move larger help text to a constant or resource file.
- Keep XMPP parsing/serialization isolated in `xmppclient` and log raw stanzas when debugging interoperability issues.

---

If you want, I can expand this into a full README with build/run instructions, sample session, and contribution notes.
