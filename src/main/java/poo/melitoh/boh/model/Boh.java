package poo.melitoh.boh.model;

import com.googlecode.lanterna.gui2.Label;
import poo.melitoh.boh.ui.Typewriter;
import poo.melitoh.boh.utils.SoundManager;

/**
 * Personagem principal Boh.
 */
public class Boh extends Actor {
    private final Label faceLabel;
    private final Label speechLabel;
    private volatile String currentSpeech = "";

    public Boh() {
        super();
        faceLabel = new Label("(._.)");
        speechLabel = new Label("");
        panel.addComponent(faceLabel);
        panel.addComponent(speechLabel);
    }

    public void setMood(String mood) {
        faceLabel.setText(mood);
    }

    public void say(String text) {
        Typewriter tw = new Typewriter(text, s -> {
            currentSpeech = s;
            speechLabel.setText(s);
        }, c -> SoundManager.playTypingSound(c));
        new Thread(tw, "Typewriter-Boh").start();
    }

    public String getCurrentSpeech() {
        return currentSpeech;
    }

    @Override
    public void updateState() {
        // Atualizações por frame (anim. de face, blink, etc.)
        // serão aplicadas aqui.
    }
}
