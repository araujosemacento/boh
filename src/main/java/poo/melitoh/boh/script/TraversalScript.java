package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de travessia da lista - carrega diálogos do arquivo
 * phase6_traversal.json.
 */
public class TraversalScript extends JsonBasedScript {

    public TraversalScript() {
        super("traversal");
    }

    @Override
    protected void handleAction(Director director, String action) {
        // Travessia não tem ações especiais
        super.handleAction(director, action);
    }
}
