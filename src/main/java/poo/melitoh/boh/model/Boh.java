package poo.melitoh.boh.model;

import poo.melitoh.boh.ui.Typewriter;
import poo.melitoh.boh.utils.SoundManager;
import poo.melitoh.boh.core.PlaybackController;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Personagem principal.
 * <p>
 * Essa classe representa o protagonista que "fala" com o usuário através de
 * efeito animado typewriter, onde uma letra é exibida por vez, acompanhada das
 * expressões faciais animadas. Boh ensina os conceitos do algoritmo de inversão
 * de listas carismaticamente (if i do say so, myself).
 * <p>
 * Funcionalidades: <br>
 * - Animação facial: alterna entre expressões enquanto ele "fala". <br>
 * - Typewriter: exibe texto caractere por caractere, teria som na adaptação
 * completa do script. <br>
 * - Texto estático: era pra exibir conteúdo sem animação (código, exemplos).
 * <br>
 * - Controle de estado: integração com
 * {@link poo.melitoh.boh.core.PlaybackController} pra pausar/retomar as
 * animações. <br>
 * <p>
 * A animação facial roda em thread separada, atualizando a expressão apenas
 * quando Boh está "falando" (talking = true), pra dar a impressão de movimento
 * labial estilizado, mudança de expressão faccial, etc.
 */
public class Boh extends Actor {

    /** Lista de expressões faciais pra animação de "fala". */
    private static final List<String> IDLE_FACES = Arrays.asList("[ ▀ ¸ ▀]", "[ ▀ ° ▀]",
            "[ ▀ ■ ▀]", "[ ▀ ─ ▀]", "[ ▀ ~ ▀]", "[ ▀ ▄ ▀]", "[ ▀ ¬ ▀]", "[ ▀ · ▀]",
            "[ ▀ _ ▀]");

    /** Expressão facial atual sendo exibida. */
    private volatile String currentFace = IDLE_FACES.get(0);

    /** Texto sendo digitado pelo efeito typewriter. */
    private volatile String currentSpeech = "";

    /** Texto estático exibido ao lado da fala. */
    private volatile String staticText = "";

    /** Flag de controle da thread de animação facial. */
    private final AtomicBoolean animating = new AtomicBoolean(true);

    /** Flag que indica se Boh está "falando" (animação ativa). */
    private final AtomicBoolean talking = new AtomicBoolean(false);

    /** Referência ao controlador de playback pra sincronização. */
    private PlaybackController playbackController;

    /** Thread atual do typewriter pra permitir interrupção. */
    private volatile Thread currentTypewriterThread;

    /**
     * Construtor que inicializa Boh e inicia a animação das expressões faciais.
     * <p>
     * Chama o construtor pai e dispara a thread de animação de expressões.
     */
    public Boh() {
        super();
        startFaceAnimation();
    }

    /**
     * Vincula o controlador de playback ao Boh.
     * <p>
     * Permite que a animação respeite o estado de pause/play e a velocidade
     * configurada pelo usuário.
     *
     * @param controller O {@link poo.melitoh.boh.core.PlaybackController}
     *                   ativo.
     */
    public void setPlaybackController(PlaybackController controller) {
        this.playbackController = controller;
    }

    /**
     * Inicia a thread de animação facial.
     * <p>
     * Roda em background, alternando entre expressões a cada 200ms, mas só
     * atualiza a face quando {@code talking} é true, pra que ele não pare de
     * falar e continue "gesticulando".
     */
    private void startFaceAnimation() {
        Thread faceThread = new Thread(() -> {
            int index = 0;
            while (animating.get()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                // Only update face if talking
                if (talking.get()) {
                    currentFace = IDLE_FACES.get(index % IDLE_FACES.size());
                    index++;
                }
            }
        }, "Boh-FaceAnim");
        faceThread.start();
    }

    /**
     * Define o humor/expressão fixa do Boh.
     * <p>
     * Placeholder pra futura implementação das outras expressões (feliz,
     * confuso, pensativo, etc.).
     *
     * @param mood Identificador do humor (não implementado).
     */
    public void setMood(String mood) {
        // A futura lógica de mudança de humor vem aqui
    }

    /**
     * Define o texto estático pra ser exibido do lado/embaixo da fala do Boh.
     */
    public void setStaticText(String text) {
        this.staticText = text != null ? text : "";
    }

    /**
     * Retorna o texto estático atual.
     */
    public String getStaticText() {
        return staticText;
    }

    /**
     * Limpa o texto estático.
     */
    public void clearStaticText() {
        this.staticText = "";
    }

    /**
     * Interrompe a fala atual, se houver.
     */
    public void interruptSpeech() {
        if (currentTypewriterThread != null && currentTypewriterThread.isAlive()) {
            currentTypewriterThread.interrupt();
        }
        talking.set(false);
    }

    /**
     * Faz Boh "falar" um texto com efeito typewriter.
     * <p>
     * Cria uma instância de {@link poo.melitoh.boh.model.Typewriter} que exibe
     * o texto caractere por caractere e atualiza a flag de talking pra ativar a
     * animação das expressões.
     * <p>
     * O método retorna a Thread pra aguardar o término da fala com
     * {@code join()} se preciso for.
     *
     * @param text Texto a ser "falado".
     * @return A Thread do typewriter pra controle externo.
     */
    public Thread say(String text) {
        // Suppliers com fallback caso controller não esteja presente
        java.util.function.Supplier<Boolean> runSupplier = () -> (playbackController == null
                || playbackController.isPlaying());
        java.util.function.Supplier<Long> delaySupplier = () -> (playbackController != null
                ? playbackController.getCharDelay()
                : 50L);

        Typewriter tw = new Typewriter(text, s -> currentSpeech = s,
                c -> SoundManager.playTypingSound(c), runSupplier, delaySupplier,
                talking::set);
        Thread t = new Thread(tw, "Typewriter-Boh");
        currentTypewriterThread = t;
        t.start();
        return t;
    }

    /**
     * Exibe texto estático imediatamente (sem animação de digitação).
     */
    public void showStatic(String text) {
        this.currentSpeech = "";
        this.staticText = text != null ? text : "";
    }

    /**
     * Retorna a expressão facial atual.
     *
     * @return String que representa o rosto do Boh no momento (ex: "[ ▀ ° ▀]").
     */
    public String getCurrentFace() {
        return currentFace;
    }

    /**
     * Retorna o texto sendo exibido pelo typewriter.
     *
     * @return Texto parcial ou completo da fala atual.
     */
    public String getCurrentSpeech() {
        return currentSpeech;
    }

    /**
     * Verifica se Boh está falando.
     *
     * @return {@code true} se o typewriter estiver ativo.
     */
    public boolean isTalking() {
        return talking.get();
    }

    /**
     * Atualiza o estado do Boh.
     * <p>
     * Placeholder pra futuras atualizações por frame que não dependam das
     * threads de animação e typewriter.
     */
    @Override
    public void updateState() {
        // Atualizações por frame se necessário
    }

    /**
     * Libera recursos e para thread de animação.
     * <p>
     * Tem que ser chamado quando a aplicação for encerrada pra garantir que a
     * thread de animação facial termine graciosamente *v*.
     */
    public void cleanup() {
        animating.set(false);
    }
}
