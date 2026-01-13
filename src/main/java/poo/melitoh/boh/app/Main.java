package poo.melitoh.boh.app;

import poo.melitoh.boh.core.Director;
import poo.melitoh.boh.gui.GameWindow;
import poo.melitoh.boh.script.IntroScript;

/**
 * Ponto de entrada principal pra aplicação do Boh. </br>
 * 
 * Classe responsável por orquestrar a inicialização do sistema, tentando operar
 * a lógica, a interface gráfica com estilo terminal e o roteiro inicial.
 * 
 * @see {@link Director} : Atua como o núcleo de controle, gerenciando o estado
 *      global e a execução de scripts. </br>
 *      {@link GameWindow} : Gerencia a visualização via Lanterna, pra manter a
 *      renderização de forma independente. </br>
 *      {@link IntroScript} : O diálogo inicial, pra isolar a lógica de roteiro
 *      da lógica de sistema.
 */
public class Main {
    /**
     * Método de inicialização que configura o ambiente de execução.
     * 
     * A estrutura foi implementada pra modularizar responsabilidades: o Diretor
     * cuida da lógica, enquanto a Janela (GameWindow) cuida da apresentação. O
     * carregamento do script acontece, por último, pra garantir - espero eu -
     * que o sistema de visualização já esteja operacional.
     * 
     * @param args Argumentos de linha de comando (não utilizados).
     * @throws Exception Lançava exceções genéricas pra simplificar o protótipo
     *                   (não foi alterado até agora |-_-| ZzZzZ).
     */
    public static void main(String[] args) throws Exception {
        // Instancia o gerenciador de roteiro e estado.
        Director director = new Director();

        // Cria a interface de terminal vinculada ao diretor.
        GameWindow win = new GameWindow(director);

        // Inicia o loop de renderizacao em uma thread secundaria.
        // {@link GameWindow#start()}
        win.start();

        // Dispara o primeiro roteiro de interação.
        director.loadScript(new IntroScript());

        /**
         * Bloqueia a thread principal para manter o programa em execução. Como
         * o sistema visual e a lógica do roteiro operam em threads separadas,
         * pra evitar congelamento da interface, como ocorrido no protótipo, o
         * encerramento prematuro da thread main precisa ser evitado através do
         * join().
         */
        Thread.currentThread().join();
    }
}
