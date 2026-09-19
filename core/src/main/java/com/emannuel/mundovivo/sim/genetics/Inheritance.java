package com.emannuel.mundovivo.sim.genetics;

import com.emannuel.mundovivo.sim.util.Rng;

/**
 * Como um genoma vira dois, e dois viram um.
 *
 * <p>Todos os métodos escrevem no array de destino que recebem. Nenhum
 * aloca, nenhum devolve array novo — nascimento acontece dentro do laço de
 * simulação, e é lá que o coletor de lixo não pode ser convidado a entrar.
 * A mesma razão pela qual {@code CreaturePool} pré-aloca tudo.
 */
public final class Inheritance {

    private Inheritance() {
    }

    /**
     * Sorteia um genoma inteiro, para quem nasce sem pais.
     *
     * <p>Hoje só a população fundadora do mundo passa por aqui.
     */
    public static void random(int[] destino, Rng rng) {
        for (int i = 0; i < destino.length; i++) {
            destino[i] = (int) rng.nextLong();
        }
    }

    /**
     * Cruzamento: cada bloco vem inteiro de um dos dois pais, por moeda
     * justa.
     *
     * <p><b>O bloco atravessa intacto, e é esse o ponto.</b> Misturar os
     * bits dos dois pais dentro de um bloco — um hash, uma média, um
     * embaralhamento — produziria um alelo que nenhum dos dois tinha, e a
     * correlação entre pai e filho iria junto. Herdar o bloco fechado é o
     * que mantém a herdabilidade de pé, e é o que faz a seleção natural ter
     * sobre o que agir. Ver a explicação longa em {@link Genome}.
     *
     * <p>Consequência de graça: genes do mesmo bloco viajam juntos de
     * geração em geração. Isso é ligação gênica, e é assim na natureza.
     *
     * <p>{@code destino} pode ser o mesmo array de um dos pais sem
     * corromper nada — cada posição é escrita uma vez e lida uma vez.
     */
    public static void cross(int[] pai, int[] mae, int[] destino, Rng rng) {
        for (int i = 0; i < destino.length; i++) {
            destino[i] = rng.chance(0.5f) ? pai[i] : mae[i];
        }
    }

    /**
     * Mutação: com a probabilidade dada, cada bloco tem um bit invertido.
     *
     * <p>Um bit, não um bloco inteiro sorteado de novo. Trocar o bloco todo
     * seria uma mutação estrutural — o filho deixaria de ter qualquer
     * parentesco naquele bloco, que é exatamente o efeito que o esquema de
     * blocos existe para evitar. Invertendo um bit, o alelo muda e continua
     * vizinho do que era.
     *
     * <p>Note que "vizinho" aqui é no espaço de bits, não no de genes: o
     * avalanche de {@link Genome#gene} garante que um bit trocado muda os
     * genes daquele bloco de forma imprevisível. É essa a diferença entre
     * mutação e ruído gradual, e é ela que permite à mutação criar variação
     * nova de verdade em vez de empurrar todo mundo na mesma direção.
     *
     * @param taxaPorBloco probabilidade de mutação em cada bloco
     */
    public static void mutate(int[] genoma, float taxaPorBloco, Rng rng) {
        if (taxaPorBloco <= 0f) {
            return;
        }
        for (int i = 0; i < genoma.length; i++) {
            if (rng.chance(taxaPorBloco)) {
                genoma[i] ^= 1 << rng.nextInt(32);
            }
        }
    }
}
