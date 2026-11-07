package roadtogold;

import java.util.List;

public final class Team {
    private final String name;
    private final int strength;
    private final String logoPath;
    private final List<Player> starters;
    private final List<Player> substitutes;

    public Team(String name, int strength, String logoPath, List<Player> starters, List<Player> substitutes) {
        this.name = name;
        this.strength = strength;
        this.logoPath = logoPath;
        this.starters = starters;
        this.substitutes = substitutes;
    }

    public String name() { return name; }
    public int strength() { return strength; }
    public String logoPath() { return logoPath; }
    public List<Player> starters() { return starters; }
    public List<Player> substitutes() { return substitutes; }
}
