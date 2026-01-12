package poo.melitoh.boh.app;

import poo.melitoh.boh.core.Director;
import poo.melitoh.boh.gui.GameWindow;
import poo.melitoh.boh.script.IntroScript;

/**
 * Ponto de entrada para executar o programa.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Director director = new Director();
        GameWindow win = new GameWindow(director);
        win.start();
        director.loadScript(new IntroScript());
        // Mantém a aplicação viva (o loop do GameWindow roda em uma thread separada)
        Thread.currentThread().join();
    }
}
