package com.emannuel.mundovivo.sim.faction;

import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreaturePool;
import com.emannuel.mundovivo.sim.world.World;

import java.util.Arrays;

/**
 * Partição do mundo por facção: cada tile de terra pertence à facção cujo
 * membro vivo está mais perto.
 *
 * <p>"Mais perto" aqui é distância percorrida por tiles vizinhos
 * caminháveis, não linha reta. Uma baía de duas criaturas frente a frente
 * pelo mar não devia dividir o litoral ao meio pela água — cada uma
 * caminharia horas para contornar até a fronteira "mais próxima" fazer
 * sentido no chão. Contar passos pelo grafo de tiles caminháveis já
 * resolve isso de graça: a água nunca é atravessada, então o território de
 * cada lado da baía cresce contornando-a, do jeito que uma criatura
 * realmente andaria.
 *
 * <p>Isso também é o motivo de o cálculo ser uma busca em largura
 * multi-fonte (todos os membros vivos entram na fila ao mesmo tempo, e o
 * primeiro a alcançar um tile decide o dono) em vez de comparar cada tile
 * contra cada criatura: é O(tiles), não O(tiles × criaturas) — a diferença
 * entre varrer o mundo padrão uma vez (49 mil operações) e varrê-lo uma vez
 * por criatura viva (dezenas de milhões, com a população de meio-jogo).
 * Os três arrays de apoio são pré-alocados no tamanho do mundo e
 * reaproveitados a cada recálculo, então recalcular não pressiona o GC.
 *
 * <p>Tiles de água nunca são alcançados pela busca (ela não atravessa
 * terreno não caminhável) e ficam sem dono, assim como um mundo sem
 * nenhuma criatura viva fica todo sem dono. Nenhum dos dois é um bug: água
 * não é território de ninguém, e território é uma vista derivada de quem
 * está vivo agora, não um registro histórico.
 */
public final class Territory {

    /** Nenhuma facção reivindica o tile: é água, ou não há criatura viva por perto. */
    public static final int UNCLAIMED = -1;

    private final World world;
    private final int[] ownerFaction;
    private final int[] distance;
    private final int[] queue;

    public Territory(World world) {
        this.world = world;
        int size = world.tileCount();
        this.ownerFaction = new int[size];
        this.distance = new int[size];
        this.queue = new int[size];
        Arrays.fill(ownerFaction, UNCLAIMED);
    }

    /** Facção dona do tile, ou {@link #UNCLAIMED}. */
    public int ownerAt(int index) {
        return ownerFaction[index];
    }

    /** Facção dona do tile, ou {@link #UNCLAIMED}. */
    public int ownerAt(int x, int y) {
        return ownerFaction[world.index(x, y)];
    }

    /** Quantos tiles a facção controla no recálculo mais recente. */
    public int tileCountOf(int factionId) {
        int total = 0;
        for (int owner : ownerFaction) {
            if (owner == factionId) {
                total++;
            }
        }
        return total;
    }

    /**
     * Recalcula o território inteiro a partir da posição atual de cada
     * criatura viva.
     *
     * <p>Quando duas criaturas de facções diferentes ocupam o mesmo tile no
     * instante do recálculo, a que vem primeiro na lista de ativos do pool
     * vence — arbitrário, mas determinístico: mesma simulação, mesma
     * história, mesmo resultado, o que os testes de determinismo exigem de
     * qualquer parte do jogo.
     */
    public void recompute(CreaturePool pool) {
        Arrays.fill(ownerFaction, UNCLAIMED);
        Arrays.fill(distance, -1);

        int width = world.width();
        int tail = 0;

        for (int i = 0, n = pool.activeCount(); i < n; i++) {
            Creature c = pool.activeAt(i);
            int tile = clampedTile(c);
            if (distance[tile] < 0) {
                distance[tile] = 0;
                ownerFaction[tile] = c.factionId;
                queue[tail++] = tile;
            }
        }

        int head = 0;
        while (head < tail) {
            int tile = queue[head++];
            int x = tile % width;
            int y = tile / width;
            int nextDistance = distance[tile] + 1;
            int faction = ownerFaction[tile];

            tail = visit(x - 1, y, nextDistance, faction, tail);
            tail = visit(x + 1, y, nextDistance, faction, tail);
            tail = visit(x, y - 1, nextDistance, faction, tail);
            tail = visit(x, y + 1, nextDistance, faction, tail);
        }
    }

    private int visit(int x, int y, int newDistance, int faction, int tail) {
        if (!world.inBounds(x, y) || !world.tileAtUnsafe(x, y).walkable()) {
            return tail;
        }
        int index = world.index(x, y);
        if (distance[index] >= 0) {
            // Já alcançado por uma busca que chegou primeiro ou no mesmo
            // instante — em BFS isso já é a distância mínima, não há como
            // um caminho mais tardio ser mais curto.
            return tail;
        }
        distance[index] = newDistance;
        ownerFaction[index] = faction;
        queue[tail] = index;
        return tail + 1;
    }

    private int clampedTile(Creature c) {
        int x = clamp(c.tileX(), 0, world.width() - 1);
        int y = clamp(c.tileY(), 0, world.height() - 1);
        return world.index(x, y);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
