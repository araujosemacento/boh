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
import poo.melitoh.boh.core.PlaybackController;
import poo.melitoh.boh.model.Boh;
import poo.melitoh.boh.utils.TextFormatter;

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
                PlaybackController pc = director.getPlaybackController();

                KeyStroke key = screen.pollInput();
                if (key != null) {
                    if (key.getKeyType() == KeyType.Escape
                            || key.getKeyType() == KeyType.EOF) {
                        stop();
                        System.exit(0);
                        break;
                    } else if (key.getKeyType() == KeyType.Character
                            && key.getCharacter() == ' ') {
                        pc.togglePlayPause();
                    } else if (key.getKeyType() == KeyType.ArrowUp) {
                        pc.increaseSpeed();
                    } else if (key.getKeyType() == KeyType.ArrowDown) {
                        pc.decreaseSpeed();
                    } else if (key.getKeyType() == KeyType.ArrowRight) {
                        // Próxima fala
                        pc.requestSkip();
                    } else if (key.getKeyType() == KeyType.ArrowLeft) {
                        // Fala anterior
                        pc.requestPrevious();
                    }
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

                // Draw Boh Speech (com formatação)
                String speech = boh.getCurrentSpeech();
                int speechX = fixedFaceX + 12;
                int speechWidth = TextFormatter.render(tg, speechX, fixedFaceY, speech);

                // Draw Static Text (se houver, após a fala)
                String staticText = boh.getStaticText();
                if (staticText != null && !staticText.isEmpty()) {
                    // Se não há fala sendo digitada, mostra o texto estático no
                    // lugar
                    if (speech == null || speech.isEmpty()) {
                        TextFormatter.render(tg, speechX, fixedFaceY, staticText);
                    } else {
                        // Mostra após a fala
                        TextFormatter.render(tg, speechX + speechWidth + 1, fixedFaceY,
                                staticText);
                    }
                }

                // Draw Hint
                StringBuilder hint = new StringBuilder();
                hint.append("ESC:Sair SPACE:Play/Pause ←→:Nav ↑↓:Speed(")
                        .append(pc.getCharDelay()).append("ms)");

                if (pc.getTotalLines() > 0) {
                    hint.append(" [").append(pc.getCurrentLineIndex() + 1).append("/")
                            .append(pc.getTotalLines()).append("]");
                }

                if (!pc.isPlaying()) {
                    hint.append(" [PAUSED]");
                }

                tg.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
                putBold(tg, 2, size.getRows() - 1, hint.toString());
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
