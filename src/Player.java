package roadtogold;

public final class Player {
    private final String name;
    private final String position;
    private final int quality;
    private int energy = 100;

    public Player(String name, String position, int quality) {
        this.name = name;
        this.position = position;
        this.quality = quality;
    }

    public String name() { return name; }
    public String position() { return position; }
    public int quality() { return quality; }
    public int energy() { return energy; }
    public void tire() { energy = Math.max(0, energy - (Math.random() < 0.6 ? 1 : 0)); }
}
