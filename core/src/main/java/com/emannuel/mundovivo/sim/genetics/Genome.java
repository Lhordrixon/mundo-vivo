package com.emannuel.mundovivo.sim.genetics;

/**
 * O genoma de uma criatura: oito blocos de 32 bits.
 *
 * <p>Cada bloco é uma semente. Ele não guarda um gene, guarda a receita de
 * muitos: {@link #gene(int, int)} expande um bloco em quantos valores se
 * quiser, por hash puro. Um genoma de 32 bytes vira, na prática, um número
 * ilimitado de genes — e nenhum deles ocupa memória.
 *
 * <p><b>Por que blocos, e não uma semente só.</b> A alternativa óbvia é
 * guardar uma semente por criatura e fazer {@code filho = hash(pai, mãe)}.
 * Ela é mais simples e está errada: o hash de dois pais é um valor
 * descorrelacionado dos dois. Dois pais grandes gerariam um filho pequeno
 * com a mesma probabilidade de qualquer outro, e sem correlação entre pai e
 * filho não existe herdabilidade. Sem herdabilidade a seleção natural não
 * seleciona nada — o que sobra é deriva aleatória com aparência de
 * evolução, que é o pior dos mundos: parece que funciona.
 *
 * <p>Blocos consertam isso porque o filho recebe blocos <em>inteiros</em>
 * de cada pai. O bloco chega intacto, então tudo que ele codifica chega
 * junto, e a correlação pai-filho sobrevive. De brinde vem a ligação
 * gênica: genes do mesmo bloco são herdados juntos, que é como funciona de
 * verdade, e não uma aproximação simpática.
 *
 * <p>O contraste entre os dois esquemas não está só escrito aqui — está
 * medido em {@code HeritabilityTest}, que roda os dois lado a lado.
 *
 * <p>Classe utilitária: não há instância de {@code Genome} por criatura. O
 * genoma de uma criatura é o {@code int[]} que ela carrega, alocado uma vez
 * por slot do pool.
 */
public final class Genome {

    /** Quantos blocos um genoma tem. */
    public static final int BLOCKS = 8;

    // ------------------------------------------------- mapa de blocos
    //
    // Cada bloco concentra uma família de funções. O mapa não é decorativo:
    // Phenotype lê blocos específicos para cada traço, então trocar um
    // número aqui muda quem herda o quê.

    /** Metabolismo: fome, digestão, gasto em repouso. */
    public static final int BLOCK_METABOLISM = 0;

    /** Estrutura: tamanho e porte. */
    public static final int BLOCK_STRUCTURE = 1;

    /** Neural: velocidade de decisão, futuro cérebro. */
    public static final int BLOCK_NEURAL = 2;

    /** Imunidade: resistência a dano e doença. */
    public static final int BLOCK_IMMUNITY = 3;

    /** Reprodução: fertilidade, custo de ninhada. */
    public static final int BLOCK_REPRODUCTION = 4;

    /** Comportamento: agressividade, sociabilidade. */
    public static final int BLOCK_BEHAVIOUR = 5;

    /** Percepção: alcance de visão e olfato. */
    public static final int BLOCK_PERCEPTION = 6;

    /** Longevidade: envelhecimento e idade máxima. */
    public static final int BLOCK_LONGEVITY = 7;

    /** Constante do SplitMix64, a mesma de {@code Rng}. */
    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;

    private Genome() {
    }

    /**
     * Expande um bloco em um gene.
     *
     * <p>Função pura: mesmo par sempre devolve o mesmo valor, sem estado e
     * sem tocar em sorteador nenhum. É isso que permite chamar
     * {@code gene} no meio do laço de simulação sem quebrar o determinismo
     * da semente do mundo — ela não consome a sequência de ninguém.
     *
     * <p>Usa o finalizador do SplitMix64, o mesmo avalanche de {@code Rng}.
     * Isso importa: dois genes do mesmo bloco precisam ser independentes
     * entre si, e dois blocos que diferem em um único bit precisam produzir
     * genes descorrelacionados. Um hash fraco daria genes que andam juntos
     * e um genoma que, na prática, tem menos dimensões do que aparenta.
     * {@code GeneIndependenceTest} mede as duas coisas.
     *
     * @param block     o valor de 32 bits do bloco — o alelo, não o índice
     *                  do bloco no genoma
     * @param geneIndex qual gene extrair deste bloco
     * @return valor em {@code [0,1)}
     */
    public static float gene(int block, int geneIndex) {
        // O bloco ocupa os 32 bits altos; o índice, espalhado pela constante
        // áurea, ocupa os baixos. Sem o espalhamento, índices pequenos e
        // vizinhos (0, 1, 2...) mexeriam em pouquíssimos bits da entrada e
        // sobrariam para o avalanche resolver sozinho.
        long z = (((long) block) << 32) ^ (geneIndex * GOLDEN_GAMMA);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z = z ^ (z >>> 31);
        return (z >>> 40) * 0x1p-24f;
    }
}
