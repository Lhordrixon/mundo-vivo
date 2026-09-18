package com.emannuel.mundovivo.sim.faction;

import com.emannuel.mundovivo.sim.util.Rng;

import java.util.Arrays;

/**
 * Identidade das facções: nome, cor e quantos membros vivos cada uma tem.
 *
 * <p>Até aqui uma facção era só um inteiro sequencial. Virava "facção 37"
 * em qualquer tela ou log, que é um índice, não um reino. Agora cada uma
 * nasce com nome e cor próprios, sorteados no momento da fundação.
 *
 * <p>Continua sem objeto por facção: três arrays paralelos indexados pelo
 * próprio id, pela mesma razão que o mundo é um array de {@code byte} em
 * vez de um array de objetos {@code Tile}. O id é o índice, e não há tabela
 * de busca no meio.
 *
 * <p>Facções não são recicladas como criaturas. Uma criatura nasce em uma
 * facção herdada de um dos pais; uma facção nova só aparece quando alguém
 * nasce sem pais — hoje, apenas a população inicial do mundo. Isso limita
 * a contagem de facções a um punhado de fundadoras, então não há pressão
 * de memória que justifique uma pilha de livres como a do
 * {@link com.emannuel.mundovivo.sim.creature.CreaturePool}: arrays que
 * dobram de tamanho quando enchem são suficientes.
 *
 * <p><b>Cor é {@code int}, não {@code Color}.</b> Nada em {@code sim/}
 * importa libGDX, e essa é a primeira das decisões que sustentam o
 * projeto — é o que permite rodar mundo e população inteiros sem abrir
 * tela. O formato é RGBA8888, o mesmo de
 * {@link com.emannuel.mundovivo.sim.world.TileType#colorRgba8888()}, então
 * a camada de desenho consome os dois do mesmo jeito.
 */
public final class FactionRegistry {

    /**
     * Sílabas iniciais e finais dos nomes.
     *
     * <p>Duas tabelas combinadas dão 384 nomes possíveis. Com as 240
     * fundadoras de um mundo padrão haverá repetição, e tudo bem: nome
     * repetido entre dois reinos de uma criatura cada é menos estranho que
     * "Reino 37". Unicidade entra quando facção virar entidade de verdade
     * — hoje ela ainda é um id com contador.
     *
     * <p>As sílabas são inventadas e não remetem a nenhum povo real, pela
     * mesma razão que as espécies se chamam ALPHA e BETA: batizar de
     * "Reino Élfico" prometeria cultura e diplomacia que o código não tem.
     */
    private static final String[] NAME_HEADS = {
        "Var", "Kor", "Thal", "Bre", "Dun", "Ael", "Mor", "Sel",
        "Gar", "Ith", "Ren", "Tov", "Ash", "Kel", "Orn", "Vel",
        "Dre", "Hal", "Nym", "Sur", "Tyr", "Zan", "Eld", "Bran",
    };

    private static final String[] NAME_TAILS = {
        "eth", "lun", "dor", "mar", "gard", "vik", "ran", "thas",
        "mir", "nen", "stad", "holm", "wyn", "fell", "rok", "dal",
    };

    /**
     * Inverso da razão áurea, usado para espalhar os matizes.
     * Ver a nota sobre distinção de cor em {@link #create(Rng)}.
     */
    private static final float GOLDEN_RATIO_CONJUGATE = 0.618_033_99f;

    /**
     * Pares de saturação e brilho.
     *
     * <p>Existem para separar facções que caíram em matizes próximos: com
     * muitos reinos, duas cores acabam vizinhas no círculo de matiz, e aí é
     * o tom que as distingue. Nenhum par é escuro demais — o fundo do jogo
     * é quase preto, e cor escura sobre ele some.
     */
    private static final float[][] TONES = {
        {0.80f, 0.95f},
        {0.55f, 0.90f},
        {0.95f, 0.75f},
        {0.68f, 1.00f},
    };

    private int[] memberCount = new int[16];
    private String[] names = new String[16];
    private int[] colors = new int[16];
    private int count;

    /**
     * Funda uma facção nova, com um membro — a própria criatura fundadora.
     *
     * <p><b>Determinismo.</b> O nome e o tom saem de um único
     * {@link Rng#nextLong()}, tirado do mesmo sorteador da simulação. Mesma
     * semente de mundo, mesmos nomes e mesmas cores, em qualquer aparelho —
     * a mesma garantia que vale para o terreno e para a espécie. Um sorteio
     * por facção, e não três, para mexer o mínimo possível na sequência que
     * o resto da simulação consome.
     *
     * <p><b>Por que o matiz não é sorteado.</b> Ele vem do índice da
     * facção, girado pelo inverso da razão áurea: cada facção nova cai a
     * cerca de 137,5° da anterior no círculo de cor, que é o giro que
     * maximiza a menor distância entre matizes para qualquer quantidade de
     * facções. Sortear matiz seria pior, não melhor — sorteio agrupa, e
     * dois reinos vizinhos com o mesmo tom de verde é exatamente o que se
     * quer evitar.
     *
     * <p><b>O que este método não consegue garantir.</b> A separação é no
     * espaço de ids, não no mapa. Duas facções vizinhas <em>no terreno</em>
     * podem ter ids distantes e, com azar, cores parecidas. Resolver isso
     * exigiria olhar o território para escolher a cor, e o território é
     * recalculado a cada segundo — a cor mudaria junto, o que é pior que o
     * problema. Com 240 facções também não há 240 cores que um olho humano
     * separe: o objetivo aqui é reino distinguível do vizinho na maioria
     * dos casos, não paleta perfeita.
     *
     * @param rng o sorteador da simulação, para o nome e o tom saírem da
     *            semente do mundo como todo o resto
     * @return o id da facção recém-criada
     */
    public int create(Rng rng) {
        ensureCapacity(count + 1);

        long roll = rng.nextLong();

        memberCount[count] = 1;
        names[count] = nameFrom(roll);
        colors[count] = paletteColor(count, roll);

        return count++;
    }

