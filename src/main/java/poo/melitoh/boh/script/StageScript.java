package poo.melitoh.boh.script;

import poo.melitoh.boh.core.Director;

/**
 * Contrato para scripts de cena/estágio.
 */
public interface StageScript {
    void execute(Director director);
}
