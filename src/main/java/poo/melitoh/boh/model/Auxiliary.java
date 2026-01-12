package poo.melitoh.boh.model;

import com.googlecode.lanterna.gui2.Label;

/**
 * AUX (conteúdo estático ou lista).
 */
public class Auxiliary extends Actor {
    private final Label content;

    public Auxiliary() {
        super();
        content = new Label("");
        panel.addComponent(content);
    }

    public void setContent(String s) {
        content.setText(s);
    }

    @Override
    public void updateState() {
        // Sem comportamento por padrão
    }
}
