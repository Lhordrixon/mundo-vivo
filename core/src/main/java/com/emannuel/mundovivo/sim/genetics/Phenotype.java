package com.emannuel.mundovivo.sim.genetics;

import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;

/**
 * Traduz genoma em traços: o que a criatura é, a partir do que ela herdou.
 *
 * <p>Função pura e sem alocação. Roda uma vez por nascimento, escreve em
 * campos primitivos da criatura, e nunca mais é consultada — o custo por
 * quadro é zero.
 *
 * <p><b>Todo traço é aditivo, e isso não é detalhe.</b> Cada um é a média
 * de doze genes espalhados por quatro blocos, nunca um gene só. A razão é
 * que um traço de gene único é degrau: ou o filho herdou aquele bloco e
 * tem o valor do pai, ou não herdou e tem o da mãe. A média dos pais não
 * prevê nada, a regressão filho × pais fica em degraus, e a seleção só
 * consegue mover a população aos saltos. Com doze genes somados, o traço
 * vira contínuo, o filho cai perto da média dos pais, e pressão seletiva
 * fraca consegue empurrar a distribuição aos poucos — que é como a seleção
 * natural funciona.
 *
 * <p>Some-se a isso o teorema central do limite: a média de doze uniformes
 * se concentra no meio da faixa, então a maioria das criaturas nasce
 * mediana e os extremos são raros. É o formato certo para uma população,
 * e é o que {@code TraitDistributionTest} confere.
 *
 * <p><b>Os traços multiplicam a base de {@link CreatureConfig}, não a
 * substituem.</b> A faixa é estreita de propósito — {@value #MIN_FATOR}× a
 * {@value #MAX_FATOR}× — porque o equilíbrio populacional deste jogo foi
 * calibrado com medição, e genética não é desculpa para desregulá-lo. Uma
 * criatura geneticamente veloz é 30% mais rápida que a média, não o dobro.
 */
public final class Phenotype {

    /** Genes somados por bloco em cada traço. */
    private static final int GENES_POR_BLOCO = 3;

    /** Fator mínimo sobre o valor base do config. */
    public static final float MIN_FATOR = 0.7f;

    /** Fator máximo sobre o valor base do config. */
    public static final float MAX_FATOR = 1.3f;

    // Quais blocos alimentam cada traço. Quatro cada, sempre incluindo o
    // bloco "dono" do traço pelo mapa de Genome, mais três que plausivelmente
    // influenciam — pleiotropia barata, e o que garante que nenhum traço
    // dependa de um bloco só. Arrays estáticos: não se aloca nada por
    // criatura.
    private static final int[] BLOCOS_VELOCIDADE = {
        Genome.BLOCK_STRUCTURE, Genome.BLOCK_NEURAL,
        Genome.BLOCK_METABOLISM, Genome.BLOCK_BEHAVIOUR,
    };

    private static final int[] BLOCOS_TAMANHO = {
        Genome.BLOCK_STRUCTURE, Genome.BLOCK_METABOLISM,
        Genome.BLOCK_IMMUNITY, Genome.BLOCK_LONGEVITY,
    };

    private static final int[] BLOCOS_VISAO = {
        Genome.BLOCK_PERCEPTION, Genome.BLOCK_NEURAL,
        Genome.BLOCK_BEHAVIOUR, Genome.BLOCK_STRUCTURE,
    };

    private static final int[] BLOCOS_METABOLISMO = {
        Genome.BLOCK_METABOLISM, Genome.BLOCK_STRUCTURE,
        Genome.BLOCK_IMMUNITY, Genome.BLOCK_REPRODUCTION,
    };

    private static final int[] BLOCOS_LONGEVIDADE = {
        Genome.BLOCK_LONGEVITY, Genome.BLOCK_IMMUNITY,
        Genome.BLOCK_METABOLISM, Genome.BLOCK_REPRODUCTION,
    };

    // Faixas de índice de gene distintas por traço: sem isso, dois traços
    // que compartilham blocos leriam exatamente os mesmos genes e andariam
    // grudados.
    private static final int GENE_BASE_VELOCIDADE = 0;
    private static final int GENE_BASE_TAMANHO = 100;
    private static final int GENE_BASE_VISAO = 200;
    private static final int GENE_BASE_METABOLISMO = 300;
    private static final int GENE_BASE_LONGEVIDADE = 400;

    private Phenotype() {
    }

    /** Recalcula todos os traços da criatura a partir do genoma dela. */
    public static void apply(Creature c, CreatureConfig config) {
        c.size = fator(c.genome, BLOCOS_TAMANHO, GENE_BASE_TAMANHO);
        c.speedTilesPerSecond = config.speedTilesPerSecond
                * fator(c.genome, BLOCOS_VELOCIDADE, GENE_BASE_VELOCIDADE);
        c.visionRadiusTiles = config.searchRadiusTiles
                * fator(c.genome, BLOCOS_VISAO, GENE_BASE_VISAO);
        c.metabolicRate = config.hungerPerSecond
                * fator(c.genome, BLOCOS_METABOLISMO, GENE_BASE_METABOLISMO);
        c.maxAgeSeconds = config.maxAgeSeconds
                * fator(c.genome, BLOCOS_LONGEVIDADE, GENE_BASE_LONGEVIDADE);
    }

    /**
     * Traço bruto em {@code [0,1)}: a média dos genes que o alimentam.
     *
     * <p>Público porque os testes de herdabilidade precisam medir um traço
     * aditivo sem construir uma criatura inteira em volta.
     */
    public static float traco(int[] genoma, int[] blocos, int geneBase) {
        float soma = 0f;
        for (int b = 0; b < blocos.length; b++) {
            int bloco = genoma[blocos[b]];
            for (int g = 0; g < GENES_POR_BLOCO; g++) {
                soma += Genome.gene(bloco, geneBase + g);
            }
        }
        return soma / (blocos.length * GENES_POR_BLOCO);
    }

    private static float fator(int[] genoma, int[] blocos, int geneBase) {
        return MIN_FATOR + traco(genoma, blocos, geneBase) * (MAX_FATOR - MIN_FATOR);
    }
}
