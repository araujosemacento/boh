package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;
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

        new Thread(() -> {
            try {
                Thread.sleep(500); // Delay inicial

                for (int i = 0; i < phase.getLineCount(); i++) {
                    DialogueLine line = phase.getLine(i);

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
                    if (text != null && !text.trim().isEmpty() && !text.equals(" ")) {
                        if (director.getBoh() != null) {
                            Thread sayThread = director.getBoh().say(text);
                            try {
                                sayThread.join();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                    }

                    // Pausa após a fala
                    long pauseMs = (long) (line.getPauseAfter() * 1000);
                    if (pauseMs > 0) {
                        Thread.sleep(pauseMs);
                    }
                }

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
