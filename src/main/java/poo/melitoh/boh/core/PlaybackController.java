package poo.melitoh.boh.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PlaybackController: gerencia o estado da reprodução (play/pause, velocidade).
 * Exibe atributos volatile para leitura thread-safe sem bloqueio excessivo.
 */
public class PlaybackController {
    private final AtomicBoolean playing = new AtomicBoolean(true);
    private final AtomicLong charDelay = new AtomicLong(50); // Default speed (ms per char)

    public boolean isPlaying() {
        return playing.get();
    }

    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    public void togglePlayPause() {
        playing.set(!playing.get());
    }

    public long getCharDelay() {
        return charDelay.get();
    }

    public void setCharDelay(long delay) {
        if (delay < 5) delay = 5; // Proteção contra velocidade infinita
        if (delay > 500) delay = 500;
        this.charDelay.set(delay);
    }
    
    public void increaseSpeed() {
        setCharDelay(getCharDelay() - 10);
    }

    public void decreaseSpeed() {
        setCharDelay(getCharDelay() + 10);
    }
}
