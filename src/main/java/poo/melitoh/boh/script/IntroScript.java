package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;
import java.util.Arrays;
import java.util.List;

/**
 * Roteiro de introdução simples.
 */
public class IntroScript implements StageScript {
    private final List<String> lines = Arrays.asList("Oi, tudo bem?", "Eu me chamo Boh!",
            "Obrigado por executar meu programa.");

    @Override
    public void execute(Director director) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Initial delay like in Proto

                for (String line : lines) {
                    if (director.getBoh() != null) {
                        director.getBoh().say(line);

                        // Wait for typing to finish (approximate)
                        // Typewriter speed is 50ms/char
                        int typingDuration = line.length() * 50;
                        int readingPause = 1500;

                        Thread.sleep(typingDuration + readingPause);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Script-Intro").start();
    }
}
