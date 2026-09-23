package com.emannuel.mundovivo.sim.creature;

/**
 * A que espécie uma criatura pertence.
 *
 * <p>Hoje a espécie só decide quem pode ter filho com quem. Os nomes são
 * secos para não prometer o que o código não faz:
 * docs/decisoes/0009-nomes-secos-de-especie.md.
 *
 * <p>Uma criatura viva nunca tem espécie nula: {@link Creature#reset} atribui
 * um valor válido antes de qualquer outra coisa, e {@code Simulation}
 * sobrescreve logo em seguida — por sorteio, no povoamento inicial, ou por
 * herança, a cada nascimento.
 *
 * <p>O array {@link #VALUES} existe pela mesma razão do de
 * {@link CreatureState}: {@code values()} clona o array a cada chamada, e
 * este enum é consultado dentro do laço de simulação.
 */
public enum Species {

    ALPHA,
    BETA;

    /** Cache de {@code values()} — ver nota na documentação da classe. */
    public static final Species[] VALUES = values();
}
