import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Ponto de entrada: cria a janela principal e inicia a interface Swing. */
public final class RoadToGold {
    private RoadToGold() { }
    public static void main(String[] args) {
        // Cria os componentes gráficos na thread própria da interface Swing.
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Road to Gold");
            // Configura a janela e coloca nela o painel com os ecrãs do jogo.
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new GamePanel());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setExtendedState(JFrame.MAXIMIZED_BOTH);
            window.setVisible(true);
        });
    }
}
