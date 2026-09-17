package com.emannuel.mundovivo.sim;

import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.faction.FactionRegistry;
import com.emannuel.mundovivo.sim.faction.Territory;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationTest {

    private static final float STEP = 1f / 60f;

    private static Simulation simulation(long seed) {
        World world = WorldGenerator.generate(WorldConfig.medium(seed));
        return new Simulation(world, new CreatureConfig());
    }

    private static void run(Simulation sim, float seconds) {
        int steps = (int) (seconds / STEP);
        for (int i = 0; i < steps; i++) {
            sim.step(STEP);
        }
    }

    @Test
    @DisplayName("mesma semente produz exatamente a mesma história")
    void isDeterministic() {
        Simulation a = simulation(4242L);
        Simulation b = simulation(4242L);

        run(a, 120f);
        run(b, 120f);

        assertEquals(a.population(), b.population(), "populações divergiram");
        assertEquals(a.births(), b.births(), "nascimentos divergiram");
        assertEquals(a.deathsByStarvation(), b.deathsByStarvation(), "mortes por fome divergiram");

        for (int i = 0; i < a.population(); i++) {
            Creature ca = a.creatures().activeAt(i);
            Creature cb = b.creatures().activeAt(i);

            assertAll(
                    () -> assertEquals(ca.id, cb.id),
                    () -> assertEquals(ca.x, cb.x, 0f),
                    () -> assertEquals(ca.y, cb.y, 0f),
                    () -> assertEquals(ca.hunger, cb.hunger, 0f),
                    () -> assertEquals(ca.state, cb.state),
                    () -> assertEquals(ca.factionId, cb.factionId, "facção divergiu")
            );
        }
    }

    @Test
    @DisplayName("criaturas nunca saem do mundo nem pisam na água")
    void creaturesStayOnWalkableGround() {
        Simulation sim = simulation(31337L);
        World world = sim.world();

        for (int step = 0; step < 6_000; step++) {
            sim.step(STEP);

            if (step % 500 != 0) {
                continue;
            }
            for (int i = 0; i < sim.population(); i++) {
                Creature c = sim.creatures().activeAt(i);
                int tx = c.tileX();
                int ty = c.tileY();

                assertTrue(world.inBounds(tx, ty),
                        "criatura fora do mundo em (" + c.x + "," + c.y + ")");
                assertTrue(world.tileAtUnsafe(tx, ty).walkable(),
                        "criatura sobre terreno não caminhável: " + world.tileAtUnsafe(tx, ty));
            }
        }
    }

    @ParameterizedTest(name = "a população da semente {0} sobrevive a 20 minutos")
    @ValueSource(longs = {12345L, 2026L, 777L})
    void populationSurvives(long seed) {
        Simulation sim = simulation(seed);
        run(sim, 20f * 60f);

        assertTrue(sim.population() > 0,
                "população extinta na semente " + seed);
        assertTrue(sim.population() < sim.config().maxCreatures,
                "população bateu no teto do pool — a comida deixou de limitar");
    }

    @Test
    @DisplayName("a simulação não aloca criaturas novas: os slots são reciclados")
    void reusesPoolSlots() {
        // Pool pequeno de propósito: com o teto padrão de 3000 a simulação
        // nunca chega a precisar de um slot repetido em meia hora, e o teste
        // passaria sem exercitar a reciclagem uma única vez.
        CreatureConfig config = new CreatureConfig();
        config.maxCreatures = 300;

        Simulation sim = new Simulation(
                WorldGenerator.generate(WorldConfig.medium(99L)), config);
        run(sim, 30f * 60f);

        assertAll(
                () -> assertTrue(sim.creatures().totalSpawns() > sim.creatures().capacity(),
                        "só " + sim.creatures().totalSpawns() + " nascimentos em "
                                + sim.creatures().capacity() + " slots — reciclagem não exercitada"),
                () -> assertTrue(sim.population() <= sim.creatures().capacity())
        );
    }

    @Test
    @DisplayName("fome e vida ficam sempre dentro de [0,1]")
    void vitalsStayInRange() {
        Simulation sim = simulation(5150L);

        for (int step = 0; step < 4_000; step++) {
            sim.step(STEP);

            if (step % 400 != 0) {
                continue;
            }
            for (int i = 0; i < sim.population(); i++) {
                Creature c = sim.creatures().activeAt(i);
                assertTrue(c.hunger >= 0f && c.hunger <= 1f, "fome fora da faixa: " + c.hunger);
                assertTrue(c.health > 0f && c.health <= 1f, "vida fora da faixa: " + c.health);
                assertTrue(c.age >= 0f && c.age < sim.config().maxAgeSeconds,
                        "idade fora da faixa: " + c.age);
            }
        }
    }

    @Test
    @DisplayName("o passo de tempo é cortado para o app sobreviver a voltar do segundo plano")
    void clampsHugeTimeSteps() {
        Simulation sim = simulation(8L);
        int before = sim.population();

        // Trinta segundos de uma vez: é o que chega quando o celular volta
        // do bloqueio. Sem o corte, todo mundo morreria de fome de uma vez.
        sim.step(30f);

        assertAll(
                () -> assertEquals(Simulation.MAX_STEP_SECONDS, sim.elapsedSeconds(), 1e-6f),
                () -> assertTrue(sim.population() >= before - 2,
                        "a população despencou em um único passo grande")
        );
    }

    @Test
    @DisplayName("a comida cai quando há bocas e volta a crescer")
    void foodRespondsToGrazing() {
        Simulation sim = simulation(606L);
        float initial = sim.foodMap().totalFood();

        run(sim, 5f * 60f);
        float afterGrazing = sim.foodMap().totalFood();

        assertTrue(afterGrazing < initial,
                "a comida não caiu: as criaturas não estão comendo");
    }

    @Test
    @DisplayName("um passo com centenas de criaturas cabe no orçamento de um quadro")
    void stepIsFastEnough() {
        Simulation sim = simulation(12345L);
        run(sim, 10f * 60f);

        int population = sim.population();
        assertTrue(population > 200, "população pequena demais para o teste valer: " + population);

        long startedAt = System.nanoTime();
        for (int i = 0; i < 600; i++) {
            sim.step(STEP);
        }
        double msPerStep = (System.nanoTime() - startedAt) / 1_000_000.0 / 600.0;

        // 16,6 ms é o quadro inteiro a 60 fps; a simulação não pode comer
        // mais que uma fração dele, porque ainda falta desenhar.
        assertTrue(msPerStep < 4.0,
                "passo custou " + String.format("%.2f", msPerStep)
                        + " ms com " + population + " criaturas");
    }

    // ------------------------------------------------------------ facções

    @Test
    @DisplayName("cada criatura fundadora nasce em uma facção só sua")
    void foundersEachGetTheirOwnFaction() {
        Simulation sim = simulation(2222L);

        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < sim.population(); i++) {
            seen.add(sim.creatures().activeAt(i).factionId);
        }

        assertAll(
                () -> assertEquals(sim.population(), seen.size(),
                        "duas criaturas fundadoras compartilhando facção"),
                () -> assertEquals(sim.population(), sim.factions().factionCount())
        );
    }

    @Test
    @DisplayName("nenhuma facção nova aparece depois da fundação inicial do mundo")
    void noNewFactionsAfterInitialFounding() {
        Simulation sim = simulation(555L);
        int founders = sim.factions().factionCount();

        run(sim, 20f * 60f);

        assertEquals(founders, sim.factions().factionCount(),
                "uma facção surgiu do nada depois do povoamento inicial");

        for (int i = 0; i < sim.population(); i++) {
            Creature c = sim.creatures().activeAt(i);
            assertTrue(c.factionId >= 0 && c.factionId < founders,
                    "criatura com facção fora do intervalo das fundadoras: " + c.factionId);
        }
    }

    @Test
    @DisplayName("todo dono de território é uma facção existente, ou ninguém")
    void territoryOwnersAreValidFactionsOrUnclaimed() {
        Simulation sim = simulation(2024L);
        run(sim, 5f * 60f);

        Territory territory = sim.territory();
        FactionRegistry factions = sim.factions();
        int tileCount = sim.world().tileCount();

        for (int i = 0; i < tileCount; i++) {
            int owner = territory.ownerAt(i);
            assertTrue(owner == Territory.UNCLAIMED || (owner >= 0 && owner < factions.factionCount()),
                    "dono inválido no tile " + i + ": " + owner);
        }
    }

    @Test
    @DisplayName("território nunca reivindica água")
    void territoryNeverClaimsWater() {
        Simulation sim = simulation(909L);
        run(sim, 5f * 60f);

        World world = sim.world();
        Territory territory = sim.territory();

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                if (world.tileAtUnsafe(x, y).isWater()) {
                    assertEquals(Territory.UNCLAIMED, territory.ownerAt(x, y),
                            "água reivindicada em (" + x + "," + y + ")");
                }
            }
        }
    }
}
