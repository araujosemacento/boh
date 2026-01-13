package poo.melitoh.boh.utils;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.SGR;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para processar e renderizar texto formatado com códigos especiais.
 * 
 * Códigos suportados: - §r : Reset (volta à cor padrão) - §b : Azul (bold) - §g
 * : Verde - §o : Laranja - §w : Branco (padrão) - §0 : Preto
 * 
 * A formatação se aplica apenas até: espaço, quebra de linha, ou outro código
 * §.
 */
public class TextFormatter {

    /**
     * Representa um segmento de texto com sua cor associada.
     */
    public static class TextSegment {
        public final String text;
        public final TextColor color;
        public final boolean bold;

        public TextSegment(String text, TextColor color, boolean bold) {
            this.text = text;
            this.color = color;
            this.bold = bold;
        }
    }

    /**
     * Parseia uma string com códigos de formatação e retorna uma lista de
     * segmentos. A formatação se aplica apenas à "palavra" atual (até espaço,
     * \n ou §r).
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
                case 'o' -> { // Laranja (usando amarelo como aproximação)
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

            // Verifica se é um delimitador que reseta a cor (espaço ou quebra
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
                // Reset para cor padrão após delimitador
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
     * Renderiza texto formatado usando TextGraphics do Lanterna.
     * 
     * @param tg   TextGraphics para renderização
     * @param x    Posição X inicial
     * @param y    Posição Y
     * @param text Texto com códigos de formatação
     * @return Largura total do texto renderizado (para posicionamento)
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

        // Restaura cor original
        tg.setForegroundColor(originalColor);

        return currentX - x;
    }

    /**
     * Remove os códigos de formatação e retorna apenas o texto puro.
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
