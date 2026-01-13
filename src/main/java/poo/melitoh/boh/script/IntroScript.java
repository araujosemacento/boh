package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de introdução - carrega diálogos do arquivo JSON phase1_intro.json.
 */
public class IntroScript extends JsonBasedScript {

    public IntroScript() {
        super("intro");
    }

    @Override
    protected void handleAction(Director director, String action) {
        switch (action) {
        case "ASK_NAME":
            // Aqui poderia solicitar entrada do usuário
            System.out.println("[IntroScript] Ação: Solicitar nome do usuário");
            break;
        case "SHOW_LIST":
            // Aqui poderia mostrar a representação visual da lista
            System.out.println("[IntroScript] Ação: Mostrar lista");
            break;
        default:
            super.handleAction(director, action);
        }
    }
}
