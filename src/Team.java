import java.util.List;

/** Guarda os dados de uma equipa e os jogadores disponíveis para a partida. */
public final class Team {
    /** Nome apresentado no marcador e nos menus. */
    private final String name;
    /** Força geral usada na previsão de resultados. */
    private final int strength;
    /** Caminho da imagem do emblema dentro da pasta do projeto. */
    private final String logoPath;
    /** Onze jogadores que começam a partida. */
    private final List<Player> starters;
    /** Jogadores disponíveis para substituições. */
    private final List<Player> substitutes;

    /** Cria a equipa com identidade, força, titulares e suplentes. */
    public Team(String name, int strength, String logoPath, List<Player> starters, List<Player> substitutes) {
        this.name = name;
        this.strength = strength;
        this.logoPath = logoPath;
        this.starters = starters;
        this.substitutes = substitutes;
    }

    /** Métodos de acesso usados pela interface e pela simulação. */
    public String name() { return name; }
    public int strength() { return strength; }
    public String logoPath() { return logoPath; }
    public List<Player> starters() { return starters; }
    public List<Player> substitutes() { return substitutes; }
}
