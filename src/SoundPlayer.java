import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.nio.file.Path;

/** Reproduz efeitos sonoros curtos sem parar a interface do jogo. */
public final class SoundPlayer {
    private SoundPlayer() { }

    public static void play(String filename) {
        Thread soundThread = new Thread(() -> {
            try {
                Path path = Path.of("sounds", filename);
                if (!path.toFile().isFile()) path = Path.of("..", "sounds", filename);
                try (AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path.toString()))) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audio);
                    clip.start();
                }
            } catch (Exception ex) {
                System.err.println("Não foi possível reproduzir " + filename + ": " + ex.getMessage());
            }
        }, "game-sound");
        soundThread.setDaemon(true);
        soundThread.start();
    }
}
