package com.emannuel.mundovivo.sim.faction;

import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreaturePool;
import com.emannuel.mundovivo.sim.world.TileType;
import com.emannuel.mundovivo.sim.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TerritoryTest {

    private static World flatLand(int width, int height) {
        World world = new World(width, height, 1L);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                world.setTile(x, y, TileType.GRASSLAND);
            }
        }
        return world;
    }

    @Test
    @DisplayName("antes do primeiro recálculo, todo tile está sem dono")
    void startsFullyUnclaimed() {
        World world = flatLand(4, 4);
        Territory territory = new Territory(world);

        for (int i = 0; i < world.tileCount(); i++) {
            assertEquals(Territory.UNCLAIMED, territory.ownerAt(i));
        }
    }

    @Test
    @DisplayName("sem nenhuma criatura viva, o recálculo deixa tudo sem dono")
    void recomputeWithNoCreaturesClaimsNothing() {
        World world = flatLand(6, 6);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);

        territory.recompute(pool);

        for (int i = 0; i < world.tileCount(); i++) {
            assertEquals(Territory.UNCLAIMED, territory.ownerAt(i));
        }
    }

    @Test
    @DisplayName("uma única criatura reivindica toda a terra que consegue alcançar")
    void singleCreatureClaimsAllReachableLand() {
        World world = flatLand(5, 3);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);

        Creature c = pool.spawn(2.5f, 1.5f, 0f, 0f);
        c.factionId = 7;

        territory.recompute(pool);

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                assertEquals(7, territory.ownerAt(x, y), "tile (" + x + "," + y + ") sem dono");
            }
        }
        assertEquals(world.tileCount(), territory.tileCountOf(7));
    }

    @Test
    @DisplayName("um corredor reto se divide exatamente na metade entre duas facções")
    void straightCorridorSplitsAtTheMidpoint() {
        World world = flatLand(10, 1);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);

        Creature a = pool.spawn(0.5f, 0.5f, 0f, 0f);
        a.factionId = 0;
        Creature b = pool.spawn(9.5f, 0.5f, 0f, 0f);
        b.factionId = 1;

        territory.recompute(pool);

        // Tiles 0..4 estão a 4 passos ou menos de A e a 5 ou mais de B;
        // 5..9 é o espelho. Sem empate em nenhum tile.
        for (int x = 0; x <= 4; x++) {
            assertEquals(0, territory.ownerAt(x, 0), "tile " + x + " deveria ser da facção 0");
        }
        for (int x = 5; x <= 9; x++) {
            assertEquals(1, territory.ownerAt(x, 0), "tile " + x + " deveria ser da facção 1");
        }
        assertAll(
                () -> assertEquals(5, territory.tileCountOf(0)),
                () -> assertEquals(5, territory.tileCountOf(1))
        );
    }

    @Test
    @DisplayName("água nunca é reivindicada, mesmo cercada de território dos dois lados")
    void waterIsNeverClaimed() {
        World world = flatLand(5, 1);
        world.setTile(2, 0, TileType.OCEAN);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);

        Creature a = pool.spawn(0.5f, 0.5f, 0f, 0f);
        a.factionId = 0;
        Creature b = pool.spawn(4.5f, 0.5f, 0f, 0f);
        b.factionId = 1;

        territory.recompute(pool);

        assertAll(
                () -> assertEquals(Territory.UNCLAIMED, territory.ownerAt(2, 0)),
                () -> assertEquals(0, territory.ownerAt(0, 0)),
                () -> assertEquals(0, territory.ownerAt(1, 0)),
                () -> assertEquals(1, territory.ownerAt(3, 0)),
                () -> assertEquals(1, territory.ownerAt(4, 0))
        );
    }

    /**
     * Uma língua de água separa duas margens; o território de cada lado
     * precisa contornar a água para alcançar os tiles do canto oposto — e,
     * nesses dois tiles de canto, a distância pelo grafo empata, e o
     * critério de desempate é quem apareceu primeiro na lista de ativos.
     *
     * <pre>
     *   (0,0)(1,0)(2,0)     y=0: terra
     *   (0,1) água (2,1)    y=1: A à esquerda, água no meio, B à direita
     *   (0,2)(1,2)(2,2)     y=2: terra
     * </pre>
     *
     * <p>Distância real de (1,0) e de (1,2) até A e até B é 2 dos dois
     * lados (contornando por cima ou por baixo da água). A busca alcança
     * esses dois tiles pelo lado de A primeiro porque A é semeado antes de
     * B na lista de ativos do pool — não por estar geometricamente mais
     * perto, que não está.
     */
    @Test
    @DisplayName("território contorna água e empates vão para quem entrou primeiro no pool")
    void territoryRoutesAroundWaterAndBreaksTiesByPoolOrder() {
        World world = flatLand(3, 3);
        world.setTile(1, 1, TileType.OCEAN);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);

        Creature a = pool.spawn(0.5f, 1.5f, 0f, 0f);
        a.factionId = 0;
        Creature b = pool.spawn(2.5f, 1.5f, 0f, 0f);
        b.factionId = 1;

        territory.recompute(pool);

        assertAll(
                () -> assertEquals(0, territory.ownerAt(0, 0)),
                () -> assertEquals(0, territory.ownerAt(0, 1)),
                () -> assertEquals(0, territory.ownerAt(0, 2)),
                () -> assertEquals(0, territory.ownerAt(1, 0), "empate deveria ir para A, semeado primeiro"),
                () -> assertEquals(0, territory.ownerAt(1, 2), "empate deveria ir para A, semeado primeiro"),
                () -> assertEquals(Territory.UNCLAIMED, territory.ownerAt(1, 1)),
                () -> assertEquals(1, territory.ownerAt(2, 0)),
                () -> assertEquals(1, territory.ownerAt(2, 1)),
                () -> assertEquals(1, territory.ownerAt(2, 2))
        );
        assertAll(
                () -> assertEquals(5, territory.tileCountOf(0)),
                () -> assertEquals(3, territory.tileCountOf(1))
        );
    }

    @Test
    @DisplayName("recalcular de novo esquece o resultado anterior")
    void recomputeForgetsThePreviousResult() {
        World world = flatLand(4, 1);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);

        Creature a = pool.spawn(0.5f, 0.5f, 0f, 0f);
        a.factionId = 3;
        territory.recompute(pool);
        assertEquals(3, territory.ownerAt(3, 0));

        pool.despawn(a);
        Creature b = pool.spawn(0.5f, 0.5f, 0f, 0f);
        b.factionId = 9;
        territory.recompute(pool);

        assertEquals(9, territory.ownerAt(3, 0));
    }
}
