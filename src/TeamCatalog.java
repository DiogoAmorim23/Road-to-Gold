import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Lista as equipas do jogo e cria os respetivos plantéis. */
public final class TeamCatalog {

    /** Os dados de cada jogador ficam juntos: nome, posição e qualidade. */
    public record PlayerInfo(String name, String position, int quality) { }

    /** Cada equipa guarda separadamente os titulares e os suplentes. */
    public record TeamInfo(String name, int strength, String logo,
                           List<PlayerInfo> players, List<PlayerInfo> substitutes) { }

    private static final List<TeamInfo> TEAMS = List.of(
        new TeamInfo("SL Benfica", 77, "teams/SL_Benfica_logo.png", List.of(
            new PlayerInfo("Samuel Soares", "GR", 85),
            new PlayerInfo("Tomás Araújo", "DEF", 89),
            new PlayerInfo("Lenglet", "DEF", 84),
            new PlayerInfo("Dahl", "DEF", 89),
            new PlayerInfo("Bah", "DEF", 84),
            new PlayerInfo("Palhinha", "MED", 77),
            new PlayerInfo("Aursnes", "MED", 81),
            new PlayerInfo("Prestianni", "MED", 89),
            new PlayerInfo("Pavlidis", "ATA", 79),
            new PlayerInfo("Rafa", "ATA", 80),
            new PlayerInfo("Schjelderup", "ATA", 85)
        ), List.of(
            new PlayerInfo("Anatoliy Trubin", "GR", 84),
            new PlayerInfo("António Silva", "DEF", 83),
            new PlayerInfo("Nicolás Otamendi", "DEF", 82),
            new PlayerInfo("Leandro Barreiro", "MED", 78),
            new PlayerInfo("Florentino", "MED", 80),
            new PlayerInfo("Andreas Schjelderup", "ATA", 79),
            new PlayerInfo("Arthur Cabral", "ATA", 77)
        )),
        new TeamInfo("FC Porto", 78, "teams/FC_Porto_logo.png", List.of(
            new PlayerInfo("Diogo Costa", "GR", 85),
            new PlayerInfo("Martim Fernandes", "DEF", 89),
            new PlayerInfo("Bednarek", "DEF", 84),
            new PlayerInfo("Kiwior", "DEF", 89),
            new PlayerInfo("Francisco Moura", "DEF", 84),
            new PlayerInfo("Alan Varela", "MED", 77),
            new PlayerInfo("Gabri Veiga", "MED", 81),
            new PlayerInfo("Victor Froholdt", "MED", 89),
            new PlayerInfo("Pepê", "ATA", 79),
            new PlayerInfo("Rafa", "ATA", 80),
            new PlayerInfo("Oskar P.", "ATA", 85)
        ), List.of(
            new PlayerInfo("Cláudio Ramos", "GR", 78),
            new PlayerInfo("Zé Pedro", "DEF", 76),
            new PlayerInfo("João Mário", "DEF", 78),
            new PlayerInfo("Eustáquio", "MED", 79),
            new PlayerInfo("Grujić", "MED", 77),
            new PlayerInfo("Gonçalo Borges", "ATA", 76),
            new PlayerInfo("Deniz Gül", "ATA", 75)
        )),
        new TeamInfo("Sporting CP", 76, "teams/Sporting_CP_logo.png", List.of(), List.of()),
        new TeamInfo("SC Braga", 72, "teams/SC_Braga_logo.png", List.of(), List.of()),
        new TeamInfo("Vitória SC", 70, "teams/Vitoria_SC_logo.png", List.of(), List.of()),
        new TeamInfo("Portugal", 82, "teams/Portugal_logo.png", List.of(), List.of()),
        new TeamInfo("Equipa personalizada", 70, "teams/custom_logo.png", List.of(), List.of())
    );

    private static final String[] STARTING_POSITIONS = {
        "GR", "DEF", "DEF", "DEF", "DEF", "MED", "MED", "MED", "ATA", "ATA", "ATA"
    };
    private static final String[] BENCH_POSITIONS = {"GR", "DEF", "DEF", "MED", "MED", "ATA", "ATA"};

    private TeamCatalog() { }

    /** Devolve a lista usada pelo menu de escolha das equipas. */
    public static List<TeamInfo> teams() { return TEAMS; }

    /** Cria uma equipa a partir dos dados guardados no catálogo. */
    public static Team create(int index) {
        TeamInfo info = TEAMS.get(index);
        List<PlayerInfo> playerData = info.players();

        // Equipas ainda sem plantel definido recebem jogadores simples gerados automaticamente.
        if (playerData.isEmpty()) {
            playerData = createExampleRoster(info.name(), info.strength());
        }

        List<Player> starters = new ArrayList<>();
        for (PlayerInfo data : playerData) {
            starters.add(new Player(data.name(), data.position(), data.quality()));
        }

        List<Player> substitutes;
        if (info.substitutes().isEmpty()) {
            substitutes = createSubstitutes(info.name(), info.strength());
        } else {
            substitutes = convertPlayers(info.substitutes());
        }
        return new Team(info.name(), info.strength(), info.logo(), starters, substitutes);
    }

    /** Cria a equipa personalizada com o nome e a força escritos no menu. */
    public static Team createCustom(String name, int strength) {
        TeamInfo custom = TEAMS.get(TEAMS.size() - 1);
        List<PlayerInfo> playerData = createExampleRoster(name, strength);
        List<Player> starters = new ArrayList<>();
        for (PlayerInfo data : playerData) {
            starters.add(new Player(data.name(), data.position(), data.quality()));
        }
        return new Team(name, strength, custom.logo(), starters, createSubstitutes(name, strength));
    }

    /** Converte os dados escritos no catálogo em objetos usados durante o jogo. */
    private static List<Player> convertPlayers(List<PlayerInfo> playerData) {
        List<Player> players = new ArrayList<>();
        for (PlayerInfo data : playerData) {
            players.add(new Player(data.name(), data.position(), data.quality()));
        }
        return players;
    }

    private static List<PlayerInfo> createExampleRoster(String teamName, int strength) {
        List<PlayerInfo> players = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < STARTING_POSITIONS.length; i++) {
            String position = STARTING_POSITIONS[i];
            int quality = Math.max(45, Math.min(95, strength + random.nextInt(13) - 6));
            players.add(new PlayerInfo(position + " " + (i + 1), position, quality));
        }
        return players;
    }

    private static List<Player> createSubstitutes(String teamName, int strength) {
        List<Player> substitutes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < BENCH_POSITIONS.length; i++) {
            int quality = Math.max(45, Math.min(95, strength - 4 + random.nextInt(12)));
            substitutes.add(new Player("Suplente " + (i + 1), BENCH_POSITIONS[i], quality));
        }
        return substitutes;
    }
}
