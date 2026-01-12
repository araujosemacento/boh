package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de introdução simples.
 */
public class IntroScript implements StageScript {
    private final String[] lines = { "Oi, tudo bem?", "Me chamo Boh."
    };

    @Override
    public void execute(Director director) {
        // Execução básica: dispara a fala do Boh
        // (sem sincronização avançada aqui)
        if (director != null && director.getBoh() != null) {
            director.getBoh().say(lines[0]);
        }
    }
}
