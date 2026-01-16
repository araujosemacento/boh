package poo.melitoh.boh.utils;

/**
 * Gerenciador estático de som pra mimetizar a aplicação original.
 * <p>
 * Essa classe é projetada pra centralizar a reprodução de sons, os atributos
 * estáticos são usados pra acessar o funcionamento sem instanciar.
 * <p>
 * Status: É só um placeholder - os métodos existem mas não reproduzem som, por
 * eu não ter arranjado os arquivos de audio que eu pretendia usar. A
 * implementação real usaria {@code javax.sound.sampled} pra carregar e tocar
 * .WAV ou .MP3.
 * <p>
 * Uso pretendido: <br>
 * - {@code playTypingSound()}: tocar som robótico durante o animação de fala.
 * <br>
 */
public final class SoundManager {
    /**
     * Construtor privado pra prevenir instancia.
     * <p>
     * Classe usada apenas através de seus métodos estáticos.
     */
    private SoundManager() {}

    /**
     * Reproduz o som de digitação pra um caractere/cadeia de caracteres.
     * <p>
     * Chamado pelo {@link poo.melitoh.boh.ui.Typewriter} a cada callback que
     * fosse aceito, fosse por quantidade ou intervalo de tempo, permitindo
     * feedback sonoro da "fala" do Boh.
     * <p>
     * Implementação atual: stub (não faz nada). Código futuro vai variar o som
     * dando a impressão de articulação, mas ainda sendo basicamente gibberish
     * (ruído, acho que é a tradução?).
     *
     * @param c Caractere sendo "digitado".
     */
    public static void playTypingSound(char c) {
        // TODO Implementar com javax.sound.sampled. Por agora é um stub.
    }
}
