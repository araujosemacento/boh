package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;
import poo.melitoh.boh.core.PlaybackController;
import poo.melitoh.boh.model.DialogueLine;
import poo.melitoh.boh.model.DialoguePhase;
import poo.melitoh.boh.utils.DialogueLoader;

/**
 * Script base que carrega diálogos de arquivos JSON. Subclasses podem
 * sobrescrever métodos para comportamentos específicos.
 */
public class JsonBasedScript implements StageScript {
    protected final String phaseId;
    protected DialoguePhase phase;

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

        PlaybackController pc = director.getPlaybackController();
        pc.setTotalLines(phase.getLineCount());
        pc.setCurrentLineIndex(0);

        new Thread(() -> {
            try {
                Thread.sleep(500); // Delay inicial

                int i = 0;
                while (i < phase.getLineCount()) {
                    pc.setCurrentLineIndex(i);
                    DialogueLine line = phase.getLine(i);

                    // Limpa texto estático anterior se não houver novo
                    if (!line.hasStaticText()) {
                        director.getBoh().clearStaticText();
                    } else {
                        // Define texto estático
                        director.getBoh().setStaticText(line.getStatic());
                    }

                    // Processa ações especiais antes da fala
                    if (line.hasAction()) {
                        handleAction(director, line.getAction());
                    }

                    // Define expressão
                    if (director.getBoh() != null) {
                        director.getBoh().setMood(line.getExpression());
                    }

                    // Executa a fala
                    String text = line.getText();
                    Thread sayThread = null;

                    if (text != null && !text.trim().isEmpty() && !text.equals(" ")) {
                        if (director.getBoh() != null) {
                            sayThread = director.getBoh().say(text);
                        }
                    }

                    // Aguarda a fala terminar, verificando navegação
                    boolean skipped = false;
                    boolean goBack = false;

                    if (sayThread != null) {
                        while (sayThread.isAlive()) {
                            // Verifica se usuário quer pular
                            if (pc.consumeSkipRequest()) {
                                director.getBoh().interruptSpeech();
                                skipped = true;
                                break;
                            }
                            // Verifica se usuário quer voltar
                            if (pc.consumePreviousRequest()) {
                                director.getBoh().interruptSpeech();
                                goBack = true;
                                break;
                            }
                            Thread.sleep(50);
                        }
                    }

                    // Navegação
                    if (goBack) {
                        i = Math.max(0, i - 1);
                        continue;
                    }

                    if (skipped) {
                        i++;
                        continue;
                    }

                    // Pausa após a fala (verificando navegação)
                    long pauseMs = (long) (line.getPauseAfter() * 1000);
                    long elapsed = 0;
                    while (elapsed < pauseMs) {
                        if (pc.consumeSkipRequest()) {
                            break;
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

                    i++;
                }

                // Limpa estado ao finalizar
                director.getBoh().clearStaticText();

                // Arte final, se houver
                if (phase.hasFinalArt()) {
                    handleFinalArt(director, phase.getFinalArtAsString());
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Script-" + phaseId).start();
    }

    /**
     * Processa ações especiais. Subclasses podem sobrescrever.
     */
    protected void handleAction(Director director, String action) {
        // Implementação padrão vazia - subclasses podem sobrescrever
        System.out.println("[Script] Ação: " + action);
    }

    /**
     * Processa arte final. Subclasses podem sobrescrever.
     */
    protected void handleFinalArt(Director director, String art) {
        // Implementação padrão - imprime no console
        System.out.println(art);
    }

    /**
     * Retorna a fase carregada para acesso por subclasses.
     */
    protected DialoguePhase getPhase() {
        return phase;
    }
}
