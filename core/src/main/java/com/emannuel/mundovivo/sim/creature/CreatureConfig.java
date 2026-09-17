package com.emannuel.mundovivo.sim.creature;

/**
 * Números que governam o comportamento das criaturas.
 *
 * <p>Todos em unidades de tempo real (segundos) e de mundo (tiles), não em
 * frames. Um celular que roda a 30 quadros e outro que roda a 60 precisam
 * ter o mesmo jogo, não um com criaturas em dobro da velocidade.
 *
 * <p>Vale ser exato sobre o que isso garante. A história exata <em>muda</em>
 * com a taxa de quadros: passos diferentes consomem o sorteador em ordens
 * diferentes, e duas execuções a 30 e a 60 quadros divergem criatura por
 * criatura desde os primeiros segundos. O que se mantém é o comportamento
 * agregado — medido em dez sementes, a população média após vinte minutos
 * ficou em 494, 432 e 463 a 30, 60 e 90 quadros, dentro da variação entre
 * sementes. Só a 20 quadros aparece um desvio para cima (706), porque com
 * passos grossos uma criatura come um naco maior por visita.
 *
 * <p>Os valores padrão foram calibrados para que a população sobreviva e
 * oscile em vez de morrer no primeiro minuto ou crescer sem limite: em doze
 * sementes testadas por trinta minutos, nenhuma extinção e nenhuma batida
 * no teto do pool. São um ponto de partida medido, não uma verdade.
 */
public final class CreatureConfig {

    // --- população ---

    /** Teto rígido de criaturas vivas. Define o tamanho do pool. */
    public int maxCreatures = 3000;

    /** Quantas criaturas nascem junto com o mundo. */
    public int initialPopulation = 120;

    // --- metabolismo ---

    /** Quanto a fome sobe por segundo, em fração de [0,1]. */
    public float hungerPerSecond = 0.045f;

    /** Perda de vida por segundo com a fome no máximo. */
    public float starvationDamagePerSecond = 0.12f;

    /** Ganho de vida por segundo quando bem alimentada. */
    public float healthRegenPerSecond = 0.04f;

    /** Unidades de comida consumidas por segundo ao comer. */
    public float eatingRate = 0.45f;

    /** Quanto de fome cada unidade de comida tira. */
    public float hungerReliefPerFood = 1.6f;

    // --- decisão ---

    /** Acima desta fome, a criatura larga o que estiver fazendo e procura comida. */
    public float hungerSeekThreshold = 0.42f;

    /** Abaixo desta fome, a criatura se considera saciada. */
    public float hungerFullThreshold = 0.12f;

    /** Raio de busca por comida, em tiles. Custa varredura de tiles: manter pequeno. */
    public int searchRadiusTiles = 14;

    /**
     * Raio de busca por parceiro, em tiles. Deliberadamente muito maior que
     * o da comida.
     *
     * <p>Não é um número solto: com os dois raios iguais, uma população que
     * afina deixa de se encontrar e entra em espiral de extinção mesmo com
     * comida sobrando — um terço das sementes testadas morria por isso. E
     * ampliá-lo é de graça, porque a busca por parceiro já percorre a lista
     * de criaturas vivas; o raio só filtra o resultado.
     */
    public int mateSearchRadiusTiles = 70;

    // --- movimento ---

    /** Velocidade em tiles por segundo. */
    public float speedTilesPerSecond = 2.2f;

    /** Distância a partir da qual a criatura considera que chegou ao destino. */
    public float arrivalDistanceTiles = 0.35f;

    // --- ciclo de vida ---

    /** Idade, em segundos, a partir da qual pode reproduzir. */
    public float adultAgeSeconds = 22f;

    /** Idade máxima; depois disso morre de velhice. */
    public float maxAgeSeconds = 180f;

    /** Espera entre duas reproduções da mesma criatura. */
    public float reproductionCooldownSeconds = 55f;

    /** Fome máxima para considerar reprodução — bicho faminto não namora. */
    public float reproductionHungerMax = 0.22f;

    /** Distância de contato para que a reprodução aconteça, em tiles. */
    public float matingDistanceTiles = 1.2f;

    /**
     * Fome que a reprodução custa a cada pai.
     * Sem esse custo a população cresce até bater no teto do pool e fica lá:
     * é ele que amarra a reprodução à comida disponível.
     */
    public float reproductionHungerCost = 0.35f;

    /** Fome com que um filhote nasce. */
    public float newbornHunger = 0.30f;

    /** Comida devolvida ao tile quando uma criatura morre. */
    public float corpseFoodValue = 0.25f;

    public CreatureConfig copy() {
        CreatureConfig c = new CreatureConfig();
        c.maxCreatures = maxCreatures;
        c.initialPopulation = initialPopulation;
        c.hungerPerSecond = hungerPerSecond;
        c.starvationDamagePerSecond = starvationDamagePerSecond;
        c.healthRegenPerSecond = healthRegenPerSecond;
        c.eatingRate = eatingRate;
        c.hungerReliefPerFood = hungerReliefPerFood;
        c.hungerSeekThreshold = hungerSeekThreshold;
        c.hungerFullThreshold = hungerFullThreshold;
        c.searchRadiusTiles = searchRadiusTiles;
        c.mateSearchRadiusTiles = mateSearchRadiusTiles;
        c.speedTilesPerSecond = speedTilesPerSecond;
        c.arrivalDistanceTiles = arrivalDistanceTiles;
        c.adultAgeSeconds = adultAgeSeconds;
        c.maxAgeSeconds = maxAgeSeconds;
        c.reproductionCooldownSeconds = reproductionCooldownSeconds;
        c.reproductionHungerMax = reproductionHungerMax;
        c.matingDistanceTiles = matingDistanceTiles;
        c.reproductionHungerCost = reproductionHungerCost;
        c.newbornHunger = newbornHunger;
        c.corpseFoodValue = corpseFoodValue;
        return c;
    }

    public void validate() {
        require(maxCreatures > 0, "maxCreatures deve ser > 0");
        require(initialPopulation >= 0 && initialPopulation <= maxCreatures,
                "initialPopulation deve caber em maxCreatures");
        require(hungerPerSecond > 0f, "hungerPerSecond deve ser > 0");
        require(hungerFullThreshold < hungerSeekThreshold,
                "hungerFullThreshold precisa ser menor que hungerSeekThreshold, "
                        + "senão a criatura nunca para de comer nem nunca começa");
        require(searchRadiusTiles > 0, "searchRadiusTiles deve ser > 0");
        require(mateSearchRadiusTiles > 0, "mateSearchRadiusTiles deve ser > 0");
        require(speedTilesPerSecond > 0f, "speedTilesPerSecond deve ser > 0");
        require(adultAgeSeconds < maxAgeSeconds,
                "adultAgeSeconds precisa ser menor que maxAgeSeconds, senão ninguém reproduz");
        require(eatingRate > 0f, "eatingRate deve ser > 0");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
