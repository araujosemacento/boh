package poo.melitoh.boh.core;

import java.util.ArrayList;
import java.util.List;
import poo.melitoh.boh.model.Actor;
import poo.melitoh.boh.model.Boh;
import poo.melitoh.boh.model.Auxiliary;
import poo.melitoh.boh.script.StageScript;

/**
 * Projetado pra ser meio que realmente um diretor de cena, controlando atores e
 * coordenando os roteiros.
 * <p>
 * Essa classe concentra o estado dos atores e facilita a comunicação entre a
 * lógica de roteiro (os scripts) e o que o usuário vê na tela.
 * <p>
 * Componentes principais: <br>
 * - {@link poo.melitoh.boh.model.Boh} : Personagem principal. principal. <br>
 * - {@link poo.melitoh.boh.model.Auxiliary} : O sidekick, não implementado no
 * momento, que teria o papel de "atuar" a visualização da função de uma
 * variável auxiliar na inversão de uma lista. <br>
 * - {@link poo.melitoh.boh.core.PlaybackController} : Gerencia o fluxo de texto
 * e o estado da reprodução do diálogo. <br>
 */
public class Director {
    private final List<Actor> actors = new ArrayList<>();
    private final Boh boh;
    private final Auxiliary aux;
    private final PlaybackController playbackController;

    /**
     * Construtor padrão que inicializa os atores e o controlador de playback.
     * <p>
     * Aqui os atores básicos (Boh e Aux), são criados e registrados na lista
     * interna pra que o loop de atualização consiga processá-los quando
     * exibidos na tela.
     */
    public Director() {
        playbackController = new PlaybackController();
        boh = new Boh();
        boh.setPlaybackController(playbackController);
        aux = new Auxiliary();
        actors.add(boh);
        actors.add(aux);
    }

    /**
     * Retorna o controlador de fala, usado pra checar se o Boh terminou de
     * "falar" ou pra pular o typewriter.
     *
     * @return O {@link poo.melitoh.boh.core.PlaybackController} ativo.
     */
    public PlaybackController getPlaybackController() {
        return playbackController;
    }

    /**
     * Acessa a instância do Boh pra manipular expressões faciais ou frases.
     *
     * @return A instância do {@link poo.melitoh.boh.model.Boh}.
     */
    public Boh getBoh() {
        return boh;
    }

    /**
     * Acessaria o AUX, {@code [RIP]}.
     *
     * @return A instância de {@link poo.melitoh.boh.model.Auxiliary}.
     */
    public Auxiliary getAux() {
        return aux;
    }

    /**
     * Carrega e executa um roteiro específico.
     * <p>
     * O roteiro recebe o próprio {@code Director} como contexto pra poder
     * interagir com os atores e o "cronômetro" de fala.
     *
     * @param script O {@link poo.melitoh.boh.script.StageScript} a ser
     *               executado, no momento, sendo esse o
     *               {@link poo.melitoh.boh.script.IntroScript}.
     */
    public void loadScript(StageScript script) {
        if (script != null)
            script.execute(this);
    }

    /**
     * Fornece a lista completa de atores registrados "no palco".
     *
     * @return Lista de {@link poo.melitoh.boh.model.Actor}.
     */
    public List<Actor> getActors() {
        return actors;
    }

    /**
     * Atualiza o estado interno de todos os atores.
     * <p>
     * Esse método é pra ser chamado, geralmente, dentro do loop de atualização
     * da interface, pra garantir que animações e mudanças de texto reflitam nos
     * atores em tempo de execução.
     */
    public void updateAll() {
        for (Actor a : actors)
            a.updateState();
    }
}
