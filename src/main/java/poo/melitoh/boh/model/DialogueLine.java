package poo.melitoh.boh.model;

/**
 * Representa uma linha de diálogo individual com metadados de exibição.
 * <p>
 * Essa classe encapsula todos os dados necessários pra renderizar uma única
 * fala do Boh, incluindo o texto principal, expressão facial, tempo de pausa,
 * conteúdo estático adicional e ações especiais.
 * <p>
 * Os dados são tipicamente carregados de arquivos JSON através do
 * {@link poo.melitoh.boh.utils.DialogueLoader} do pacote {@code utils} e
 * consumidos pelos scripts das fases.
 * <p>
 * Atributos: <br>
 * - {@code text}: O texto falado pelo Boh (com códigos de formatação). <br>
 * - {@code expression}: Expressão facial durante a fala. <br>
 * - {@code pauseAfter}: Segundos de pausa após a fala. <br>
 * - {@code staticText}: Texto estático exibido ao lado. <br>
 * - {@code ascii}: Referencia a arte ASCII a ser exibida. <br>
 * - {@code action}: Ação especial a ser executada. <br>
 * - {@code colorizeArrows}: Se deve colorir setas no texto (presente no script
 * Python original). <br>
 */
public class DialogueLine {
    /** Texto principal da fala. */
    private String text;

    /** Expressão facial do Boh durante a fala. */
    private String expression;

    /** Tempo de pausa após a fala (em segundos). */
    private double pauseAfter;

    /** Texto estático exibido ao lado da fala. */
    private String staticText;

    /** Referência a arte ASCII definida na fase. */
    private String ascii;

    /** Ação especial a ser executada durante a fala. */
    private String action;

    /** Flag pra colorir as setas. */
    private boolean colorizeArrows;

    /**
     * Construtor padrão com valores iniciais padrçao.
     * <p>
     * Define expressão como "idle", pausa de 1 segundo e setas brancas.
     */
    public DialogueLine() {
        this.expression = "idle";
        this.pauseAfter = 1.0;
        this.colorizeArrows = false;
    }

    /**
     * Retorna o texto principal da fala.
     *
     * @return Texto com possíveis códigos de formatação (§b, §g, etc.).
     */
    public String getText() {
        return text;
    }

    /**
     * Define o texto principal da fala.
     *
     * @param text Texto a ser exibido pelo typewriter.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Retorna a expressão facial associada à fala.
     *
     * @return Identificador da expressão (ex: "idle", "happy").
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Define a expressão facial durante a fala.
     *
     * @param expression Identificador da expressão.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Retorna o tempo de pausa após a fala.
     *
     * @return Pausa em segundos.
     */
    public double getPauseAfter() {
        return pauseAfter;
    }

    /**
     * Define o tempo de pausa após a fala.
     *
     * @param pauseAfter Pausa em segundos.
     */
    public void setPauseAfter(double pauseAfter) {
        this.pauseAfter = pauseAfter;
    }

    /**
     * Retorna o texto estático associado à fala.
     *
     * @return Texto estático (código, exemplos) ou null.
     */
    public String getStatic() {
        return staticText;
    }

    /**
     * Define o texto estático exibido ao lado da fala.
     *
     * @param staticText Conteúdo estático.
     */
    public void setStatic(String staticText) {
        this.staticText = staticText;
    }

    /**
     * Retorna a referência à arte ASCII.
     *
     * @return Nome da arte definida na fase ou null.
     */
    public String getAscii() {
        return ascii;
    }

    /**
     * Define a referência à arte ASCII.
     *
     * @param ascii Nome da arte a ser exibida.
     */
    public void setAscii(String ascii) {
        this.ascii = ascii;
    }

    /**
     * Retorna a ação especial da fala.
     *
     * @return Identificador da ação ou null.
     */
    public String getAction() {
        return action;
    }

    /**
     * Define a ação especial a ser executada.
     *
     * @param action Identificador da ação.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Verifica se setas devem ser coloridas.
     *
     * @return {@code true} se colorização está ativa.
     */
    public boolean isColorizeArrows() {
        return colorizeArrows;
    }

    /**
     * Define se setas devem ser coloridas.
     *
     * @param colorizeArrows {@code true} pra ativar colorização.
     */
    public void setColorizeArrows(boolean colorizeArrows) {
        this.colorizeArrows = colorizeArrows;
    }

    /**
     * Verifica se a linha tem texto estático para exibir.
     */
    public boolean hasStaticText() {
        return staticText != null && !staticText.isEmpty();
    }

    /**
     * Verifica se a linha tem arte ASCII.
     */
    public boolean hasAsciiArt() {
        return ascii != null && !ascii.isEmpty();
    }

    /**
     * Verifica se a linha dispara uma ação especial.
     */
    public boolean hasAction() {
        return action != null && !action.isEmpty();
    }
}
