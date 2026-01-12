package poo.melitoh.boh.gui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.gui2.BasicWindow;
import poo.melitoh.boh.core.Director;

/**
 * GameWindow: gerencia o loop principal e render básico via Lanterna.
 */
public class GameWindow extends BasicWindow implements Runnable {
    private final Director director;
    private Screen screen;
    private volatile boolean running = false;

    public GameWindow(Director director) {
        super("Boh");
        this.director = director;
    }

    public void start() throws Exception {
        screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal());
        screen.startScreen();
        running = true;
        new Thread(this, "GameWindow-Loop").start();
    }

    @Override
    public void run() {
        TextGraphics tg = screen.newTextGraphics();
        try {
            while (running) {
                screen.clear();
                tg.setForegroundColor(TextColor.ANSI.WHITE);
                // Render mínimo: exibe o texto corrente do Boh
                tg.putString(2, 2, "Boh: " + director.getBoh().getCurrentSpeech());
                screen.refresh();
                Thread.sleep(33);
            }
        } catch (Exception e) {
            running = false;
        }
    }

    public void stop() throws Exception {
        running = false;
        if (screen != null)
            screen.stopScreen();
    }
}
