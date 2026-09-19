package com.emannuel.mundovivo.sim.genetics;

import com.emannuel.mundovivo.sim.util.Rng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O que acontece com uma população inteira, e não com um casal.
 *
 * <p>São as duas propriedades que separam uma genética que funciona de uma
 * que só parece funcionar: a frequência de um alelo tem que passear e
 * eventualmente fixar quando não há seleção (deriva), e um traço aditivo
 * tem que se distribuir em torno do meio, não empilhar nos extremos.
 */
class PopulationGeneticsTest {

    private static final int POPULACAO = 100;
    private static final int GERACOES = 400;

    @Test
    @DisplayName("sem seleção, a frequência de um alelo passeia e acaba fixando")
    void driftEventuallyFixesAnAllele() {
        int fixaram = 0;
        int fixacaoMaisRapida = Integer.MAX_VALUE;
        int valoresIntermediariosVistos = 0;
        String problema = null;

        for (int execucao = 0; execucao < 12 && problema == null; execucao++) {
            Rng rng = new Rng(1000L + execucao);
            int[][] populacao = populacaoInicial(rng);

            float frequencia = frequenciaDoAlelo(populacao);
            int geracaoDaFixacao = -1;

            for (int g = 0; g < GERACOES; g++) {
                populacao = proximaGeracao(populacao, rng);
                frequencia = frequenciaDoAlelo(populacao);

                if (frequencia < 0f || frequencia > 1f) {
                    problema = "frequência saiu de [0,1]: " + frequencia;
                    break;
                }
                // Passeio, e não salto: contar quantas vezes a frequência
                // parou em algum lugar que não é 0 nem 1 é o que separa
                // deriva de um bug que zera a população de uma vez.
                if (frequencia > 0f && frequencia < 1f) {
                    valoresIntermediariosVistos++;
                } else if (geracaoDaFixacao < 0) {
                    geracaoDaFixacao = g;
                }
            }

            if (geracaoDaFixacao >= 0) {
                fixaram++;
                fixacaoMaisRapida = Math.min(fixacaoMaisRapida, geracaoDaFixacao);
            }
        }

        final int fixaramFinal = fixaram;
        final int maisRapida = fixacaoMaisRapida;
        final int intermediarios = valoresIntermediariosVistos;
        final String problemaFinal = problema;

        assertAll(
                () -> assertNull(problemaFinal, String.valueOf(problemaFinal)),
                () -> assertTrue(fixaramFinal > 0,
                        "nenhuma das 12 execuções fixou em " + GERACOES + " gerações —"
                                + " ou a deriva não está acontecendo, ou algo está"
                                + " reintroduzindo o alelo perdido"),
                // Numa população de 100 com acasalamento aleatório, um alelo
                // neutro leva dezenas de gerações para fixar. Fixar no
                // primeiro punhado seria sinal de que o cruzamento está
                // colapsando a variação, não derivando.
                () -> assertTrue(maisRapida > 10,
                        "a fixação mais rápida veio na geração " + maisRapida
                                + ", cedo demais para ser deriva"),
                () -> assertTrue(intermediarios > 12 * 20,
                        "a frequência quase não passou por valores intermediários ("
                                + intermediarios + ") — isso é salto, não passeio")
        );
    }

    @Test
    @DisplayName("um traço aditivo se concentra no meio da faixa, sem empilhar nos extremos")
    void additiveTraitIsRoughlySymmetric() {
        Rng rng = new Rng(555L);
        int[] genoma = new int[Genome.BLOCKS];
        int[] blocos = {0, 1, 2, 3};

        int amostras = 20000;
        double soma = 0;
        int abaixoDoMeio = 0;
        int nosExtremos = 0;
        float menor = Float.MAX_VALUE;
        float maior = -Float.MAX_VALUE;

        for (int i = 0; i < amostras; i++) {
            Inheritance.random(genoma, rng);
            float t = Phenotype.traco(genoma, blocos, 0);

            soma += t;
            if (t < 0.5f) {
                abaixoDoMeio++;
            }
            // "Extremo" aqui é o quinto inferior ou superior da faixa.
            if (t < 0.2f || t > 0.8f) {
                nosExtremos++;
            }
            menor = Math.min(menor, t);
            maior = Math.max(maior, t);
        }

        double media = soma / amostras;
        double fracaoAbaixo = (double) abaixoDoMeio / amostras;
        double fracaoExtremos = (double) nosExtremos / amostras;
        final float menorFinal = menor;
        final float maiorFinal = maior;

        assertAll(
                () -> assertTrue(Math.abs(media - 0.5) < 0.02,
                        "média do traço em " + media + ", longe do meio da faixa"),
                () -> assertTrue(Math.abs(fracaoAbaixo - 0.5) < 0.03,
                        "distribuição assimétrica: " + fracaoAbaixo + " abaixo do meio"),
                // Doze genes somados: o teorema central do limite deve deixar
                // os extremos raros. Se estiverem comuns, o traço não está
                // sendo aditivo de verdade.
                () -> assertTrue(fracaoExtremos < 0.05,
                        fracaoExtremos + " da população nos extremos — o traço não está"
                                + " somando genes suficientes"),
                () -> assertTrue(menorFinal >= 0f && maiorFinal < 1f,
                        "traço fora de [0,1): " + menorFinal + ".." + maiorFinal)
        );
    }

    // ------------------------------------------------------------ máquina

    private static int[][] populacaoInicial(Rng rng) {
        int[][] populacao = new int[POPULACAO][Genome.BLOCKS];
        for (int i = 0; i < POPULACAO; i++) {
            Inheritance.random(populacao[i], rng);
        }
        return populacao;
    }

    /**
     * Uma geração de acasalamento aleatório, sem seleção nenhuma e sem
     * mutação — mutação reintroduziria o alelo perdido e a fixação nunca
     * aconteceria.
     */
    private static int[][] proximaGeracao(int[][] atual, Rng rng) {
        int[][] proxima = new int[POPULACAO][Genome.BLOCKS];
        for (int i = 0; i < POPULACAO; i++) {
            int pai = rng.nextInt(POPULACAO);
            int mae = rng.nextInt(POPULACAO);
            Inheritance.cross(atual[pai], atual[mae], proxima[i], rng);
        }
        return proxima;
    }

    /** Fração da população que carrega o bit 0 do bloco 0 ligado. */
    private static float frequenciaDoAlelo(int[][] populacao) {
        int portadores = 0;
        for (int i = 0; i < populacao.length; i++) {
            if ((populacao[i][0] & 1) != 0) {
                portadores++;
            }
        }
        return (float) portadores / populacao.length;
    }
}
