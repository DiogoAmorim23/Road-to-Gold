import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class RoadToGold {
    private RoadToGold() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Road to Gold");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new GamePanel());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
