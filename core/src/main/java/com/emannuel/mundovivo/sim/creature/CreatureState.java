package com.emannuel.mundovivo.sim.creature;

/** Estados possíveis de uma criatura. */
public enum CreatureState {

    /** Sem necessidade urgente: anda a esmo pelo terreno caminhável. */
    WANDERING,

    /** Com fome: procura e caminha até um tile com comida. */
    SEEKING_FOOD,

    /** Parada sobre comida, comendo. */
    EATING,

    /** Saciada e adulta: procura outra criatura no mesmo estado. */
    SEEKING_MATE;

    /** Cache de {@code values()} — o enum é consultado dentro do laço de simulação. */
    public static final CreatureState[] VALUES = values();
}
