package poo.melitoh.boh.ui;

import java.util.function.Consumer;

/**
 * Typewriter: anima texto a cada caracter em thread separada. Recebe callback
 * para atualizar a UI e tocar som por caractere.
 */
public class Typewriter implements Runnable {
    private final String text;
    private final Consumer<String> onUpdate;
    private final java.util.function.Consumer<Character> onChar;
    private final long delay;

    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar, long delay) {
        this.text = text == null ? "" : text;
        this.onUpdate = onUpdate;
        this.onChar = onChar;
        this.delay = delay;
    }

    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar) {
        this(text, onUpdate, onChar, 10);
    }

    @Override
    public void run() {
        StringBuilder buffer = new StringBuilder();
        for (char c : text.toCharArray()) {
            buffer.append(c);
            onUpdate.accept(buffer.toString());
            if (onChar != null)
                onChar.accept(c);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
