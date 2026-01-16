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
 * Janela principal do jogo, responsável pelo loop de renderização e input
 * (teoricamente).
 * <p>
 * Essa classe implementa o padrão Game Loop, executando em thread separada pra
 * manter a interface responsiva enquanto os diálogos são processados. Usa a
 * biblioteca Lanterna pra renderização no estilo de uma janela de terminal.
 * <p>
 * Estrutura do loop: <br>
 * - Input: processa teclas pressionadas via {@code screen.pollInput()} da
 * biblioteca Lanterna. <br>
 * - Update: atualiza estado dos atores através do
 * {@link poo.melitoh.boh.core.Director}. <br>
 * - Draw: renderiza a expressão (rostinho do Boh), fala e dicas na tela. <br>
 * - Refresh: sincroniza o buffer com a tela visível. <br>
 * <p>
 * Controles: <br>
 * - ESC: encerra a aplicação. <br>
 * - SPACE: pausa/retoma a reprodução. <br>
 * - ←→: navega entre falas (anterior/próxima). <br>
 * - ↑↓: ajusta velocidade do typewriter. <br>
 */
public class GameWindow implements Runnable {
    /** Referência ao diretor pra acessar atores e playback. */
    private final Director director;

    /** Tela Lanterna pra renderização em modo buffer. */
    private Screen screen;

    /** Flag de controle do loop principal. */
    private volatile boolean running = false;

    /**
     * Construtor que vincula a janela ao diretor de cena.
     *
     * @param director O {@link poo.melitoh.boh.core.Director} que gerencia os
     *                 atores e o estado da reprodução.
     */
    public GameWindow(Director director) {
        this.director = director;
    }

    /**
     * Inicializa o terminal e inicia o loop de renderização.
     * <p>
     * Configura a fábrica de terminal com fonte monoespaçada, cria a tela em
     * modo buffer e dispara a thread de render. Esse método seria chamado uma
     * única vez antes de carregar qualquer script.
     *
     * @throws Exception Se houver falha na criação do terminal ou tela.
     */
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

    /**
     * Método auxiliar pra escrever texto em negrito.
     *
     * @param tg   TextGraphics pra renderização.
     * @param x    Posição X (coluna).
     * @param y    Posição Y (linha).
     * @param text Texto a ser exibido.
     */
    private void putBold(TextGraphics tg, int x, int y, String text) {
        tg.putString(x, y, text, SGR.BOLD);
    }

    /**
     * Loop principal de render.
     * <p>
     * Executa continuamente enquanto {@code running} for true, processando
     * input, limpando a tela, desenhando os elementos e atualizando o buffer.
     * Acho que o loop roda a aproximadamente 30 FPS (33ms de sleep por frame).
     */
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

                // Era a posição fixa do Boh no protótipo
                // e eu simplesmente deixei como tava.
                // Funciona perfeitamente.
                int fixedFaceX = 2;
                int fixedFaceY = 2;

                // Coloca a expressão atual do Boh na tela
                String face = boh.getCurrentFace();
                putBold(tg, fixedFaceX, fixedFaceY, face);

                // Mostra a fala do Boh formatada
                String speech = boh.getCurrentSpeech();
                int speechX = fixedFaceX + 12;
                int speechWidth = TextFormatter.render(tg, speechX, fixedFaceY, speech);

                /*
                 * Era pra mostrar a fala estática (sem o efeito typewriter)
                 */
                String staticText = boh.getStaticText();
                if (staticText != null && !staticText.isEmpty()) {
                    // Se não tem uma fala sendo digitada,
                    // mostraria o texto estático no lugar
                    if (speech == null || speech.isEmpty()) {
                        TextFormatter.render(tg, speechX, fixedFaceY, staticText);
                    } else {
                        // Mostra após a fala
                        TextFormatter.render(tg, speechX + speechWidth + 1, fixedFaceY,
                                staticText);
                    }
                }

                // As dicas de controle na parte inferior
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

    /**
     * Para o loop de render e libera memória.
     * <p>
     * Define a flag de running pra false e chama cleanup nos atores pra
     * interromper threads de animação e liberar recursos.
     *
     * @throws Exception Se houver falha ao parar a tela.
     */
    public void stop() throws Exception {
        running = false;
        if (director.getBoh() != null) {
            director.getBoh().cleanup();
        }
    }
}
