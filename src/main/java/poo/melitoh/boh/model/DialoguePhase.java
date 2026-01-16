package poo.melitoh.boh.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa uma fase completa do diálogo contendo múltiplas falas e recursos.
 * <p>
 * Cada fase corresponde a um "capítulo" da apresentação, agrupando as linhas de
 * diálogo relacionadas a um tema específico (introdução, ponteiros, travessia,
 * etc.). Além das falas, a fase pode conter arte ASCII (do AUX) e recursos
 * visuais associados.
 * <p>
 * Os dados são carregados dos arquivos JSON (ex: phase1_intro.json) pelo
 * {@link poo.melitoh.boh.utils.DialogueLoader} e consumidos pelos scripts.
 * <p>
 * Estrutura: <br>
 * - {@code phaseId}: Identificador único da fase (ex: "intro"). <br>
 * - {@code phaseName}: Nome legível da fase. <br>
 * - {@code lines}: Lista de {@link poo.melitoh.boh.model.DialogueLine} a serem
 * exibidas. <br>
 * - {@code asciiArt}: Mapa das artes ASCII disponíveis na fase. <br>
 * - {@code finalArt}: Arte exibida no final da última fase da conversa. <br>
 */
public class DialoguePhase {
    /** Identificador único da fase. */
    private String phaseId;

    /** Nome legível da fase. */
    private String phaseName;

    /** Lista de linhas de diálogo da fase. */
    private List<DialogueLine> lines;

    /** Mapa de artes ASCII indexadas por nome. */
    private Map<String, List<String>> asciiArt;

    /** Arte exibida ao final da fase. */
    private List<String> finalArt;

    /**
     * Construtor padrão que inicializa as coleções vazias.
     */
    public DialoguePhase() {
        this.lines = new ArrayList<>();
        this.asciiArt = new HashMap<>();
    }

    /**
     * Retorna o identificador da fase.
     *
     * @return ID da fase (ex: "intro", "pointers").
     */
    public String getPhaseId() {
        return phaseId;
    }

    /**
     * Define o identificador da fase.
     *
     * @param phaseId ID único da fase.
     */
    public void setPhaseId(String phaseId) {
        this.phaseId = phaseId;
    }

    /**
     * Retorna o nome legível da fase.
     *
     * @return Nome da fase pra exibição.
     */
    public String getPhaseName() {
        return phaseName;
    }

    /**
     * Define o nome legível da fase.
     *
     * @param phaseName Nome da fase.
     */
    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    /**
     * Retorna a lista de linhas de diálogo.
     *
     * @return Lista de {@link DialogueLine}.
     */
    public List<DialogueLine> getLines() {
        return lines;
    }

    /**
     * Define a lista de linhas de diálogo.
     *
     * @param lines Nova lista de linhas.
     */
    public void setLines(List<DialogueLine> lines) {
        this.lines = lines;
    }

    /**
     * Retorna o mapa de artes ASCII.
     *
     * @return Mapa de nome pra lista de linhas da arte.
     */
    public Map<String, List<String>> getAsciiArt() {
        return asciiArt;
    }

    /**
     * Define o mapa de artes ASCII.
     *
     * @param asciiArt Novo mapa de artes.
     */
    public void setAsciiArt(Map<String, List<String>> asciiArt) {
        this.asciiArt = asciiArt;
    }

    /**
     * Retorna a arte final da fase.
     *
     * @return Lista de linhas da arte final.
     */
    public List<String> getFinalArt() {
        return finalArt;
    }

    /**
     * Define a arte final da fase.
     *
     * @param finalArt Lista de linhas da arte.
     */
    public void setFinalArt(List<String> finalArt) {
        this.finalArt = finalArt;
    }

    /**
     * Retorna a arte ASCII pelo nome, se definida nessa fase.
     */
    public String getAsciiArtByName(String name) {
        if (asciiArt == null || !asciiArt.containsKey(name)) {
            return null;
        }
        return String.join("\n", asciiArt.get(name));
    }

    /**
     * Retorna a quantidade de linhas de diálogo nessa fase.
     */
    public int getLineCount() {
        return lines != null ? lines.size() : 0;
    }

    /**
     * Retorna uma linha de diálogo pelo índice.
     */
    public DialogueLine getLine(int index) {
        if (lines == null || index < 0 || index >= lines.size()) {
            return null;
        }
        return lines.get(index);
    }

    /**
     * Verifica se essa fase tem arte final para exibir.
     */
    public boolean hasFinalArt() {
        return finalArt != null && !finalArt.isEmpty();
    }

    /**
     * Retorna a arte final como uma única string.
     */
    public String getFinalArtAsString() {
        if (finalArt == null) {
            return "";
        }
        return String.join("\n", finalArt);
    }
}
