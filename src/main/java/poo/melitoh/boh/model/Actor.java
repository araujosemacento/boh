package poo.melitoh.boh.model;

import com.googlecode.lanterna.gui2.Panel;

/**
 * Actor: classe abstrata base para personagens/elementos
 * na tela. Guarda um painel (Lanterna) e estado básico.
 */
public abstract class Actor {
    protected final Panel panel;
    protected int x, y;
    protected volatile boolean visible = true;

    public Actor() {
        this.panel = new Panel();
        this.x = 0;
        this.y = 0;
    }

    public Panel getPanel() {
        return panel;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public abstract void updateState();
}
