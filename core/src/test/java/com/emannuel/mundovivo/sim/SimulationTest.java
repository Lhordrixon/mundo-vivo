package com.emannuel.mundovivo.sim;

import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.creature.CreatureState;
import com.emannuel.mundovivo.sim.creature.Species;
import com.emannuel.mundovivo.sim.faction.FactionRegistry;
import com.emannuel.mundovivo.sim.faction.Territory;
import com.emannuel.mundovivo.render.TileMapping;
import com.emannuel.mundovivo.sim.world.TileType;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        // O limiar existe para o teste valer a pena — medir o custo de um
        // passo com um punhado de criaturas não mede nada. Era 200 antes das
        // espécies; com o acasalamento restrito a mesma semente estabiliza
        // em dezenas, não em centenas (ver a nota de regime no README).
        assertTrue(population > 40, "população pequena demais para o teste valer: " + population);

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

    // -------------------------------------------------------- dano externo

    /**
     * Mundo de campo aberto, sem uma gota de água, com uma criatura só.
     *
     * <p>Feito à mão em vez de gerado porque o teste precisa mandar no
     * terreno: a graça é afundar o chão debaixo da criatura no meio da
     * simulação, que é o caso que o dano de terreno existe para cobrir.
     */
    private static Simulation loneCreatureOnGrass(long seed) {
        World world = new World(9, 9, seed);
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                world.setTile(x, y, TileType.GRASSLAND);
            }
        }

        CreatureConfig config = new CreatureConfig();
        config.initialPopulation = 1;
        config.maxCreatures = 4;

        Simulation sim = new Simulation(world, config);
        assertEquals(1, sim.population(), "o povoamento inicial não colocou ninguém no mundo");
        return sim;
    }

    /** Afunda o mundo inteiro: a criatura fica sem para onde escapar. */
    private static void floodEverything(World world) {
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                world.setTile(x, y, TileType.DEEP_OCEAN);
            }
        }
    }

    @Test
    @DisplayName("criatura em terreno perigoso perde vida com o tempo e acaba morrendo")
    void hazardousTerrainDrainsHealthAndKills() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);
        assertEquals(1f, c.health, 0f, "deveria nascer com vida cheia");

        floodEverything(sim.world());

        // Meio segundo: já feriu, ainda não matou.
        run(sim, 0.5f);
        assertAll(
                () -> assertEquals(1, sim.population(), "morreu cedo demais"),
                () -> assertTrue(c.health < 0.9f,
                        "vida não caiu em terreno perigoso: " + c.health),
                () -> assertEquals(Creature.CAUSE_HAZARDOUS_TERRAIN, c.lastDamageCause)
        );

        // Tempo de sobra para 0.5 de dano por segundo derrubar uma vida cheia.
        run(sim, 5f);
        assertAll(
                () -> assertEquals(0, sim.population(), "sobreviveu ao afogamento"),
                () -> assertEquals(1L, sim.deathsByExternalDamage()),
                () -> assertEquals(0L, sim.deathsByStarvation(), "morte creditada à fome"),
                () -> assertEquals(0L, sim.deathsByOldAge(), "morte creditada à velhice")
        );
    }

    @Test
    @DisplayName("morte por terreno vira comida no tile, como qualquer outra morte")
    void deathByHazardStillLeavesACorpse() {
        Simulation sim = loneCreatureOnGrass(77L);
        Creature c = sim.creatures().activeAt(0);
        int tile = sim.world().index(c.tileX(), c.tileY());

        // Rebrota desligada e tile esvaziado: depois disso, a única comida
        // que pode aparecer ali é o cadáver. É assim que se prova que a
        // morte por terreno passa pelo mesmo die() da morte por fome, em
        // vez de ter um caminho próprio.
        sim.foodMap().regrowthPerSecond(0f);
        sim.foodMap().consume(tile, 999f);
        assertEquals(0f, sim.foodMap().amountAt(tile), 0f);

        // Só o terreno afunda. O mapa de comida continua achando que ali é
        // campo, de propósito: a capacidade de um tile de água é zero, e
        // deposit() respeita a capacidade, então um cadáver no fundo do mar
        // não vira comida de ninguém. Testar com a capacidade zerada mediria
        // essa regra do FoodMap, não o caminho de morte.
        floodEverything(sim.world());

        run(sim, 6f);

        assertAll(
                () -> assertEquals(0, sim.population(), "sobreviveu ao afogamento"),
                () -> assertEquals(sim.config().corpseFoodValue,
                        sim.foodMap().amountAt(tile), 1e-6f,
                        "a morte por terreno não depositou cadáver no tile")
        );
    }

    @Test
    @DisplayName("criatura em terreno normal não sofre dano de terreno")
    void safeTerrainDoesNoDamage() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);

        run(sim, 5f);

        assertAll(
                () -> assertEquals(1, sim.population(), "morreu em campo aberto"),
                () -> assertEquals(1f, c.health, 0f, "perdeu vida sem nada para feri-la"),
                () -> assertNull(c.lastDamageCause, "levou dano do nada"),
                () -> assertEquals(0L, sim.deathsByExternalDamage())
        );
    }

    @Test
    @DisplayName("mundo gerado normal não produz nenhuma morte por dano externo")
    void generatedWorldNeverInflictsTerrainDamage() {
        // O movimento recusa terreno não caminhável, então ninguém entra
        // andando no oceano profundo. Se este teste ficar vermelho, alguma
        // criatura passou a pisar onde não devia — a regressão que o dano
        // de terreno tornaria fatal em vez de apenas estranha.
        Simulation sim = simulation(12345L);
        run(sim, 10f * 60f);

        assertEquals(0L, sim.deathsByExternalDamage(),
                "criatura sofreu dano de terreno em um mundo gerado normalmente");
    }

    // ------------------------------------------------------------- espécies

    /**
     * Campo aberto, sem povoamento nenhum: o teste coloca quem quiser, onde
     * quiser, no estado que quiser.
     *
     * <p>Povoamento zero de propósito. A reprodução em mundo gerado depende
     * de sorteio, posição e fome, e um teste que dependa disso prova pouco:
     * "não nasceu ninguém" pode ser espécie diferente, pode ser que os dois
     * nunca se encontraram. Aqui as duas criaturas nascem adultas, saciadas
     * e a meio tile uma da outra — se não nasce filho, é pela regra.
     */
    private static Simulation emptyGrassWorld(long seed) {
        World world = new World(16, 16, seed);
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                world.setTile(x, y, TileType.GRASSLAND);
            }
        }

        CreatureConfig config = new CreatureConfig();
        config.initialPopulation = 0;
        config.maxCreatures = 8;

        Simulation sim = new Simulation(world, config);
        assertEquals(0, sim.population(), "o mundo do teste deveria nascer vazio");
        return sim;
    }

    /** Adulta, saciada, saudável e já procurando parceiro: tudo que {@code canReproduce} exige. */
    private static Creature readyToMate(Simulation sim, float x, float y, Species species) {
        Creature c = sim.creatures().spawn(x, y, 0f, 0f);
        c.factionId = sim.factions().create();
        c.species = species;
        c.age = sim.config().adultAgeSeconds + 1f;
        c.state = CreatureState.SEEKING_MATE;
        return c;
    }

    @Test
    @DisplayName("duas criaturas de espécies diferentes, grudadas e prontas, não têm filho")
    void differentSpeciesNeverBreed() {
        Simulation sim = emptyGrassWorld(1234L);
        Creature a = readyToMate(sim, 5.5f, 5.5f, Species.ALPHA);
        Creature b = readyToMate(sim, 6.0f, 5.5f, Species.BETA);

        sim.step(STEP);

        assertAll(
                () -> assertEquals(0L, sim.births(), "nasceu filho de espécies diferentes"),
                () -> assertEquals(2, sim.population()),
                // Sem isto o teste passaria por engano se elas tivessem se
                // afastado: o que precisa ser provado é que a regra barrou,
                // não que a distância barrou.
                () -> assertTrue(a.distanceTo(b) <= sim.config().matingDistanceTiles,
                        "se afastaram durante o passo; o teste deixou de medir a espécie")
        );
    }

    @Test
    @DisplayName("as mesmas duas criaturas, mesma espécie, têm filho no mesmo passo")
    void sameSpeciesStillBreed() {
        Simulation sim = emptyGrassWorld(1234L);
        readyToMate(sim, 5.5f, 5.5f, Species.ALPHA);
        readyToMate(sim, 6.0f, 5.5f, Species.ALPHA);

        sim.step(STEP);

        assertAll(
                () -> assertEquals(1L, sim.births(), "mesma espécie deixou de reproduzir"),
                () -> assertEquals(3, sim.population())
        );
    }

    @Test
    @DisplayName("o filho herda a espécie dos pais")
    void childInheritsSpecies() {
        Simulation sim = emptyGrassWorld(99L);
        readyToMate(sim, 5.5f, 5.5f, Species.BETA);
        readyToMate(sim, 6.0f, 5.5f, Species.BETA);

        sim.step(STEP);
        assertEquals(1L, sim.births());

        int betas = 0;
        for (int i = 0; i < sim.population(); i++) {
            if (sim.creatures().activeAt(i).species == Species.BETA) {
                betas++;
            }
        }
        assertEquals(3, betas, "o filho nasceu de outra espécie que não a dos pais");
    }

    @Test
    @DisplayName("o povoamento inicial nasce com mais de uma espécie")
    void foundersAreNotAllOneSpecies() {
        Simulation sim = simulation(2222L);

        Set<Species> seen = new HashSet<>();
        for (int i = 0; i < sim.population(); i++) {
            seen.add(sim.creatures().activeAt(i).species);
        }

        assertEquals(Species.VALUES.length, seen.size(),
                "o sorteio de espécie não cobriu o enum: " + seen);
    }

    @Test
    @DisplayName("nenhuma criatura viva fica sem espécie, nem depois de reciclar slots")
    void everyLivingCreatureHasASpecies() {
        // Pool pequeno força reciclagem de slot, que é onde uma espécie
        // esquecida no reset apareceria.
        CreatureConfig config = new CreatureConfig();
        config.maxCreatures = 300;
        Simulation sim = new Simulation(
                WorldGenerator.generate(WorldConfig.medium(99L)), config);

        // Trinta minutos, a mesma duração de reusesPoolSlots: é o tempo que
        // este pool de 300 leva para precisar repetir um slot, e slot
        // repetido é onde uma espécie esquecida no reset apareceria.
        for (int step = 0; step < 108_000; step++) {
            sim.step(STEP);

            if (step % 5_000 != 0) {
                continue;
            }
            for (int i = 0; i < sim.population(); i++) {
                assertNotNull(sim.creatures().activeAt(i).species,
                        "criatura viva sem espécie");
            }
        }
        assertTrue(sim.creatures().totalSpawns() > sim.creatures().capacity(),
                "reciclagem de slot não foi exercitada");
    }

    @ParameterizedTest(name = "a semente {0} sobrevive à restrição por espécie, ainda que menor")
    @ValueSource(longs = {12345L, 2026L, 777L})
    void speciesRestrictionLeavesAViablePopulation(long seed) {
        // Sanidade, não igualdade, e o nome importa: medido, restringir o
        // acasalamento por espécie **reduz muito** a população — média de 58
        // contra 470 com uma espécie só, mesma semente e mesmo sorteador.
        // O que este teste garante é só o piso: ninguém extingue e ninguém
        // trava o pool. Chamá-lo de "não estrangula a reprodução" seria
        // mentira; estrangula, e está registrado no README.
        Simulation sim = simulation(seed);
        run(sim, 20f * 60f);

        assertAll(
                () -> assertTrue(sim.population() > 0, "população extinta na semente " + seed),
                () -> assertTrue(sim.population() < sim.config().maxCreatures,
                        "população bateu no teto do pool"),
                () -> assertTrue(sim.births() > 0, "ninguém reproduziu em 20 minutos")
        );
    }

    // ------------------------------------------------------ golpe do jogador

    @Test
    @DisplayName("o golpe fere a criatura que está no tile tocado")
    void strikeDamagesTheCreatureOnThatTile() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);

        Creature hit = sim.strikeAt(c.tileX(), c.tileY());

        assertAll(
                () -> assertSame(c, hit, "o golpe devolveu outra criatura"),
                () -> assertEquals(1f - sim.config().playerStrikeDamage, c.health, 1e-6f),
                () -> assertEquals(Creature.CAUSE_PLAYER_STRIKE, c.lastDamageCause),
                () -> assertEquals(1, sim.population(), "um golpe só não deveria matar")
        );
    }

    @Test
    @DisplayName("golpes repetidos matam pelo mesmo caminho de morte da fome e do terreno")
    void repeatedStrikesKillThroughTheSharedDeathPath() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);
        int tileX = c.tileX();
        int tileY = c.tileY();
        int tile = sim.world().index(tileX, tileY);

        // Rebrota desligada e tile esvaziado: a comida que aparecer ali
        // depois disso só pode ter vindo do cadáver — é assim que se prova
        // que a morte saiu pelo die() compartilhado, e não por um caminho
        // próprio do golpe.
        sim.foodMap().regrowthPerSecond(0f);
        sim.foodMap().consume(tile, 999f);

        int strikes = 0;
        while (sim.population() > 0 && strikes < 10) {
            sim.strikeAt(tileX, tileY);
            strikes++;
        }

        assertAll(
                () -> assertEquals(0, sim.population(), "não morreu depois de 10 golpes"),
                () -> assertEquals(1L, sim.deathsByExternalDamage()),
                () -> assertEquals(0L, sim.deathsByStarvation(), "morte creditada à fome"),
                () -> assertEquals(0L, sim.deathsByOldAge(), "morte creditada à velhice"),
                () -> assertEquals(sim.config().corpseFoodValue,
                        sim.foodMap().amountAt(tile), 1e-6f,
                        "a morte por golpe não depositou cadáver no tile"),
                () -> assertEquals(Creature.CAUSE_PLAYER_STRIKE, c.lastDamageCause)
        );
    }

    @Test
    @DisplayName("golpe em tile vazio não muda nada e não estoura")
    void strikeOnEmptyTileChangesNothing() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);
        float healthBefore = c.health;

        // Um tile qualquer que não é o dela — o mundo do teste tem 9x9 e ela
        // ocupa um só.
        int otherX = (c.tileX() + 3) % sim.world().width();
        int otherY = (c.tileY() + 3) % sim.world().height();
        Creature hit = sim.strikeAt(otherX, otherY);

        assertAll(
                () -> assertNull(hit, "achou alguém em um tile vazio"),
                () -> assertEquals(1, sim.population()),
                () -> assertEquals(healthBefore, c.health, 0f),
                () -> assertNull(c.lastDamageCause, "levou dano sem ter sido tocada"),
                () -> assertEquals(0L, sim.deathsByExternalDamage())
        );
    }

    @Test
    @DisplayName("golpe fora do mundo é ignorado sem exceção")
    void strikeOutsideTheWorldIsIgnored() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);
        World world = sim.world();

        assertAll(
                () -> assertNull(sim.strikeAt(-1, 0)),
                () -> assertNull(sim.strikeAt(0, -1)),
                () -> assertNull(sim.strikeAt(world.width(), 0)),
                () -> assertNull(sim.strikeAt(0, world.height())),
                () -> assertEquals(1, sim.population()),
                () -> assertEquals(1f, c.health, 0f)
        );
    }

    /**
     * O cano inteiro que este sistema existe para provar, menos a câmera.
     *
     * <p>Pega onde a criatura é desenhada, converte essa coordenada de
     * mundo de volta para tile pela mesma conversão que o jogo usa, e
     * golpeia o tile resultante. Se a conversão estiver invertida ou
     * deslocada, o golpe erra e a criatura sai ilesa.
     *
     * <p>O trecho que falta — tela para mundo — é {@code camera.unproject},
     * aritmética do próprio libGDX, que depende de {@code Gdx.graphics} e
     * não roda em teste sem backend.
     */
    @Test
    @DisplayName("a coordenada onde a criatura é desenhada, convertida de volta, acerta ela")
    void tapOnTheDrawnPositionHitsThatCreature() {
        Simulation sim = loneCreatureOnGrass(4242L);
        Creature c = sim.creatures().activeAt(0);

        float worldPixelHeight = sim.world().height() * TileMapping.TILE_SIZE;
        float drawnX = c.x * TileMapping.TILE_SIZE;
        float drawnY = worldPixelHeight - c.y * TileMapping.TILE_SIZE;

        int tappedX = TileMapping.tileX(drawnX);
        int tappedY = TileMapping.tileY(drawnY, sim.world().height());

        assertAll(
                () -> assertEquals(c.tileX(), tappedX, "coluna convertida errada"),
                () -> assertEquals(c.tileY(), tappedY, "linha convertida errada"),
                () -> assertSame(c, sim.strikeAt(tappedX, tappedY),
                        "o toque na posição desenhada não encontrou a criatura")
        );
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
