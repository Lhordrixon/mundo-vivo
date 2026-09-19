package com.emannuel.mundovivo.sim.genetics;

import com.emannuel.mundovivo.sim.util.Rng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Determinismo, e as regras básicas de cruzamento e mutação. */
class InheritanceTest {

    @Test
    @DisplayName("mesmos pais e mesmo estado de Rng produzem o mesmo filho, bit a bit")
    void sameParentsAndRngStateProduceTheSameChild() {
        int[] pai = new int[Genome.BLOCKS];
        int[] mae = new int[Genome.BLOCKS];
        Inheritance.random(pai, new Rng(1L));
        Inheritance.random(mae, new Rng(2L));

        int[] primeiro = new int[Genome.BLOCKS];
        int[] segundo = new int[Genome.BLOCKS];

        Rng rngA = new Rng(777L);
        Inheritance.cross(pai, mae, primeiro, rngA);
        Inheritance.mutate(primeiro, 0.02f, rngA);

        Rng rngB = new Rng(777L);
        Inheritance.cross(pai, mae, segundo, rngB);
        Inheritance.mutate(segundo, 0.02f, rngB);

        assertArrayEquals(primeiro, segundo,
                "duas execuções do mesmo cruzamento divergiram");
    }

    @Test
    @DisplayName("todo bloco do filho veio inteiro de um dos dois pais")
    void everyChildBlockComesWholeFromAParent() {
        Rng rng = new Rng(31337L);
        int[] pai = new int[Genome.BLOCKS];
        int[] mae = new int[Genome.BLOCKS];
        int[] filho = new int[Genome.BLOCKS];

        int doPai = 0;
        int daMae = 0;
        for (int repeticao = 0; repeticao < 500; repeticao++) {
            Inheritance.random(pai, rng);
            Inheritance.random(mae, rng);
            Inheritance.cross(pai, mae, filho, rng);

            for (int b = 0; b < Genome.BLOCKS; b++) {
                boolean veioDoPai = filho[b] == pai[b];
                boolean veioDaMae = filho[b] == mae[b];
                assertTrue(veioDoPai || veioDaMae,
                        "bloco " + b + " do filho não é de nenhum dos pais — o"
                                + " cruzamento está misturando bits dentro do bloco");
                if (veioDoPai) {
                    doPai++;
                } else {
                    daMae++;
                }
            }
        }

        // Moeda justa: nem perto de 50/50 por acaso, nem enviesada para um lado.
        float fracaoDoPai = (float) doPai / (doPai + daMae);
        assertTrue(fracaoDoPai > 0.45f && fracaoDoPai < 0.55f,
                "a moeda do cruzamento está viciada: " + fracaoDoPai + " veio do pai");
    }

    @Test
    @DisplayName("mutação troca exatamente um bit, e só nos blocos sorteados")
    void mutationFlipsExactlyOneBit() {
        Rng rng = new Rng(4242L);
        int[] original = new int[Genome.BLOCKS];
        int[] mutado = new int[Genome.BLOCKS];

        int blocosMutados = 0;
        int blocosVistos = 0;
        for (int repeticao = 0; repeticao < 2000; repeticao++) {
            Inheritance.random(original, rng);
            System.arraycopy(original, 0, mutado, 0, Genome.BLOCKS);
            Inheritance.mutate(mutado, 0.5f, rng);

            for (int b = 0; b < Genome.BLOCKS; b++) {
                blocosVistos++;
                int diferenca = original[b] ^ mutado[b];
                if (diferenca != 0) {
                    blocosMutados++;
                    assertEquals(1, Integer.bitCount(diferenca),
                            "bloco " + b + " mudou " + Integer.bitCount(diferenca)
                                    + " bits em vez de um");
                }
            }
        }

        float taxaObservada = (float) blocosMutados / blocosVistos;
        assertTrue(taxaObservada > 0.45f && taxaObservada < 0.55f,
                "taxa de mutação observada " + taxaObservada + " longe da pedida (0.5)");
    }

    @Test
    @DisplayName("taxa zero não muda nada")
    void zeroRateMutatesNothing() {
        Rng rng = new Rng(8L);
        int[] original = new int[Genome.BLOCKS];
        Inheritance.random(original, rng);
        int[] copia = original.clone();

        Inheritance.mutate(copia, 0f, rng);

        assertArrayEquals(original, copia, "mutou com taxa zero");
    }

    @Test
    @DisplayName("um genoma sorteado usa a faixa inteira dos 32 bits")
    void randomGenomeUsesTheWholeRange() {
        Rng rng = new Rng(2026L);
        int[] genoma = new int[Genome.BLOCKS];

        int orNaFaixa = 0;
        int andNaFaixa = -1;
        for (int i = 0; i < 1000; i++) {
            Inheritance.random(genoma, rng);
            for (int b = 0; b < Genome.BLOCKS; b++) {
                orNaFaixa |= genoma[b];
                andNaFaixa &= genoma[b];
            }
        }

        final int todosOsBitsVistosEmUm = orNaFaixa;
        final int todosOsBitsVistosEmZero = andNaFaixa;
        assertAll(
                () -> assertEquals(-1, todosOsBitsVistosEmUm,
                        "há bits que nunca ficaram em 1"),
                () -> assertEquals(0, todosOsBitsVistosEmZero,
                        "há bits que nunca ficaram em 0")
        );
    }
}
