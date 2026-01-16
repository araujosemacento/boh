package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Contrato pra scripts de cena/estágio que definem o fluxo das ações.
 * <p>
 * Uma implementação desta interface representa um "roteiro" executável, um
 * conjunto de instruções que o {@link poo.melitoh.boh.core.Director} vai seguir
 * pra conduzir a cena.
 * <p>
 * Filosofia: <br>
 * O {@code StageScript} não se preocupa com detalhes de baixo nível como
 * renderizar o loop; em vez disso, ele dita o fluxo de alto nível: "Boh diz
 * isso", "Boh faz aquela expressão", "Agora mostra tal coisa".
 * <p>
 * O método {@link #execute(Director)} é o ponto de entrada e geralmente roda em
 * sua própria thread (ou é despachado pra uma), permitindo que ele faça pausas
 * (ex: esperar o texto terminar de ser digitado) sem travar a UI.
 */
public interface StageScript {
    /**
     * Executa o roteiro da cena.
     * <p>
     * Este método é reescrito pra conter a lógica sequencial da cena. É chamado
     * pelo {@link poo.melitoh.boh.core.Director} quando uma nova fase é
     * iniciada.
     *
     * @param director O diretor da cena, que fornece acesso aos atores (Boh) e
     *                 controles de playback.
     */
    void execute(Director director);
}
