package poo.melitoh.boh.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Componente de animação do texto caractere por caractere.
 * <p>
 * Essa classe implementa o efeito retrô de "máquina de escrever" usado pra
 * simular digitação em tempo real. Roda em thread separada pra não bloquear o
 * loop de render e se integra com o sistema de {@code playback} pra respeitar
 * pausa e ajuste de velocidade.
 * <p>
 * Funcionalidades: <br>
 * - Exibição incremental: adiciona um caractere por vez ao buffer. <br>
 * - Callback de atualização: notifica a cada caractere pra atualizar a
 * interface. <br>
 * - Callback de som: Ia permitir tocar efeito sonoro a cada caractere/nº de
 * caracteres, não lembro como fiz no original. <br>
 * - Controle de pause: respeita o estado do
 * {@link poo.melitoh.boh.core.PlaybackController}. <br>
 * - Velocidade dinâmica: delay entre caracteres pode mudar durante execução.
 * <br>
 * <p>
 * O Typewriter notifica sobre mudanças no estado de atividade (começou/parou de
 * "digitar") pra que as animações dependentes (como o rosto do Boh) possam
 * reagir adequadamente.
 */
public class Typewriter implements Runnable {
    /** Texto completo a ser "digitado". */
    private final String text;

    /** Callback chamado a cada atualização do buffer de texto. */
    private final Consumer<String> onUpdate;

    /** Callback chamado pra cada caractere (pra tocar som). */
    private final Consumer<Character> onChar;

    /**
     * Supplier que indica se a execução deve prosseguir (respeitando pause).
     */
    private final Supplier<Boolean> shouldRun;

    /** Supplier que fornece o delay atual entre caracteres. */
    private final Supplier<Long> delaySupplier;

    /** Callback pra notificar mudanças no estado de atividade. */
    private final Consumer<Boolean> onActivityChange;

    /**
     * Construtor completo com todos os callbacks e suppliers.
     * <p>
     * Esse construtor oferece controle total sobre o comportamento do
     * typewriter, permitindo integração com o sistema de playback.
     *
     * @param text             Texto a ser exibido.
     * @param onUpdate         Callback pra atualização do texto visível.
     * @param onChar           Callback pra cada caractere.
     * @param shouldRun        Supplier que indica se deve continuar.
     * @param delaySupplier    Supplier do delay entre caracteres.
     * @param onActivityChange Callback pra mudança de estado ativo.
     */
    public Typewriter(String text, Consumer<String> onUpdate, Consumer<Character> onChar,
            Supplier<Boolean> shouldRun, Supplier<Long> delaySupplier,
            Consumer<Boolean> onActivityChange) {
        this.text = text == null ? "" : text;
        this.onUpdate = onUpdate;
        this.onChar = onChar;
        this.shouldRun = shouldRun != null ? shouldRun : () -> true;
        this.delaySupplier = delaySupplier != null ? delaySupplier : () -> 50L;
        this.onActivityChange = onActivityChange != null ? onActivityChange : (b) -> {
        };
    }

    /**
     * Construtor com delay fixo.
     *
     * @param text       Texto a ser exibido.
     * @param onUpdate   Callback pra atualização.
     * @param onChar     Callback pra caractere.
     * @param fixedDelay Delay fixo em ms.
     */
    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar, long fixedDelay) {
        this(text, onUpdate, onChar, () -> true, () -> fixedDelay, null);
    }

    /**
     * Construtor simplificado com delay padrão de 50ms.
     *
     * @param text     Texto a ser exibido.
     * @param onUpdate Callback pra atualização.
     * @param onChar   Callback pra caractere.
     */
    public Typewriter(String text, Consumer<String> onUpdate,
            java.util.function.Consumer<Character> onChar) {
        this(text, onUpdate, onChar, 50);
    }

    /**
     * Executa o efeito typewriter.
     * <p>
     * Percorre cada caractere do texto, adicionando ao buffer e notificando os
     * callbacks. Respeita o estado de pause (aguardando em loop) e utiliza o
     * delay fornecido entre cada caractere.
     */
    @Override
    public void run() {
        StringBuilder buffer = new StringBuilder();
        char[] chars = text.toCharArray();

        onActivityChange.accept(true);

        for (int i = 0; i < chars.length; i++) {
            // Pause handling
            while (!shouldRun.get()) {
                onActivityChange.accept(false);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            onActivityChange.accept(true); // Continua a execução depois da
                                           // pausa

            char c = chars[i];
            buffer.append(c);
            onUpdate.accept(buffer.toString());
            if (onChar != null)
                onChar.accept(c);

            try {
                Thread.sleep(delaySupplier.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        onActivityChange.accept(false);
    }
}
