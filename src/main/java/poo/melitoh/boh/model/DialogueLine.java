package poo.melitoh.boh.model;

/**
 * Representa uma linha de diálogo individual com metadados de exibição.
 */
public class DialogueLine {
    private String text;
    private String expression;
    private double pauseAfter;
    private String staticText;
    private String ascii;
    private String action;
    private boolean colorizeArrows;

    public DialogueLine() {
        this.expression = "idle";
        this.pauseAfter = 1.0;
        this.colorizeArrows = false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public double getPauseAfter() {
        return pauseAfter;
    }

    public void setPauseAfter(double pauseAfter) {
        this.pauseAfter = pauseAfter;
    }

    public String getStatic() {
        return staticText;
    }

    public void setStatic(String staticText) {
        this.staticText = staticText;
    }

    public String getAscii() {
        return ascii;
    }

    public void setAscii(String ascii) {
        this.ascii = ascii;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isColorizeArrows() {
        return colorizeArrows;
    }

    public void setColorizeArrows(boolean colorizeArrows) {
        this.colorizeArrows = colorizeArrows;
    }

    /**
     * Verifica se esta linha tem texto estático para exibir.
     */
    public boolean hasStaticText() {
        return staticText != null && !staticText.isEmpty();
    }

    /**
     * Verifica se esta linha requer exibição de arte ASCII.
     */
    public boolean hasAsciiArt() {
        return ascii != null && !ascii.isEmpty();
    }

    /**
     * Verifica se esta linha dispara uma ação especial.
     */
    public boolean hasAction() {
        return action != null && !action.isEmpty();
    }
}
