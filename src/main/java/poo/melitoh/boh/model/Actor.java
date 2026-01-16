package poo.melitoh.boh.model;

import com.googlecode.lanterna.gui2.Panel;

/**
 * Tá presvista pra ser a classe abstrata base pra todos os personagens e
 * elementos visuais.
 * <p>
 * Define a estrutura comum que todo ator vai ter: um painel Lanterna pra
 * renderização (caso necessário), posição na tela e estado de visibilidade. As
 * subclasses como {@link poo.melitoh.boh.model.Boh} e
 * {@link poo.melitoh.boh.model.Auxiliary} estendem ela pra implementar
 * comportamentos específicos.
 * <p>
 * Atributos: <br>
 * - {@code panel}: Container Lanterna pra componentes visuais (legado). <br>
 * - {@code x, y}: Coordenadas de posição na tela. <br>
 * - {@code visible}: Flag volatile pra controle de visibilidade thread-safe.
 * <br>
 */
public abstract class Actor {
    /** Painel Lanterna associado ao ator (gui2 da lanterna). */
    protected final Panel panel;

    /** Coordenadas de posição na tela (coluna, linha). */
    protected int x, y;

    /** Flag de visibilidade, acessível pra mais de uma thread. */
    protected volatile boolean visible = true;

    /**
     * Construtor padrão que inicializa o painel e posição.
     * <p>
     * Cria um painel vazio e posiciona o ator na origem (0, 0).
     */
    public Actor() {
        this.panel = new Panel();
        this.x = 0;
        this.y = 0;
    }

    /**
     * Retorna o painel Lanterna associado ao ator.
     *
     * @return O {@link Panel} do ator.
     */
    public Panel getPanel() {
        return panel;
    }

    /**
     * Torna o ator visível na tela.
     */
    public void show() {
        visible = true;
    }

    /**
     * Esconde o ator da tela.
     */
    public void hide() {
        visible = false;
    }

    /**
     * Atualiza o estado interno do ator.
     * <p>
     * Método abstrato chamado pelo loop do
     * {@link poo.melitoh.boh.core.Director} pra dái cada ator na lista
     * processar sua lógica de animação e estado.
     */
    public abstract void updateState();
}
