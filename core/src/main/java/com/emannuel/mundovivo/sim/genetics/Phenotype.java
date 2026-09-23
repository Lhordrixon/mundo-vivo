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
 * <p>Cada traço é a média de doze genes de quatro blocos, e multiplica a
 * base de {@link CreatureConfig} numa faixa estreita ({@value #MIN_FATOR}× a
 * {@value #MAX_FATOR}×) para não desregular a população calibrada. Por quê:
 * docs/decisoes/0005-genoma-em-blocos.md.
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
