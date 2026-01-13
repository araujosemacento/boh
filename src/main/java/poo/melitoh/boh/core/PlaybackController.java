package poo.melitoh.boh.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PlaybackController: gerencia o estado da reprodução (play/pause, velocidade,
 * navegação). Exibe atributos volatile para leitura thread-safe sem bloqueio
 * excessivo.
 */
public class PlaybackController {
    private final AtomicBoolean playing = new AtomicBoolean(true);
    private final AtomicLong charDelay = new AtomicLong(50); // Default speed
                                                             // (ms per char)

    // Navegação entre falas
    private final AtomicBoolean skipRequested = new AtomicBoolean(false);
    private final AtomicBoolean previousRequested = new AtomicBoolean(false);
    private final AtomicInteger currentLineIndex = new AtomicInteger(0);
    private volatile int totalLines = 0;

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
        if (delay < 5)
            delay = 5; // Proteção contra velocidade infinita
        if (delay > 500)
            delay = 500;
        this.charDelay.set(delay);
    }

    public void increaseSpeed() {
        setCharDelay(getCharDelay() - 10);
    }

    public void decreaseSpeed() {
        setCharDelay(getCharDelay() + 10);
    }

    // --- Navegação entre falas ---

    /**
     * Solicita pular para a próxima fala.
     */
    public void requestSkip() {
        skipRequested.set(true);
    }

    /**
     * Solicita voltar para a fala anterior.
     */
    public void requestPrevious() {
        previousRequested.set(true);
    }

    /**
     * Verifica e consome a solicitação de pular.
     * 
     * @return true se foi solicitado pular
     */
    public boolean consumeSkipRequest() {
        return skipRequested.getAndSet(false);
    }

    /**
     * Verifica e consome a solicitação de voltar.
     * 
     * @return true se foi solicitado voltar
     */
    public boolean consumePreviousRequest() {
        return previousRequested.getAndSet(false);
    }

    /**
     * Define o índice da linha atual.
     */
    public void setCurrentLineIndex(int index) {
        currentLineIndex.set(index);
    }

    /**
     * Retorna o índice da linha atual.
     */
    public int getCurrentLineIndex() {
        return currentLineIndex.get();
    }

    /**
     * Define o total de linhas do script atual.
     */
    public void setTotalLines(int total) {
        this.totalLines = total;
    }

    /**
     * Retorna o total de linhas.
     */
    public int getTotalLines() {
        return totalLines;
    }

    /**
     * Verifica se há próxima linha.
     */
    public boolean hasNextLine() {
        return currentLineIndex.get() < totalLines - 1;
    }

    /**
     * Verifica se há linha anterior.
     */
    public boolean hasPreviousLine() {
        return currentLineIndex.get() > 0;
    }
}
