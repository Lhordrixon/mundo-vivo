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
 * agregado — medido em doze sementes a vinte minutos, a população média
 * ficou em 218, 176 e 200 a 30, 60 e 90 quadros, sem nenhuma extinção nas
 * 36 execuções. A dispersão entre taxas existe porque passos grossos rendem
 * mordidas maiores por visita a um tile.
 *
 * <p>Estes valores são referência de ordem de grandeza, não constantes a
 * defender: qualquer mudança que consuma o sorteador em outra ordem os
 * move. Já mudaram duas vezes por isso — quando as facções ganharam nome e
 * cor (eram 538/445/403 antes), e de novo quando o mundo passou a fundar
 * poucos reinos contíguos. Desta última vez a mudança não foi só de
 * sorteio: com o combate corpo a corpo valendo, a fronteira entre reinos
 * cobra vidas, e a população de equilíbrio caiu para cerca de um terço do
 * que era sem combate.
 *
 * <p><b>Estes números foram remedidos depois das espécies, e um deles teve
 * que mudar por causa delas.</b> Restringir o acasalamento a pares da mesma
 * espécie cortou pela metade a densidade de candidatos simultâneos, e com
 * os 120 fundadores originais a população desabava: média 61 a 60 quadros,
 * sementes chegando a 2 sobreviventes, três execuções extinguindo. A
 * correção foi dobrar {@link #initialPopulation}, devolvendo a cada espécie
 * a densidade que a população inteira tinha antes — e não alargar
 * {@link #mateSearchRadiusTiles}, que não recupera nada, nem encurtar
 * {@link #reproductionCooldownSeconds}, que exagera para o outro lado.
 *
 * <p>Os valores abaixo continuam sendo um ponto de partida medido, não uma
 * verdade. O mundo voltou a ser limitado por comida, pela natalidade: a
 * comida cai para 66–77% do total e oscila, em vez de ficar intocada como
 * ficava quando o gargalo era encontrar parceiro.
 */
public final class CreatureConfig {

    // --- população ---

    /** Teto rígido de criaturas vivas. Define o tamanho do pool. */
    public int maxCreatures = 3000;

    /**
     * Quantas criaturas nascem junto com o mundo.
     *
     * <p>Eram 120 até as espécies entrarem. Com o acasalamento restrito a
     * pares da mesma espécie, 120 fundadores viravam dois grupos de ~60 e a
     * população desabava — medido, média 63 contra ~470 de antes, com
     * sementes chegando a um punhado de sobreviventes. Dobrar os fundadores
     * é o que devolve a densidade de cada espécie ao que era a densidade
     * total: com 240, a média a 60 quadros volta para 479.
     *
     * <p>Este número já foi acoplado à contagem de facções: cada fundador
     * fundava a própria, e dobrá-lo dobrava os reinos. Não é mais — quem
     * decide quantos reinos existem é {@link #initialFactions}, e mudar a
     * população só muda quantos habitantes cada reino recebe.
     */
    public int initialPopulation = 240;

    /**
     * Quantas facções o mundo funda, e portanto quantos reinos existem.
     *
     * <p>Eram 240 — uma por fundador — até ficar medido que isso não
     * sustenta nada. Com uma facção por indivíduo, todo vizinho é
     * estrangeiro: não há "dentro do reino", só 240 pontos isolados que se
     * ignoram (ou, quando existe combate, se matam). Agora os fundadores
     * são repartidos entre poucos reinos contíguos, e a maior parte dos
     * vizinhos de alguém é da mesma facção — que é o que faz território
     * significar alguma coisa.
     *
     * <p>O número foi escolhido por medição, não por gosto: ver a tabela
     * de varredura no README. Poucos reinos grandes deixam o mapa sem
     * fronteiras interessantes; muitos reinos pequenos recriam o problema
     * dos 240.
     *
     * <p>Pedir mais reinos que fundadores não é erro: funda-se quantos
     * couberem. Um mundo de uma criatura tem um reino.
     */
    public int initialFactions = 4;

    // --- metabolismo ---

    /** Quanto a fome sobe por segundo, em fração de [0,1]. */
    public float hungerPerSecond = 0.045f;

    /** Perda de vida por segundo com a fome no máximo. */
    public float starvationDamagePerSecond = 0.12f;

    /**
     * Perda de vida por segundo em cima de terreno perigoso.
     *
     * <p>Quatro vezes o dano da fome: morrer de inanição leva uns oito
     * segundos de vida cheia, afogar-se leva dois. A diferença é
     * proposital — fome é um processo, afundar é um acidente, e um
     * acidente que desse tempo de sair andando não seria um acidente.
     * Como todo número deste arquivo, é por segundo e não por quadro.
     */
    public float hazardDamagePerSecond = 0.5f;

    /**
     * Vida tirada por um toque do jogador sobre uma criatura.
     *
     * <p>Único número deste arquivo que <b>não</b> é por segundo: o golpe é
     * instantâneo, não um processo. Por isso o nome não termina em
     * {@code PerSecond} — se um dia terminar, alguém vai multiplicar por
     * {@code dt} sem pensar e o poder vira cócega.
     *
     * <p>0.34 mata em três toques. Dois seria pouco para o jogador
     * perceber que acertou antes de o bicho sumir; muitos mais tornariam
     * matar de propósito um exercício de paciência.
     */
    public float playerStrikeDamage = 0.34f;

    /**
     * Distância em que duas criaturas de facções diferentes já se ferem.
     *
     * <p>Corpo a corpo literal: um pouco maior que
     * {@link #matingDistanceTiles} (1,2), porque brigar exige menos
     * intimidade que reproduzir, e bem menor que qualquer raio de busca —
     * ninguém procura briga, a briga acontece com quem esbarrou.
     */
    public float combatRangeTiles = 1.5f;

    /**
     * Vida perdida por segundo, <b>por inimigo ao alcance</b>.
     *
     * <p>Entre 0,12 da fome e 0,5 do terreno: uma briga de um contra um
     * mata em pouco mais de cinco segundos, tempo de a criatura se afastar
     * andando se o passeio a levar para longe. Não é instantâneo como o
     * golpe do jogador porque não é um golpe — é o tempo que as duas
     * passam perto.
     *
     * <p><b>Multiplicar pelo número de inimigos é a decisão de design
     * deste número.</b> Com dano fixo, uma criatura sozinha troca de igual
     * para igual com um reino inteiro e todos morrem no mesmo ritmo, o que
     * tornaria facção e território decorativos. Multiplicando, estar em
     * menor número mata mais rápido, que é a única coisa que faz um
     * território valer alguma coisa. Isso não é exército como unidade
     * coletiva (item 83): não há formação, ordem nem moral, só a conta de
     * quantos estão ao alcance.
     */
    public float combatDamagePerSecond = 0.18f;

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
        c.initialFactions = initialFactions;
        c.hungerPerSecond = hungerPerSecond;
        c.starvationDamagePerSecond = starvationDamagePerSecond;
        c.hazardDamagePerSecond = hazardDamagePerSecond;
        c.playerStrikeDamage = playerStrikeDamage;
        c.combatRangeTiles = combatRangeTiles;
        c.combatDamagePerSecond = combatDamagePerSecond;
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
        // Só o mínimo. Pedir mais reinos que fundadores não é erro: o
        // povoamento funda quantos couberem (um mundo de uma criatura tem um
        // reino). Recusar isso quebraria mundos pequenos legítimos, como os
        // dos testes, sem proteger de nada.
        require(initialFactions > 0, "initialFactions deve ser > 0");
        require(hungerPerSecond > 0f, "hungerPerSecond deve ser > 0");
        require(hazardDamagePerSecond >= 0f,
                "hazardDamagePerSecond não pode ser negativa, senão terreno perigoso cura");
        require(playerStrikeDamage >= 0f,
                "playerStrikeDamage não pode ser negativa, senão o golpe cura");
        require(combatRangeTiles > 0f,
                "combatRangeTiles deve ser > 0, senão ninguém nunca está ao alcance");
        require(combatDamagePerSecond >= 0f,
                "combatDamagePerSecond não pode ser negativa, senão inimigo por perto cura");
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
