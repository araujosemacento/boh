package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de introdução do AUX - pra ser implementado, carregando diálogos do
 * arquivo phase3_aux_intro.json.
 */
public class AuxIntroScript extends JsonBasedScript {

    public AuxIntroScript() {
        super("aux_intro");
    }

    @Override
    protected void handleAction(Director director, String action) {
        // AUX não tem ações especiais nessa fase
        super.handleAction(director, action);
    }
}
