package poo.melitoh.boh.gui;

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
import poo.melitoh.boh.core.Director;
import poo.melitoh.boh.model.Boh;

import java.awt.Font;
import java.io.IOException;

/**
 * GameWindow: gerencia o loop principal e render básico via Lanterna.
 */
public class GameWindow implements Runnable {
    private final Director director;
    private Screen screen;
    private volatile boolean running = false;

    public GameWindow(Director director) {
        this.director = director;
    }

    public void start() throws Exception {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Font font = new Font("Monospaced", Font.PLAIN, 16);
        SwingTerminalFontConfiguration fontConfig = SwingTerminalFontConfiguration
                .newInstance(font);
        factory.setTerminalEmulatorFontConfiguration(fontConfig);

        Terminal terminal = factory.createTerminal();

        String title = "Boh [ ▀ ‿ ▀]";
        if (terminal instanceof javax.swing.JFrame) {
            ((javax.swing.JFrame) terminal).setTitle(title);
        } else {
            factory.setTerminalEmulatorTitle(title);
        }

        screen = new TerminalScreen(terminal);
        screen.startScreen();
        screen.setCursorPosition(null);

        running = true;
        new Thread(this, "GameWindow-Loop").start();
    }

    private void putBold(TextGraphics tg, int x, int y, String text) {
        tg.putString(x, y, text, SGR.BOLD);
    }

    @Override
    public void run() {
        TextGraphics tg = screen.newTextGraphics();
        try {
            while (running) {
                KeyStroke key = screen.pollInput();
                if (key != null && (key.getKeyType() == KeyType.Escape
                        || key.getKeyType() == KeyType.EOF)) {
                    stop();
                    System.exit(0);
                    break;
                }

                screen.doResizeIfNecessary();
                TerminalSize size = screen.getTerminalSize();

                screen.clear();

                Boh boh = director.getBoh();

                // Fixed positioning from Proto
                int fixedFaceX = 2;
                int fixedFaceY = 2;

                // Draw Boh Face
                String face = boh.getCurrentFace();
                putBold(tg, fixedFaceX, fixedFaceY, face);

                // Draw Boh Speech
                String speech = boh.getCurrentSpeech();
                putBold(tg, fixedFaceX + 12, fixedFaceY, speech);

                // Draw Hint
                String hint = "Pressione ESC para sair";
                tg.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
                putBold(tg, 2, size.getRows() - 1, hint);
                tg.setForegroundColor(TextColor.ANSI.DEFAULT);

                screen.refresh();

                Thread.sleep(33);
            }
        } catch (Exception e) {
            e.printStackTrace();
            running = false;
        } finally {
            try {
                if (screen != null)
                    screen.stopScreen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() throws Exception {
        running = false;
        if (director.getBoh() != null) {
            director.getBoh().cleanup();
        }
    }
}
