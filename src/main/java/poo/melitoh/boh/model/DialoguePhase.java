package poo.melitoh.boh.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa uma fase do diálogo contendo múltiplas linhas e metadados.
 */
public class DialoguePhase {
    private String phaseId;
    private String phaseName;
    private List<DialogueLine> lines;
    private Map<String, List<String>> asciiArt;
    private List<String> finalArt;

    public DialoguePhase() {
        this.lines = new ArrayList<>();
        this.asciiArt = new HashMap<>();
    }

    public String getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(String phaseId) {
        this.phaseId = phaseId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public List<DialogueLine> getLines() {
        return lines;
    }

    public void setLines(List<DialogueLine> lines) {
        this.lines = lines;
    }

    public Map<String, List<String>> getAsciiArt() {
        return asciiArt;
    }

    public void setAsciiArt(Map<String, List<String>> asciiArt) {
        this.asciiArt = asciiArt;
    }

    public List<String> getFinalArt() {
        return finalArt;
    }

    public void setFinalArt(List<String> finalArt) {
        this.finalArt = finalArt;
    }

    /**
     * Retorna a arte ASCII pelo nome, se definida nesta fase.
     */
    public String getAsciiArtByName(String name) {
        if (asciiArt == null || !asciiArt.containsKey(name)) {
            return null;
        }
        return String.join("\n", asciiArt.get(name));
    }

    /**
     * Retorna a quantidade de linhas de diálogo nesta fase.
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
     * Verifica se esta fase tem arte final para exibir.
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
