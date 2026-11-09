/** Representa um jogador e guarda os seus atributos durante uma partida. */
public final class Player {
    /** Nome apresentado na interface do jogo. */
    private final String name;
    /** Posição do jogador: GR, DEF, MED ou ATA. */
    private final String position;
    /** Qualidade base do jogador, usada para calcular o desempenho. */
    private final int quality;
    /** Energia atual; diminui gradualmente enquanto o jogo decorre. */
    private int energy = 100;

    /** Cria um jogador com nome, posição e qualidade inicial. */
    public Player(String name, String position, int quality) {
        this.name = name;
        this.position = position;
        this.quality = quality;
    }

    /** Devolve o nome, a posição, a qualidade e a energia atual do jogador. */
    public String name() { return name; }
    public String position() { return position; }
    public int quality() { return quality; }
    public int energy() { return energy; }
    /** Simula o cansaço de um minuto, sem deixar a energia descer abaixo de zero. */
    public void tire() { energy = Math.max(0, energy - (Math.random() < 0.6 ? 1 : 0)); }
}
