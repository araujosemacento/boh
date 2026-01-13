package poo.melitoh.boh.model;

import poo.melitoh.boh.ui.Typewriter;
import poo.melitoh.boh.utils.SoundManager;
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
    private final AtomicBoolean animating = new AtomicBoolean(true);

    public Boh() {
        super();
        startFaceAnimation();
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
                currentFace = IDLE_FACES.get(index % IDLE_FACES.size());
                index++;
            }
        }, "Boh-FaceAnim");
        faceThread.start();
    }

    public void setMood(String mood) {
        // Face override logic could go here
    }

    public void say(String text) {
        Typewriter tw = new Typewriter(text, s -> {
            currentSpeech = s;
        }, c -> SoundManager.playTypingSound(c), 50); // Added speed param
        new Thread(tw, "Typewriter-Boh").start();
    }

    public String getCurrentFace() {
        return currentFace;
    }

    public String getCurrentSpeech() {
        return currentSpeech;
    }

    @Override
    public void updateState() {
        // Atualizações por frame se necessário
    }

    public void cleanup() {
        animating.set(false);
    }
}
