package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de introdução da lista - pra ser implementado, carregando diálogos do
 * arquivo phase2_list_intro.json.
 */
public class ListIntroScript extends JsonBasedScript {

    public ListIntroScript() {
        super("list_intro");
    }

    @Override
    protected void handleAction(Director director, String action) {
        switch (action) {
        case "WAIT_RESPONSE":
            System.out.println("[ListIntroScript] Aguardando resposta do usuário...");
            // TODO: Implementar espera de resposta S/N
            break;
        case "SHOW_LIST_WITH_ASK":
            System.out.println("[ListIntroScript] Mostrar lista com opções S/N");
            break;
        default:
            super.handleAction(director, action);
        }
    }
}