    /** Quantas facções já existiram, vivas ou extintas. */
    public int factionCount() {
        return count;
    }

    /** Membros vivos da facção agora. */
    public int memberCountOf(int factionId) {
        checkId(factionId);
        return memberCount[factionId];
    }

    /** Nome da facção, como "Vareth" ou "Korgard". Nunca nulo, nunca um índice. */
    public String nameOf(int factionId) {
        checkId(factionId);
        return names[factionId];
    }

    /**
     * Cor da facção, em RGBA8888 — o mesmo formato de
     * {@link com.emannuel.mundovivo.sim.world.TileType#colorRgba8888()}.
     */
    public int colorOf(int factionId) {
        checkId(factionId);
        return colors[factionId];
    }

    /** Facção sem nenhum membro vivo. */
    public boolean isExtinct(int factionId) {
        return memberCountOf(factionId) <= 0;
    }

    /** Quantas facções ainda têm pelo menos um membro vivo. */
    public int livingFactionCount() {
        int living = 0;
        for (int i = 0; i < count; i++) {
            if (memberCount[i] > 0) {
                living++;
            }
        }
        return living;
    }

    /** Chamado a cada nascimento herdado: a facção do escolhido ganha um membro. */
    public void join(int factionId) {
        checkId(factionId);
        memberCount[factionId]++;
    }

    /**
     * Chamado a cada morte: a facção perde um membro.
     *
     * <p>Ignora silenciosamente uma facção já em zero. Não deveria acontecer
     * — cada morte corresponde a um nascimento ou fundação anterior — mas
     * não é motivo para a simulação inteira quebrar se acontecer.
     */
    public void leave(int factionId) {
        checkId(factionId);
        if (memberCount[factionId] > 0) {
            memberCount[factionId]--;
        }
    }

    // ------------------------------------------------------------- geração

    /**
     * Monta o nome a partir de faixas de bits diferentes do mesmo sorteio.
     * O SplitMix64 mistura bem o suficiente para que cabeça e cauda saiam
     * independentes sem gastar dois sorteios.
     */
    private static String nameFrom(long roll) {
        int head = (int) ((roll >>> 1) % NAME_HEADS.length);
        int tail = (int) ((roll >>> 21) % NAME_TAILS.length);
        return NAME_HEADS[head] + NAME_TAILS[tail];
    }

    private static int paletteColor(int index, long roll) {
        float hue = (index * GOLDEN_RATIO_CONJUGATE) % 1f;
        float[] tone = TONES[(int) ((roll >>> 41) % TONES.length)];
        return rgba8888(hue, tone[0], tone[1]);
    }

    /** HSV para RGBA8888, escrito aqui porque {@code sim/} não conhece libGDX. */
    private static int rgba8888(float hue, float saturation, float value) {
        float h = (hue - (float) Math.floor(hue)) * 6f;
        int sector = (int) h;
        float f = h - sector;

        float p = value * (1f - saturation);
        float q = value * (1f - saturation * f);
        float t = value * (1f - saturation * (1f - f));

        float r;
        float g;
        float b;
        switch (sector) {
            case 0:  r = value; g = t;     b = p;     break;
            case 1:  r = q;     g = value; b = p;     break;
            case 2:  r = p;     g = value; b = t;     break;
            case 3:  r = p;     g = q;     b = value; break;
            case 4:  r = t;     g = p;     b = value; break;
            default: r = value; g = p;     b = q;     break;
        }
        return (channel(r) << 24) | (channel(g) << 16) | (channel(b) << 8) | 0xFF;
    }

    private static int channel(float value) {
        int scaled = Math.round(value * 255f);
        if (scaled < 0) return 0;
        if (scaled > 255) return 255;
        return scaled;
    }

    private void ensureCapacity(int needed) {
        if (needed > memberCount.length) {
            int grown = Math.max(needed, memberCount.length * 2);
            memberCount = Arrays.copyOf(memberCount, grown);
            names = Arrays.copyOf(names, grown);
            colors = Arrays.copyOf(colors, grown);
        }
    }

    private void checkId(int factionId) {
        if (factionId < 0 || factionId >= count) {
            throw new IndexOutOfBoundsException(
                    "facção " + factionId + " não existe (0.." + count + ")");
        }
    }
}
