package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Roteiro da fase de introdução (Fase 1). A única implementação concreta, por
 * conta do tempo.
 * <p>
 * Carrega a fase "intro" (mapeada pra {@code phase1_intro.json}) e define o
 * comportamento específico pras ações dessa fase.
 * <p>
 * Responsabilidades: <br>
 * - Iniciar a apresentação do Boh. <br>
 * - Lidar com ações como solicitar o nome do usuário (ASK_NAME) e mostrar a
 * lista inicial (SHOW_LIST), apesar de no momento estas ações serem só stubs
 * logados no console.
 * <p>
 * É aqui que a mágica começa.
 */
public class IntroScript extends JsonBasedScript {

    /**
     * Construtor padrão da Intro.
     * <p>
     * Chama o super com o ID "intro".
     */
    public IntroScript() {
        super("intro");
    }

    @Override
    protected void handleAction(Director director, String action) {
        switch (action) {
        case "ASK_NAME":
            // Aqui iria solicitar entrada do usuário pra fazer ele pensar que o
            // nome seria armazenado e interromper ele antes de terminar de
            // digitar
            // TODO: Precisa integrar com componente de InputDialog de verdade
            System.out.println("[IntroScript] Ação: Solicitar nome do usuário");
            break;
        case "SHOW_LIST":
            // Aqui iria mostrar a representação visual da lista na UI
            // TODO: Acionar visualização de estruturas de dados
            System.out.println("[IntroScript] Ação: Mostrar lista");
            break;
        default:
            super.handleAction(director, action);
        }
    }
}
