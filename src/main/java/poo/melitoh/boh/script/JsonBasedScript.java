package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;
import poo.melitoh.boh.core.PlaybackController;
import poo.melitoh.boh.model.DialogueLine;
import poo.melitoh.boh.model.DialoguePhase;
import poo.melitoh.boh.utils.DialogueLoader;

/**
 * Script base que carrega e executa os diálogos definidos em arquivos JSON.
 * <p>
 * Essa classe concreta de {@link poo.melitoh.boh.script.StageScript} automatiza
 * a "atuação" do Boh lendo sequencialmente as falas de um
 * {@link poo.melitoh.boh.model.DialoguePhase}. Ela é projetada pra tirar o peso
 * das subclasses, que só precisam dizer qual arquivo carregar e (se pá) como
 * lidar com ações especiais.
 * <p>
 * Funcionalidades: <br>
 * - Carregamento: Busca o JSON via {@link poo.melitoh.boh.utils.DialogueLoader}
 * usando o ID fornecido. <br>
 * - Loop de Execução: Itera sobre as
 * {@link poo.melitoh.boh.model.DialogueLine}, atualizando o estado do Boh
 * (expressão, texto estático) e comandando a fala. <br>
 * - Sincronia: Aguarda o término da fala (respeitando a animação typewriter)
 * antes de prosseguir, e processa pausas definidas no JSON. <br>
 * - Controle de Fluxo: Integra {@link poo.melitoh.boh.core.PlaybackController}
 * pra permitir que o usuário pule falas ou volte pra anterior.
 * <p>
 * Sumariamente falando, torna a parte estática em comportamento dinâmico.
 */
public class JsonBasedScript implements StageScript {
    /** ID da fase que vai ser carregada (ex: "intro", "list_intro"). */
    protected final String phaseId;
    /** Objeto de dados contendo todas as falas da fase carregada. */
    protected DialoguePhase phase;

    /**
     * Cria um novo script baseado em JSON.
     *
     * @param phaseId Identificador da fase, que é resolvido pelo
     *                {@link poo.melitoh.boh.utils.DialogueLoader}.
     */
    public JsonBasedScript(String phaseId) {
        this.phaseId = phaseId;
    }

    @Override
    public void execute(Director director) {
        phase = DialogueLoader.loadPhaseById(phaseId);
        if (phase == null) {
            System.err.println("Fase não carregada: " + phaseId);
            return;
        }

        // Configura o controlador pra saber quantas linhas temos
        PlaybackController pc = director.getPlaybackController();
        pc.setTotalLines(phase.getLineCount());
        pc.setCurrentLineIndex(0);

        // Roda o script numa thread separada pra não bloquear a renderização
        new Thread(() -> {
            try {
                Thread.sleep(500); // Delay dramático inicial

                int i = 0;
                while (i < phase.getLineCount()) {
                    pc.setCurrentLineIndex(i);
                    DialogueLine line = phase.getLine(i);

                    // --- Configuração Visual ---
                    // Limpa texto estático anterior se não houver novo
                    if (!line.hasStaticText()) {
                        director.getBoh().clearStaticText();
                    } else {
                        // Define texto estático (ex: visualização de lista)
                        director.getBoh().setStaticText(line.getStatic());
                    }

                    // --- Ações Especiais ---
                    // Processa gatilhos de lógica (ex: mostrar input, trocar
                    // cena)
                    if (line.hasAction()) {
                        handleAction(director, line.getAction());
                    }

                    // --- Atuação ---
                    // Define a expressão facial do Boh pra essa fala
                    if (director.getBoh() != null) {
                        director.getBoh().setMood(line.getExpression());
                    }

                    // Executa a fala com efeito typewriter
                    String text = line.getText();
                    Thread sayThread = null;

                    if (text != null && !text.trim().isEmpty() && !text.equals(" ")) {
                        if (director.getBoh() != null) {
                            sayThread = director.getBoh().say(text);
                        }
                    }

                    // --- Sincronização e Input ---
                    // Aguarda a fala terminar, verificando se o usuário quer
                    // pular/voltar
                    boolean skipped = false;
                    boolean goBack = false;

                    if (sayThread != null) {
                        while (sayThread.isAlive()) {
                            // Verifica se usuário quer pular (avançar rápido)
                            if (pc.consumeSkipRequest()) {
                                director.getBoh().interruptSpeech();
                                skipped = true;
                                break;
                            }
                            // Verifica se usuário quer voltar pra anterior
                            if (pc.consumePreviousRequest()) {
                                director.getBoh().interruptSpeech();
                                goBack = true;
                                break;
                            }
                            Thread.sleep(50);
                        }
                    }

                    // Trata navegação solicitada durante a fala
                    if (goBack) {
                        i = Math.max(0, i - 1);
                        continue;
                    }

                    if (skipped) {
                        i++;
                        continue;
                    }

                    // --- Pausa Dramática ---
                    // Pausa depois da fala terminar naturalmente (verificando
                    // os inputs)
                    long pauseMs = (long) (line.getPauseAfter() * 1000);
                    long elapsed = 0;
                    while (elapsed < pauseMs) {
                        if (pc.consumeSkipRequest()) {
                            break; // Pula a pausa
                        }
                        if (pc.consumePreviousRequest()) {
                            i = Math.max(0, i - 1);
                            goBack = true;
                            break;
                        }
                        Thread.sleep(50);
                        elapsed += 50;
                    }

                    if (goBack) {
                        continue;
                    }

                    i++; // Próxima linha
                }

                // --- Finalização ---
                // Limpa estado ao completar a fase
                director.getBoh().clearStaticText();

                // Exibe "arte final" (ASCII art ou mensagem de conclusão)
                if (phase.hasFinalArt()) {
                    handleFinalArt(director, phase.getFinalArtAsString());
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Script-" + phaseId).start();
    }

    /**
     * Gancho (hook) pra processar as ações especiais definidas no JSON.
     * <p>
     * As subclasses tem de sobrescrever esse método se a fase tiver ações
     * lógicas (como "ASK_NAME" ou "SWAP_NODES"). A implementação padrão só loga
     * a ação.
     *
     * @param director Acesso ao diretor pra manipular a cena.
     * @param action   String identificadora da ação (vem do JSON).
     */
    protected void handleAction(Director director, String action) {
        // Implementação padrão vazia - subclasses podem sobrescrever
        System.out.println("[Script] Ação: " + action);
    }

    /**
     * Gancho (hook) pra lidar com a arte final da fase.
     * <p>
     * A implementação padrão só imprime no console, mas podia renderizar na
     * tela.
     *
     * @param director Acesso ao diretor.
     * @param art      String com a arte/mensagem final.
     */
    protected void handleFinalArt(Director director, String art) {
        // Implementação padrão - imprime no console
        System.out.println(art);
    }

    /**
     * Retorna a fase carregada pra acesso por subclasses.
     *
     * @return O objeto {@link poo.melitoh.boh.script.DialoguePhase} atual.
     */
    protected DialoguePhase getPhase() {
        return phase;
    }
}
