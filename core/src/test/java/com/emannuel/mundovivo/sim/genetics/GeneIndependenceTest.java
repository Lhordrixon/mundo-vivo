package com.emannuel.mundovivo.sim.genetics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O avalanche de {@link Genome#gene} precisa ser bom, e "bom" aqui tem
 * medida.
 *
 * <p>Se dois genes do mesmo bloco andarem juntos, o genoma tem menos
 * dimensões do que aparenta: traços que deveriam variar livremente ficariam
 * amarrados, e a população teria menos variedade do que os 256 bits
 * prometem. Se um bit trocado num bloco mudasse pouco os genes dele, a
 * mutação viraria um empurrãozinho na mesma direção em vez de variação
 * nova.
 */
class GeneIndependenceTest {

    private static final int AMOSTRAS = 20000;

    /** Correlação que já consideramos "nenhuma" para este uso. */
    private static final double LIMITE = 0.05;

    @Test
    @DisplayName("dois genes do mesmo bloco não são correlacionados")
    void genesFromTheSameBlockAreIndependent() {
        double pior = 0;
        String onde = "";

        // Vários pares de índice, incluindo vizinhos (0 e 1), que são o caso
        // difícil: entradas quase idênticas.
        int[][] pares = {{0, 1}, {0, 2}, {5, 6}, {0, 100}, {300, 301}};
        for (int[] par : pares) {
            double r = correlacao(par[0], par[1]);
            if (Math.abs(r) > Math.abs(pior)) {
                pior = r;
                onde = "genes " + par[0] + " e " + par[1];
            }
        }

        final double piorFinal = pior;
        final String ondeFinal = onde;
        assertTrue(Math.abs(piorFinal) < LIMITE,
                "correlação " + piorFinal + " em " + ondeFinal);
    }

    @Test
    @DisplayName("blocos que diferem em um bit produzem genes descorrelacionados")
    void oneBitApartBlocksDecorrelate() {
        double piorVizinho = 0;
        for (int bit = 0; bit < 32; bit++) {
            double r = correlacaoEntreBlocosVizinhos(bit);
            if (Math.abs(r) > Math.abs(piorVizinho)) {
                piorVizinho = r;
            }
        }

        final double pior = piorVizinho;
        assertTrue(Math.abs(pior) < LIMITE,
                "trocar um bit do bloco deixou os genes correlacionados: r=" + pior);
    }

    @Test
    @DisplayName("todo gene cai em [0,1) e cobre a faixa inteira")
    void genesStayInRangeAndCoverIt() {
        float menor = Float.MAX_VALUE;
        float maior = -Float.MAX_VALUE;
        double soma = 0;

        for (int i = 0; i < AMOSTRAS; i++) {
            float g = Genome.gene(embaralha(i), 7);
            menor = Math.min(menor, g);
            maior = Math.max(maior, g);
            soma += g;
        }
        double media = soma / AMOSTRAS;

        final float menorFinal = menor;
        final float maiorFinal = maior;
        assertAll(
                () -> assertTrue(menorFinal >= 0f, "gene negativo: " + menorFinal),
                () -> assertTrue(maiorFinal < 1f, "gene >= 1: " + maiorFinal),
                () -> assertTrue(menorFinal < 0.01f, "nunca chegou perto de 0: " + menorFinal),
                () -> assertTrue(maiorFinal > 0.99f, "nunca chegou perto de 1: " + maiorFinal),
                () -> assertTrue(Math.abs(media - 0.5) < 0.01,
                        "média " + media + " longe de 0,5 — distribuição torta")
        );
    }

    // ------------------------------------------------------------ máquina

    private static double correlacao(int geneA, int geneB) {
        double sx = 0, sy = 0, sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < AMOSTRAS; i++) {
            int bloco = embaralha(i);
            double x = Genome.gene(bloco, geneA);
            double y = Genome.gene(bloco, geneB);
            sx += x; sy += y; sxy += x * y; sxx += x * x; syy += y * y;
        }
        return pearson(sx, sy, sxy, sxx, syy, AMOSTRAS);
    }

    private static double correlacaoEntreBlocosVizinhos(int bit) {
        double sx = 0, sy = 0, sxy = 0, sxx = 0, syy = 0;
        for (int i = 0; i < AMOSTRAS; i++) {
            int bloco = embaralha(i);
            double x = Genome.gene(bloco, 3);
            double y = Genome.gene(bloco ^ (1 << bit), 3);
            sx += x; sy += y; sxy += x * y; sxx += x * x; syy += y * y;
        }
        return pearson(sx, sy, sxy, sxx, syy, AMOSTRAS);
    }

    private static double pearson(double sx, double sy, double sxy,
                                  double sxx, double syy, int n) {
        double cov = n * sxy - sx * sy;
        double varX = n * sxx - sx * sx;
        double varY = n * syy - sy * sy;
        return cov / Math.sqrt(varX * varY);
    }

    /**
     * Espalha um contador em valores de bloco.
     *
     * <p>Usar {@code i} cru seria um teste mais fraco do que parece: blocos
     * 0, 1, 2... são entradas quase idênticas, e o teste passaria a medir
     * só esse caso. Espalhando, os blocos amostrados cobrem a faixa inteira
     * de 32 bits.
     */
    private static int embaralha(int i) {
        long z = i * 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) (z ^ (z >>> 31));
    }
}
