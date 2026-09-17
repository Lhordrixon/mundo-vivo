package com.emannuel.mundovivo.sim.creature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreaturePoolTest {

    @Test
    @DisplayName("nascer e morrer mantém as contagens coerentes")
    void countsStayConsistent() {
        CreaturePool pool = new CreaturePool(10);
        assertEquals(0, pool.activeCount());
        assertEquals(10, pool.freeCount());

        Creature a = pool.spawn(1f, 1f, 0f, 0f);
        Creature b = pool.spawn(2f, 2f, 0f, 0f);

        assertAll(
                () -> assertEquals(2, pool.activeCount()),
                () -> assertEquals(8, pool.freeCount()),
                () -> assertTrue(pool.isAlive(a.slot())),
                () -> assertTrue(pool.isAlive(b.slot()))
        );

        pool.despawn(a);

        assertAll(
                () -> assertEquals(1, pool.activeCount()),
                () -> assertEquals(9, pool.freeCount()),
                () -> assertFalse(pool.isAlive(a.slot())),
                () -> assertTrue(pool.isAlive(b.slot()))
        );
    }

    @Test
    @DisplayName("pool cheio devolve null em vez de estourar")
    void fullPoolReturnsNull() {
        CreaturePool pool = new CreaturePool(3);
        assertNotNull(pool.spawn(0f, 0f, 0f, 0f));
        assertNotNull(pool.spawn(0f, 0f, 0f, 0f));
        assertNotNull(pool.spawn(0f, 0f, 0f, 0f));

        assertTrue(pool.isFull());
        assertNull(pool.spawn(0f, 0f, 0f, 0f));
    }

    @Test
    @DisplayName("slots são reaproveitados: mil nascimentos cabem em dez lugares")
    void slotsAreRecycled() {
        CreaturePool pool = new CreaturePool(10);

        for (int i = 0; i < 1_000; i++) {
            Creature c = pool.spawn(0f, 0f, 0f, 0f);
            assertNotNull(c, "pool esgotou na iteração " + i + " — slots não estão voltando");
            pool.despawn(c);
        }

        assertAll(
                () -> assertEquals(1_000L, pool.totalSpawns()),
                () -> assertEquals(0, pool.activeCount()),
                () -> assertEquals(10, pool.freeCount())
        );
    }

    @Test
    @DisplayName("remover do meio não perde ninguém")
    void removalFromMiddleKeepsEveryoneReachable() {
        CreaturePool pool = new CreaturePool(50);
        Creature[] spawned = new Creature[50];
        for (int i = 0; i < 50; i++) {
            spawned[i] = pool.spawn(i, i, 0f, 0f);
        }

        // Mata os pares, que é o caso em que a remoção por troca embaralha
        // a lista de ativos.
        for (int i = 0; i < 50; i += 2) {
            pool.despawn(spawned[i]);
        }

        assertEquals(25, pool.activeCount());

        boolean[] seen = new boolean[50];
        for (int i = 0; i < pool.activeCount(); i++) {
            seen[pool.activeAt(i).slot()] = true;
        }

        for (int i = 1; i < 50; i += 2) {
            assertTrue(seen[spawned[i].slot()],
                    "criatura do slot " + spawned[i].slot() + " sumiu da lista de ativos");
        }
    }

    @Test
    @DisplayName("matar duas vezes não corrompe o pool")
    void doubleDespawnIsIgnored() {
        CreaturePool pool = new CreaturePool(5);
        Creature c = pool.spawn(0f, 0f, 0f, 0f);

        pool.despawn(c);
        pool.despawn(c);

        assertAll(
                () -> assertEquals(0, pool.activeCount()),
                () -> assertEquals(5, pool.freeCount())
        );
    }

    @Test
    @DisplayName("ids são únicos e crescentes")
    void idsAreUnique() {
        CreaturePool pool = new CreaturePool(4);
        long previous = 0L;
        for (int i = 0; i < 100; i++) {
            Creature c = pool.spawn(0f, 0f, 0f, 0f);
            assertTrue(c.id > previous, "id não cresceu: " + c.id + " após " + previous);
            previous = c.id;
            pool.despawn(c);
        }
    }

    @Test
    @DisplayName("capacidade inválida é recusada")
    void rejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new CreaturePool(0));
        assertThrows(IllegalArgumentException.class, () -> new CreaturePool(-5));
    }

    @Test
    @DisplayName("índice ativo fora da faixa lança exceção")
    void activeAtIsBoundsChecked() {
        CreaturePool pool = new CreaturePool(4);
        pool.spawn(0f, 0f, 0f, 0f);

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> pool.activeAt(1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> pool.activeAt(-1))
        );
    }
}
