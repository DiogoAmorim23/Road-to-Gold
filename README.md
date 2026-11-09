# Road to Gold

A simple Java football simulator with a graphical interface. Choose teams, play a match minute by minute, manage substitutions, or simulate likely results.

## Run the game

You need Java 16 or newer. On Windows, double-click `run.bat`. It compiles the source files into `out` and starts the game.

## Project folders

- `src/` — Java source code
- `out/` — compiled Java files (generated when the game is run)
- `teams/` — team badge images
- `backgrounds/` — menu and game backgrounds
- `assets/` — other images used by the interface
- `font/` — regular and bold game fonts
- `sounds/` — sound effects

## Add a team

1. Put the team's badge image in `teams/`.
2. Open `src/TeamCatalog.java` and add a `TeamInfo` entry inside the `TEAMS` list. Use the image path relative to the project folder.
3. Add each starter and substitute as a `PlayerInfo` with their name, position, and quality. The positions used by the game are `GR`, `DEF`, `MED`, and `ATA`.

Example:

```java
new TeamInfo("Example FC", 75, "teams/Example_FC.png", List.of(
    new PlayerInfo("Goalkeeper Name", "GR", 82),
    new PlayerInfo("Defender Name", "DEF", 79),
    new PlayerInfo("Defender Two", "DEF", 77),
    new PlayerInfo("Defender Three", "DEF", 80),
    new PlayerInfo("Defender Four", "DEF", 76),
    new PlayerInfo("Midfielder Name", "MED", 84),
    new PlayerInfo("Midfielder Two", "MED", 81),
    new PlayerInfo("Midfielder Three", "MED", 78),
    new PlayerInfo("Forward Name", "ATA", 85),
    new PlayerInfo("Forward Two", "ATA", 82),
    new PlayerInfo("Forward Three", "ATA", 80)
), List.of(
    new PlayerInfo("Substitute Goalkeeper", "GR", 75),
    new PlayerInfo("Substitute Defender", "DEF", 74),
    new PlayerInfo("Substitute Defender Two", "DEF", 73),
    new PlayerInfo("Substitute Midfielder", "MED", 77),
    new PlayerInfo("Substitute Midfielder Two", "MED", 76),
    new PlayerInfo("Substitute Forward", "ATA", 78),
    new PlayerInfo("Substitute Forward Two", "ATA", 75)
)),
```

The first `List.of(...)` contains the 11 starters. The second contains the substitutes. Each player's quality is a number; the team's strength is used by the quick match prediction.

## Screenshots

<img width="1675" height="985" alt="1v1 screen" src="https://github.com/user-attachments/assets/8d7fc17e-e850-486a-9a20-febde1de483f" />

<img width="1675" height="984" alt="simulator-menu" src="https://github.com/user-attachments/assets/9401a720-c198-4311-9553-524f87f51b4f" />
