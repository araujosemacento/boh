package poo.melitoh.boh;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import java.awt.Font;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Proto {

        private static final List<String> IDLE_FACES = Arrays.asList("[ ▀ ¸ ▀]",
                        "[ ▀ ° ▀]", "[ ▀ ■ ▀]", "[ ▀ ─ ▀]", "[ ▀ ~ ▀]", "[ ▀ ▄ ▀]",
                        "[ ▀ ¬ ▀]", "[ ▀ · ▀]", "[ ▀ _ ▀]");

        private static volatile String currentFace = IDLE_FACES.get(0);
        private static volatile String currentSpeech = "";

        private static void putBold(TextGraphics tg, int x, int y, String text) {
                tg.putString(x, y, text, SGR.BOLD);
        }

        public static void main(String[] args) {
                try {
                        DefaultTerminalFactory factory = new DefaultTerminalFactory();
                        Font font = new Font("Monospaced", Font.PLAIN, 16);
                        SwingTerminalFontConfiguration fontConfig = SwingTerminalFontConfiguration
                                        .newInstance(font);
                        factory.setTerminalEmulatorFontConfiguration(fontConfig);
                        Terminal terminal = factory.createTerminal();
                        @SuppressWarnings("resource")
                        Screen screen = new TerminalScreen(terminal);
                        screen.startScreen();
                        screen.setCursorPosition(null);

                        AtomicBoolean running = new AtomicBoolean(true);

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

                        new Thread(() -> {
                                try {
                                        Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }

                                List<String> falas = Arrays.asList("Oi, tudo bem?",
                                                "Eu me chamo Boh!",
                                                "Obrigado por executar meu programa.");

                                for (String fala : falas) {
                                        if (!running.get())
                                                break;

                                        StringBuilder buffer = new StringBuilder();
                                        currentSpeech = "";

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
                                                Thread.sleep(1500);
                                        } catch (InterruptedException e) {
                                        }
                                }
                        }).start();

                        TextGraphics tg = screen.newTextGraphics();

                        while (running.get()) {
                                KeyStroke key = screen.pollInput();
                                if (key != null && (key.getKeyType() == KeyType.Escape
                                                || key.getKeyType() == KeyType.EOF)) {
                                        running.set(false);
                                        break;
                                }

                                screen.doResizeIfNecessary();
                                TerminalSize size = screen.getTerminalSize();

                                screen.clear();

                                int fixedFaceX = 2;
                                int fixedFaceY = 2;

                                String face = currentFace;
                                putBold(tg, fixedFaceX, fixedFaceY, face);

                                String speech = currentSpeech;
                                putBold(tg, fixedFaceX + 12, fixedFaceY, speech);

                                String hint = "Pressione ESC para sair";
                                tg.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
                                putBold(tg, 2, size.getRows() - 1, hint);
                                tg.setForegroundColor(TextColor.ANSI.DEFAULT);

                                screen.refresh();

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
