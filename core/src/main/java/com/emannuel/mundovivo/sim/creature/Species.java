package com.emannuel.mundovivo.sim.creature;

/**
 * A que espécie uma criatura pertence.
 *
 * <p><b>Os nomes são propositalmente sem graça.</b> Hoje a espécie faz
 * exatamente uma coisa: decide quem pode ter filho com quem. Não muda
 * velocidade, fome, dieta, tamanho nem hostilidade, e não aparece na tela.
 * Batizar as duas de algo evocativo — lobo e cervo, elfo e anão — prometeria
 * predação, cultura e guerra que o código não tem, e o projeto já paga caro
 * por promessa não cumprida: a conversão de toque para tile passou meses
 * escrita e sem chamador. Rótulo seco é mais honesto que lore adiantado.
 *
 * <p>É por isso que o enum nasce com duas constantes e não com um bestiário.
 * Duas bastam para a regra existir e ser testável; as raças de verdade, com
 * atributos próprios, entram aqui depois, uma de cada vez, cada uma quando
 * tiver comportamento que a justifique.
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
