package poo.melitoh.boh.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Typewriter: anima texto a cada caracter em thread separada. Recebe callback
 * para atualizar a UI e tocar som por caractere.
 */
public class Typewriter implements Runnable {
    private final String text;
    private final Consumer<String> onUpdate;
    private final Consumer<Character> onChar;
    private final Supplier<Boolean> shouldRun;
    private final Supplier<Long> delaySupplier;
    private final Consumer<Boolean> onActivityChange;

    public Typewriter(String text, Consumer<String> onUpdate, Consumer<Character> onChar,
            Supplier<Boolean> shouldRun, Supplier<Long> delaySupplier,
            Consumer<Boolean> onActivityChange) {
        this.text = text == null ? "" : text;
        this.onUpdate = onUpdate;
        this.onChar = onChar;
        this.shouldRun = shouldRun != null ? shouldRun : () -> true;
        this.delaySupplier = delaySupplier != null ? delaySupplier : () -> 50L;
        this.onActivityChange = onActivityChange != null ? onActivityChange : (b) -> {
        };
    }

    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar, long fixedDelay) {
        this(text, onUpdate, onChar, () -> true, () -> fixedDelay, null);
    }

    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar) {
        this(text, onUpdate, onChar, 50);
    }

    @Override
    public void run() {
        StringBuilder buffer = new StringBuilder();
        char[] chars = text.toCharArray();

        onActivityChange.accept(true);

        for (int i = 0; i < chars.length; i++) {
            // Pause handling
            while (!shouldRun.get()) {
                onActivityChange.accept(false);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            onActivityChange.accept(true); // Resume active state

            char c = chars[i];
            buffer.append(c);
            onUpdate.accept(buffer.toString());
            if (onChar != null)
                onChar.accept(c);

            try {
                Thread.sleep(delaySupplier.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        onActivityChange.accept(false);
    }
}
