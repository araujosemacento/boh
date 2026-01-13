package poo.melitoh.boh.model;

import poo.melitoh.boh.ui.Typewriter;
import poo.melitoh.boh.utils.SoundManager;
import poo.melitoh.boh.core.PlaybackController; // Import PlaybackController
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Personagem principal Boh.
 */
public class Boh extends Actor {

    private static final List<String> IDLE_FACES = Arrays.asList("[ ▀ ¸ ▀]", "[ ▀ ° ▀]",
            "[ ▀ ■ ▀]", "[ ▀ ─ ▀]", "[ ▀ ~ ▀]", "[ ▀ ▄ ▀]", "[ ▀ ¬ ▀]", "[ ▀ · ▀]",
            "[ ▀ _ ▀]");

    private volatile String currentFace = IDLE_FACES.get(0);
    private volatile String currentSpeech = "";
    private volatile String staticText = ""; // Texto estático exibido ao lado
    private final AtomicBoolean animating = new AtomicBoolean(true);
    private final AtomicBoolean talking = new AtomicBoolean(false); // Track
                                                                    // talking
                                                                    // state
    private PlaybackController playbackController; // Reference to controller
    private volatile Thread currentTypewriterThread; // Referência para
                                                     // interrupção

    public Boh() {
        super();
        startFaceAnimation();
    }

    public void setPlaybackController(PlaybackController controller) {
        this.playbackController = controller;
    }

    private void startFaceAnimation() {
        Thread faceThread = new Thread(() -> {
            int index = 0;
            while (animating.get()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                // Only update face if talking
                if (talking.get()) {
                    currentFace = IDLE_FACES.get(index % IDLE_FACES.size());
                    index++;
                }
            }
        }, "Boh-FaceAnim");
        faceThread.start();
    }

    public void setMood(String mood) {
        // Face override logic could go here
    }

    /**
     * Define o texto estático exibido ao lado da fala.
     */
    public void setStaticText(String text) {
        this.staticText = text != null ? text : "";
    }

    /**
     * Retorna o texto estático atual.
     */
    public String getStaticText() {
        return staticText;
    }

    /**
     * Limpa o texto estático.
     */
    public void clearStaticText() {
        this.staticText = "";
    }

    /**
     * Interrompe a fala atual, se houver.
     */
    public void interruptSpeech() {
        if (currentTypewriterThread != null && currentTypewriterThread.isAlive()) {
            currentTypewriterThread.interrupt();
        }
        talking.set(false);
    }

    public Thread say(String text) {
        // Use default suppliers if controller not present (fallback)
        java.util.function.Supplier<Boolean> runSupplier = () -> (playbackController == null
                || playbackController.isPlaying());
        java.util.function.Supplier<Long> delaySupplier = () -> (playbackController != null
                ? playbackController.getCharDelay()
                : 50L);

        Typewriter tw = new Typewriter(text, s -> currentSpeech = s,
                c -> SoundManager.playTypingSound(c), runSupplier, delaySupplier,
                talking::set);
        Thread t = new Thread(tw, "Typewriter-Boh");
        currentTypewriterThread = t;
        t.start();
        return t;
    }

    /**
     * Exibe texto estático imediatamente (sem animação de digitação).
     */
    public void showStatic(String text) {
        this.currentSpeech = "";
        this.staticText = text != null ? text : "";
    }

    public String getCurrentFace() {
        return currentFace;
    }

    public String getCurrentSpeech() {
        return currentSpeech;
    }

    public boolean isTalking() {
        return talking.get();
    }

    @Override
    public void updateState() {
        // Atualizações por frame se necessário
    }

    public void cleanup() {
        animating.set(false);
    }
}
