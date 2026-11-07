package roadtogold;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TeamCatalog {
    public record TeamInfo(String name, int strength, String logo) { }
    private static final List<TeamInfo> TEAMS = List.of(
        new TeamInfo("SL Benfica", 77, "teams/SL_Benfica_logo.png"),
        new TeamInfo("FC Porto", 78, "teams/FC_Porto_logo.png"),
        new TeamInfo("Sporting CP", 76, "teams/Sporting_CP_logo.png"),
        new TeamInfo("SC Braga", 72, "teams/SC_Braga_logo.png"),
        new TeamInfo("Portugal", 82, "teams/Portugal_logo.png"),
        new TeamInfo("Equipa personalizada", 70, "teams/custom_logo.png")
    );
    private static final String[] POSITIONS = {"GR", "DEF", "DEF", "DEF", "DEF", "MED", "MED", "MED", "ATA", "ATA", "ATA"};
    private static final String[] BENFICA = {"Samuel Soares", "Tomás Araújo", "Lenglet", "Dahl", "Bah", "Palhinha", "Aursnes", "Prestianni", "Pavlidis", "Rafa", "Schjelderup"};
    private static final String[] PORTO = {"Diogo Costa", "Martim Fernandes", "Bednarek", "Kiwior", "Francisco Moura", "Alan Varela", "Gabri Veiga", "Victor Froholdt", "Pepê", "Rafa", "Oskar P."};
    private static final int[] MOCK_QUALITY = {85, 89, 84, 89, 84, 77, 81, 89, 79, 80, 85};
    private static final String[] BENCH_POSITIONS = {"GR", "DEF", "DEF", "MED", "MED", "ATA", "ATA"};

    private TeamCatalog() { }
    public static List<TeamInfo> teams() { return TEAMS; }
    public static Team create(int index) {
        TeamInfo info = TEAMS.get(index);
        String[] names = info.name().equals("SL Benfica") ? BENFICA : info.name().equals("FC Porto") ? PORTO : null;
        Random random = new Random();
        List<Player> starters = new ArrayList<>();
        for (int i = 0; i < POSITIONS.length; i++) {
            int rating = names == null ? Math.max(45, Math.min(95, info.strength() + random.nextInt(13) - 6)) : MOCK_QUALITY[i];
            String player = names == null ? info.name() + " " + POSITIONS[i] + " " + (i + 1) : names[i];
            starters.add(new Player(player, POSITIONS[i], rating));
        }
        List<Player> bench = new ArrayList<>();
        for (int i = 0; i < BENCH_POSITIONS.length; i++) {
            bench.add(new Player("Suplente " + (i + 1), BENCH_POSITIONS[i], Math.max(45, info.strength() - 4 + random.nextInt(12))));
        }
        return new Team(info.name(), info.strength(), info.logo(), starters, bench);
    }
}
