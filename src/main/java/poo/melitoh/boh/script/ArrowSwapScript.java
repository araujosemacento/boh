package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro de inversão das setas - carrega diálogos do arquivo
 * phase5_arrow_swap.json.
 */
public class ArrowSwapScript extends JsonBasedScript {

    public ArrowSwapScript() {
        super("arrow_swap");
    }

    @Override
    protected void handleAction(Director director, String action) {
        switch (action) {
        case "WAIT_RESPONSE":
            System.out.println("[ArrowSwapScript] Aguardando resposta do usuário...");
            break;
        default:
            super.handleAction(director, action);
        }
    }
}
