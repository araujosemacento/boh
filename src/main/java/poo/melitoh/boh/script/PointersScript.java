package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de explicação dos ponteiros - carrega diálogos do arquivo
 * phase4_pointers.json.
 */
public class PointersScript extends JsonBasedScript {

    public PointersScript() {
        super("pointers");
    }

    @Override
    protected void handleAction(Director director, String action) {
        switch (action) {
        case "SHOW_ASK_TEMPLATE":
            System.out.println("[PointersScript] Mostrar template de pergunta S/N");
            break;
        default:
            super.handleAction(director, action);
        }
    }
}
