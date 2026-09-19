package com.emannuel.mundovivo.sim.genetics;

import com.emannuel.mundovivo.sim.util.Rng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O teste que justifica a fatia inteira.
 *
 * <p>Herdabilidade é a inclinação da regressão do traço do filho sobre a
 * média dos traços dos pais. Inclinação 1 significa que o filho é, em
 * média, exatamente a média dos pais — herança perfeita. Inclinação 0
 * significa que saber os pais não diz nada sobre o filho, e é aí que a
 * seleção natural deixa de existir: sem correlação, nascer forte não
 * aumenta a chance de ter filhos fortes, e a população só passeia.
 *
 * <p>Por isso o contraste com o esquema ingênuo está aqui e não num
 * comentário: ele é a documentação executável da decisão de arquitetura.
 */
class HeritabilityTest {

    private static final int CASAIS = 5000;

    /** Um traço aditivo qualquer, nos moldes do que {@code Phenotype} usa. */
    private static final int[] BLOCOS = {0, 1, 2, 3};
    private static final int GENE_BASE = 0;

    @Test
    @DisplayName("sem mutação, o filho é a média dos pais: inclinação perto de 1")
    void heritabilityWithoutMutationIsNearOne() {
        float slope = regressaoFilhoSobrePais(0f, 4242L);

        assertTrue(slope > 0.8f && slope < 1.2f,
                "inclinação " + slope + " fora de [0.8, 1.2] — a herança não está"
                        + " preservando a correlação pai-filho");
    }

    @Test
    @DisplayName("com a mutação padrão, a herdabilidade continua alta")
    void heritabilitySurvivesDefaultMutation() {
        float slope = regressaoFilhoSobrePais(0.02f, 4242L);

        assertTrue(slope > 0.5f,
                "inclinação " + slope + " abaixo de 0.5 — a mutação está afogando"
                        + " o sinal da herança");
    }

    @Test
    @DisplayName("o esquema ingênuo filho=hash(pai,mãe) perde a herdabilidade inteira")
    void naiveHashSchemeHasNoHeritability() {
        float blocos = regressaoFilhoSobrePais(0f, 99L);
        float ingenuo = regressaoIngenua(99L);

        assertAll(
                () -> assertTrue(Math.abs(ingenuo) < 0.15f,
                        "o esquema ingênuo deu inclinação " + ingenuo + ", que não é"
                                + " perto de zero — o teste de contraste não está"
                                + " medindo o que deveria"),
                () -> assertTrue(blocos > 0.8f,
                        "o esquema de blocos deu " + blocos + " na mesma semente"),
                // O ponto todo, numa asserção: a diferença entre os dois não é
                // de grau.
                () -> assertTrue(blocos - Math.abs(ingenuo) > 0.6f,
                        "blocos=" + blocos + " e ingênuo=" + ingenuo + " ficaram perto"
                                + " demais um do outro")
        );
    }

    // ------------------------------------------------------------ máquina

    /**
     * Gera casais, produz um filho de cada e devolve a inclinação da
     * regressão linear do traço do filho sobre a média dos pais.
     */
    private static float regressaoFilhoSobrePais(float taxaMutacao, long semente) {
        Rng rng = new Rng(semente);
        int[] pai = new int[Genome.BLOCKS];
        int[] mae = new int[Genome.BLOCKS];
        int[] filho = new int[Genome.BLOCKS];

        double somaX = 0, somaY = 0, somaXY = 0, somaXX = 0;

        for (int i = 0; i < CASAIS; i++) {
            Inheritance.random(pai, rng);
            Inheritance.random(mae, rng);
            Inheritance.cross(pai, mae, filho, rng);
            Inheritance.mutate(filho, taxaMutacao, rng);

            double x = (traco(pai) + traco(mae)) / 2.0;
            double y = traco(filho);
            somaX += x;
            somaY += y;
            somaXY += x * y;
            somaXX += x * x;
        }
        return inclinacao(somaX, somaY, somaXY, somaXX, CASAIS);
    }

    /**
     * O esquema que <b>não</b> foi adotado: o genoma do filho é o hash dos
     * dois pais, sem blocos.
     *
     * <p>Mora no teste, não em produção, porque é código que existe para
     * ser provado errado.
     */
    private static float regressaoIngenua(long semente) {
        Rng rng = new Rng(semente);
        int[] pai = new int[Genome.BLOCKS];
        int[] mae = new int[Genome.BLOCKS];
        int[] filho = new int[Genome.BLOCKS];

        double somaX = 0, somaY = 0, somaXY = 0, somaXX = 0;

        for (int i = 0; i < CASAIS; i++) {
            Inheritance.random(pai, rng);
            Inheritance.random(mae, rng);
            for (int b = 0; b < filho.length; b++) {
                filho[b] = misturaIngenua(pai[b], mae[b]);
            }

            double x = (traco(pai) + traco(mae)) / 2.0;
            double y = traco(filho);
            somaX += x;
            somaY += y;
            somaXY += x * y;
            somaXX += x * x;
        }
        return inclinacao(somaX, somaY, somaXY, somaXX, CASAIS);
    }

    /** Hash dos dois pais: determinístico, e completamente descorrelacionado deles. */
    private static int misturaIngenua(int pai, int mae) {
        long z = ((long) pai << 32) ^ (mae & 0xFFFFFFFFL);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) (z ^ (z >>> 31));
    }

    private static double traco(int[] genoma) {
        return Phenotype.traco(genoma, BLOCOS, GENE_BASE);
    }

    private static float inclinacao(double somaX, double somaY, double somaXY,
                                    double somaXX, int n) {
        double numerador = n * somaXY - somaX * somaY;
        double denominador = n * somaXX - somaX * somaX;
        return (float) (numerador / denominador);
    }
}
