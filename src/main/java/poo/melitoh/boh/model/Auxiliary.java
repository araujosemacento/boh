package poo.melitoh.boh.model;

import com.googlecode.lanterna.gui2.Label;

/**
 * Personagem "coadjuvante" projetado pra representar uma variável auxiliar no
 * algoritmo de inversão de uam lista encadeada.
 * <p>
 * O Aux seria o "sidekick" do Boh, com a função de ajudar na compreensão visual
 * das operações de inversão da lista. Ele representaria a variável auxiliar
 * usada durante a manipulação de ponteiros.
 * <p>
 * Status atual: {@code [RIP]} - Não implementado além da estrutura básica. A
 * classe existe como placeholder pra alguma implementação futura que possa ser
 * adicionada com as animações de ponteiros e manipulação visual dos nós.
 * <p>
 * Componentes: <br>
 * - {@code content}: Label Lanterna pra exibir texto estático ou representação
 * de dados. <br>
 */
public class Auxiliary extends Actor {
    /** Label pra exibição de conteúdo textual. */
    private final Label content;

    /**
     * Construtor padrão que inicializa o label vazio.
     * <p>
     * Adiciona o componente de texto ao painel herdado de
     * {@link poo.melitoh.boh.model.Actor}.
     */
    public Auxiliary() {
        super();
        content = new Label("");
        panel.addComponent(content);
    }

    /**
     * Define o conteúdo textual exibido pelo auxiliar.
     *
     * @param s Texto a ser exibido na mão do AUX enqaunto ele "segura", ou
     *          seja, guarda o um valor. (pode representar um valor de variável,
     *          endereço de ponteiro ou estrutura de dados).
     */
    public void setContent(String s) {
        content.setText(s);
    }

    /**
     * Atualizaria o estado.
     * <p>
     * Sem comportamento implementado no momento. Futuras versões poderiam
     * adicionar as animações ccorretas.
     */
    @Override
    public void updateState() {
        // Sem comportamento por padrão
    }
}
