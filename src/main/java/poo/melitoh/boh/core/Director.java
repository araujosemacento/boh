package poo.melitoh.boh.core;

import java.util.ArrayList;
import java.util.List;
import poo.melitoh.boh.model.Actor;
import poo.melitoh.boh.model.Boh;
import poo.melitoh.boh.model.Auxiliary;
import poo.melitoh.boh.script.StageScript;

/**
 * Director: controla atores e coordena roteiros.
 */
public class Director {
    private final List<Actor> actors = new ArrayList<>();
    private final Boh boh;
    private final Auxiliary aux;
    private final PlaybackController playbackController;

    public Director() {
        playbackController = new PlaybackController();
        boh = new Boh();
        boh.setPlaybackController(playbackController);
        aux = new Auxiliary();
        actors.add(boh);
        actors.add(aux);
    }

    public PlaybackController getPlaybackController() {
        return playbackController;
    }

    public Boh getBoh() {
        return boh;
    }

    public Auxiliary getAux() {
        return aux;
    }

    public void loadScript(StageScript script) {
        if (script != null)
            script.execute(this);
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void updateAll() {
        for (Actor a : actors)
            a.updateState();
    }
}
