package poo.melitoh.boh.utils;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.SGR;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário pra processamento e renderização de texto formatado.
 * <p>
 * Essa classe implementa um sistema simples de formatação de texto usando
 * códigos especiais prefixados com § (inspirado no sistema que o Minecraft usa,
 * porque eu ainda tô com vontade de voltar a jogar de novo. Segue o link da
 * wiki, por curiosidade: <a href=
 * "https://minecraft.fandom.com/wiki/Formatting_codes#:~:text=Text%20in%20Minecraft%20can%20be%20formatted%20with%20the%20section%20sign%20(%C2%A7).">Formatting
 * codes</a>). </br>
 * Permite adicionar cores e estilos ao texto exibido no terminal. </br>
 * Não é uma adaptação fiel, mas uma tentativa quase completa de replicar o
 * comportamento original.
 * <p>
 * Códigos suportados: <br>
 * - {@code §r} : Reset (volta à cor padrão). <br>
 * - {@code §b} : Azul. <br>
 * - {@code §g} : Verde. <br>
 * - {@code §o} : Laranja. <br>
 * - {@code §w} : Branco. <br>
 * - {@code §0} : Preto. <br>
 * <p>
 * A formatação se aplica apenas até o próximo delimitador (espaço, quebra de
 * linha ou outro código §), pra tentar manter o comportamento previsível e
 * fácil de usar, talvez seja alterado no futuro.
 */
public class TextFormatter {

    /**
     * Representa um segmento de texto com formatação associada.
     * <p>
     * Cada segmento tem uma porção do texto original junto com sua cor e
     * estado, pra permitir renderização incremental.
     */
    public static class TextSegment {
        /** Conteúdo textual do segmento. */
        public final String text;

        /** Cor do texto neste segmento. */
        public final TextColor color;

        /** Se o texto vai ser renderizado em negrito. */
        public final boolean bold;

        /**
         * Construtor do segmento com todos os atributos.
         *
         * @param text  Texto do segmento.
         * @param color Cor a ser aplicada.
         * @param bold  Se vai usar negrito.
         */
        public TextSegment(String text, TextColor color, boolean bold) {
            this.text = text;
            this.color = color;
            this.bold = bold;
        }
    }

    /**
     * Parseia uma string com os códigos de formatação.
     * <p>
     * Processa o texto de entrada, identificando os códigos § e criando
     * segmentos com as cores apropriadas. A formatação é resetada
     * automaticamente em espaços e quebras de linha.
     *
     * @param input Texto com possíveis códigos de formatação.
     * @return Lista de {@link TextSegment} pra renderização.
     */
    public static List<TextSegment> parse(String input) {
        List<TextSegment> segments = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return segments;
        }

        TextColor currentColor = TextColor.ANSI.DEFAULT;
        boolean bold = true; // Default bold como no original
        StringBuilder buffer = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            // Verifica se é um código de formatação
            if (c == '§' && i + 1 < input.length()) {
                // Flush buffer com cor atual
                if (buffer.length() > 0) {
                    segments.add(new TextSegment(buffer.toString(), currentColor, bold));
                    buffer = new StringBuilder();
                }

                char code = input.charAt(i + 1);
                switch (code) {
                case 'r' -> { // Reset
                    currentColor = TextColor.ANSI.DEFAULT;
                    bold = true;
                }
                case 'b' -> { // Azul
                    currentColor = TextColor.ANSI.BLUE_BRIGHT;
                    bold = true;
                }
                case 'g' -> { // Verde
                    currentColor = TextColor.ANSI.GREEN_BRIGHT;
                    bold = true;
                }
                case 'o' -> { // Laranja (usando amarelo como aproximação, pra
                              // implementar as setas coloridas depois, se pá)
                    currentColor = new TextColor.RGB(255, 165, 0);
                    bold = true;
                }
                case 'w' -> { // Branco
                    currentColor = TextColor.ANSI.WHITE_BRIGHT;
                    bold = true;
                }
                case '0' -> { // Preto
                    currentColor = TextColor.ANSI.BLACK;
                    bold = false;
                }
                default -> {
                    // Código desconhecido, trata como texto normal
                    buffer.append(c);
                    i++;
                    continue;
                }
                }
                i += 2; // Pula § e o código
                continue;
            }

            // Verifica se é um limitador que reseta a cor (espaço ou quebra
            // de linha)
            if (c == ' ' || c == '\n') {
                // Flush buffer com cor atual
                if (buffer.length() > 0) {
                    segments.add(new TextSegment(buffer.toString(), currentColor, bold));
                    buffer = new StringBuilder();
                }
                // Adiciona o delimitador com cor padrão
                segments.add(
                        new TextSegment(String.valueOf(c), TextColor.ANSI.DEFAULT, true));
                // Reset para cor padrão depois do caractere-chave
                currentColor = TextColor.ANSI.DEFAULT;
                i++;
                continue;
            }

            // Caractere normal
            buffer.append(c);
            i++;
        }

        // Flush final
        if (buffer.length() > 0) {
            segments.add(new TextSegment(buffer.toString(), currentColor, bold));
        }

        return segments;
    }

    /**
     * Renderiza texto formatado.
     * <p>
     * Combina o parsing e a renderização em uma operação só, iterando em cima
     * dos segmentos e aplicando cores e estilos seguindo o que foi definido.
     *
     * @param tg   {@link TextGraphics} do Lanterna pra renderização.
     * @param x    Posição X inicial (coluna).
     * @param y    Posição Y (linha).
     * @param text Texto com os códigos de formatação.
     * @return Largura total do texto renderizado (pra posicionamento).
     */
    public static int render(TextGraphics tg, int x, int y, String text) {
        List<TextSegment> segments = parse(text);
        int currentX = x;

        TextColor originalColor = tg.getForegroundColor();

        for (TextSegment segment : segments) {
            tg.setForegroundColor(segment.color);
            if (segment.bold) {
                tg.putString(currentX, y, segment.text, SGR.BOLD);
            } else {
                tg.putString(currentX, y, segment.text);
            }
            currentX += segment.text.length();
        }

        // Restaura a cor original
        tg.setForegroundColor(originalColor);

        return currentX - x;
    }

    /**
     * Remove os códigos de formatação e retorna só texto puro.
     */
    public static String stripFormatting(String input) {
        if (input == null)
            return "";
        return input.replaceAll("§[rbgow0]", "");
    }

    /**
     * Calcula a largura visual do texto (sem os códigos de formatação).
     */
    public static int getVisualLength(String input) {
        return stripFormatting(input).length();
    }
}
