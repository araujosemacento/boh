package poo.melitoh.boh;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Proof of Concept (PoC) para o Projeto Boh em Java. Versão simplificada usando
 * apenas Screen e TextGraphics (sem GUI2).
 */
public class Boh {

        private static final List<String> IDLE_FACES = Arrays.asList("[ ▀ ¸ ▀]",
                        "[ ▀ ° ▀]", "[ ▀ ■ ▀]", "[ ▀ ─ ▀]", "[ ▀ ~ ▀]", "[ ▀ ▄ ▀]",
                        "[ ▀ ¬ ▀]", "[ ▀ · ▀]", "[ ▀ _ ▀]");

        // Estado compartilhado para renderização
        private static volatile String currentFace = IDLE_FACES.get(0);
        private static volatile String currentSpeech = "";

        public static void main(String[] args) {
                try {
                        // 1. Configuração do Terminal e Screen
                        Terminal terminal = new DefaultTerminalFactory().createTerminal();
                        Screen screen = new TerminalScreen(terminal);
                        screen.startScreen();
                        screen.setCursorPosition(null); // Esconde o cursor

                        // Controle de execução
                        AtomicBoolean running = new AtomicBoolean(true);

                        // 2. Thread de Animação Facial
                        // Atualiza a variável 'currentFace' periodicamente
                        new Thread(() -> {
                                int index = 0;
                                while (running.get()) {
                                        try {
                                                Thread.sleep(200);
                                        } catch (InterruptedException e) {
                                                e.printStackTrace();
                                        }
                                        currentFace = IDLE_FACES
                                                        .get(index % IDLE_FACES.size());
                                        index++;
                                }
                        }).start();

                        // 3. Thread do Typewriter Effect
                        // Atualiza a variável 'currentSpeech' caractere por
                        // caractere (Multilinhas)
                        new Thread(() -> {
                                try {
                                        Thread.sleep(1000); // Aguarda iniciar
                                } catch (InterruptedException e) {
                                }

                                List<String> falas = Arrays.asList("Oi, tudo bem?",
                                                "Eu me chamo Boh!",
                                                "Obrigado por executar meu programa.");

                                for (String fala : falas) {
                                        if (!running.get())
                                                break;

                                        StringBuilder buffer = new StringBuilder();
                                        currentSpeech = ""; // Limpa visualmente

                                        for (char c : fala.toCharArray()) {
                                                if (!running.get())
                                                        break;

                                                buffer.append(c);
                                                currentSpeech = buffer.toString();

                                                try {
                                                        Thread.sleep(50);
                                                } catch (InterruptedException e) {
                                                }
                                        }

                                        try {
                                                Thread.sleep(1500); // Pausa
                                                                    // para
                                                                    // leitura
                                        } catch (InterruptedException e) {
                                        }
                                }
                        }).start();

                        // 4. Loop Principal de Renderização (Game Loop)
                        TextGraphics tg = screen.newTextGraphics();

                        while (running.get()) {
                                // Input handling
                                KeyStroke key = screen.pollInput();
                                if (key != null && (key.getKeyType() == KeyType.Escape
                                                || key.getKeyType() == KeyType.EOF)) {
                                        running.set(false);
                                        break;
                                }

                                // Resize handling
                                screen.doResizeIfNecessary();
                                TerminalSize size = screen.getTerminalSize();

                                // Clear Buffer
                                screen.clear();

                                // Draw Logic
                                int fixedFaceX = 2;
                                int fixedFaceY = 2; // Canto superior esquerdo

                                // Desenha o Rosto
                                String face = currentFace;
                                tg.putString(fixedFaceX, fixedFaceY, face);

                                // Desenha a Fala
                                String speech = currentSpeech;
                                // Exibe ao lado do rosto (margem de ~12 chars)
                                // Da esquerda para a direita, sem deslocamento
                                // lateral
                                tg.putString(fixedFaceX + 12, fixedFaceY, speech);

                                // Desenha instrução de saída
                                String hint = "Pressione ESC para sair";
                                tg.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
                                tg.putString(2, size.getRows() - 1, hint);
                                tg.setForegroundColor(TextColor.ANSI.DEFAULT);

                                // Render Swap
                                screen.refresh();

                                // Cap FPS (~30fps)
                                try {
                                        Thread.sleep(33);
                                } catch (InterruptedException e) {
                                }
                        }

                        screen.stopScreen();
                        System.exit(0);

                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
}
