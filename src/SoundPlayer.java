import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;
import java.nio.file.Path;

/** Reproduz efeitos sonoros curtos sem parar a interface do jogo. */
public final class SoundPlayer {
    private static volatile int volume = 70;
    private SoundPlayer() { }

    public static int volume() { return volume; }
    public static void setVolume(int percent) { volume = Math.max(0, Math.min(100, percent)); }

    public static void play(String filename) {
        Thread soundThread = new Thread(() -> {
            try {
                Path path = Path.of("sounds", filename);
                if (!path.toFile().isFile()) path = Path.of("..", "sounds", filename);
                try (AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path.toString()))) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audio);
                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        if (volume == 0) gain.setValue(gain.getMinimum());
                        else {
                            float db = (float) (20.0 * Math.log10(volume / 100.0));
                            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
                        }
                    }
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
