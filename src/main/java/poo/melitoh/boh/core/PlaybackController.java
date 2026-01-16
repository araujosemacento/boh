package poo.melitoh.boh.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gerenciador do estado da reprodução de diálogos e animações.
 * <p>
 * Essa classe é responsável por controlar o fluxo de exibição do texto,
 * (play/pause), mudar a velocidade e navegar entre as falas. Utiliza tipos
 * {@code Atomic} pra garantir acesso thread-safe sem bloqueio excessivo, já que
 * o loop de render e a thread de typewriter acessam esses valores
 * simultaneamente.
 * <p>
 * Funcionalidades: <br>
 * - Play/Pause: alterna entre reprodução ativa e parada. <br>
 * - Velocidade: ajusta o delay por caractere no efeito typewriter. <br>
 * - Navegação: permite pular pra próxima fala ou voltar pra anterior. <br>
 * - Tracking: mantém o índice da linha atual e o total de linhas. <br>
 */
public class PlaybackController {
    /** Estado de reprodução: true = tocando, false = pausado. */
    private final AtomicBoolean playing = new AtomicBoolean(true);

    /** Delay em ms entre cada caractere na animação da fala. */
    private final AtomicLong charDelay = new AtomicLong(50);

    /** Flag pra requisição de pular pra próxima fala. */
    private final AtomicBoolean skipRequested = new AtomicBoolean(false);

    /** Flag pra requisição de voltar pra fala anterior. */
    private final AtomicBoolean previousRequested = new AtomicBoolean(false);

    /** Índice da linha de diálogo atual. */
    private final AtomicInteger currentLineIndex = new AtomicInteger(0);

    /** Total de linhas no script ativo. */
    private volatile int totalLines = 0;

    /**
     * Verifica se o diálo está sendo reproduzido.
     *
     * @return {@code true} se o diálogo está sendo reproduzido, {@code false}
     *         se pausado.
     */
    public boolean isPlaying() {
        return playing.get();
    }

    /**
     * Define o estado de reprodução.
     *
     * @param playing Muda para {@code true} pra reproduzir, {@code false} pra
     *                pausar.
     */
    public void setPlaying(boolean playing) {
        this.playing.set(playing);
    }

    /**
     * Pausa e despausa.
     * <p>
     * Útil pra vincular diretamente a uma tecla de atalho (no caso,
     * implementado como {@code SPACE}).
     */
    public void togglePlayPause() {
        playing.set(!playing.get());
    }

    /**
     * Retorna o delay atual da animação em ms.
     *
     * @return Delay em ms (valor padrão: 50).
     */
    public long getCharDelay() {
        return charDelay.get();
    }

    /**
     * Define o delay entre caracteres no efeito typewriter.
     * <p>
     * Sendo limitados entre 5ms e 500ms pra evitar algum erro ou comportamento
     * inesperado.
     *
     * @param delay Novo delay em ms.
     */
    public void setCharDelay(long delay) {
        if (delay < 5)
            delay = 5; // Proteção contra velocidade infinita
        if (delay > 500)
            delay = 500;
        this.charDelay.set(delay);
    }

    /**
     * Aumenta a velocidade da typewriter (diminui o delay em 10ms).
     */
    public void increaseSpeed() {
        setCharDelay(getCharDelay() - 10);
    }

    /**
     * Diminui a velocidade do typewriter (aumenta o delay em 10ms).
     */
    public void decreaseSpeed() {
        setCharDelay(getCharDelay() + 10);
    }

    // --- Navegação entre falas ---

    /**
     * Solicita pular para a próxima fala.
     * <p>
     * Ia ser implementado como restrição em falas interativas.
     */
    public void requestSkip() {
        skipRequested.set(true);
    }

    /**
     * Solicita voltar para a fala anterior.
     * <p>
     * Ia ser implementado como restrição em falas interativas.
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
     * Verifica se tem uma próxima fala.
     */
    public boolean hasNextLine() {
        return currentLineIndex.get() < totalLines - 1;
    }

    /**
     * Verifica se tem uma fala anterior.
     */
    public boolean hasPreviousLine() {
        return currentLineIndex.get() > 0;
    }
}
