package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de conclusão - pra ser implementado, carregando diálogos do arquivo
 * phase7_conclusion.json.
 */
public class ConclusionScript extends JsonBasedScript {

    public ConclusionScript() {
        super("conclusion");
    }

    @Override
    protected void handleFinalArt(Director director, String art) {
        // Exibe a arte final de despedida, ainda não implementada
        System.out.println(art);
    }
}
