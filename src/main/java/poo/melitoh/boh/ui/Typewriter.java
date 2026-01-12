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

    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar) {
        this.text = text == null ? "" : text;
        this.onUpdate = onUpdate;
        this.onChar = onChar;
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
                Thread.sleep(10); // atraso padrão
                // preciso entender como parametrizar isso
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
