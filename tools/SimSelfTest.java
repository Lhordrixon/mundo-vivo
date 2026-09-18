import com.emannuel.mundovivo.sim.Simulation;
import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.creature.CreaturePool;
import com.emannuel.mundovivo.sim.creature.CreatureState;
import com.emannuel.mundovivo.sim.creature.Species;
import com.emannuel.mundovivo.sim.ecology.FoodMap;
import com.emannuel.mundovivo.sim.faction.FactionRegistry;
import com.emannuel.mundovivo.sim.faction.Territory;
import com.emannuel.mundovivo.sim.noise.FractalNoise;
import com.emannuel.mundovivo.sim.world.TileType;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Verificação da simulação sem Gradle e sem JUnit.
 *
 * <p>Cobre exatamente as mesmas invariantes da suíte JUnit em
 * {@code core/src/test}. Existe porque roda em qualquer máquina com JDK,
 * sem baixar dependência nenhuma — útil para conferir rapidamente que a
 * lógica continua sã depois de mexer nos parâmetros de geração.
 *
 * <p>Uso:
 * <pre>
 *   javac -d build/sim $(find core/src/main/java/com/emannuel/mundovivo/sim -name '*.java')
 *   javac -d build/tools -cp build/sim tools/SimSelfTest.java
 *   java  -cp build/sim:build/tools SimSelfTest
 * </pre>
 *
 * <p>Sai com código 1 se qualquer verificação falhar.
 */
public final class SimSelfTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== VERIFICACAO DA SIMULACAO ===\n");

        System.out.println("-- FractalNoise --");
        noiseStaysInRange();
        noiseIsDeterministic();
        noiseSeedsDiffer();
        noiseIsZeroOnLattice();
        noiseIsContinuous();
        noiseIsWellDistributed();
        noiseRejectsInvalidParameters();

        System.out.println("\n-- WorldGenerator --");
        worldIsReproducible();
        worldSeedsDiverge();
        worldRespectsSize();
        worldTilesAreValid();
        worldLandFractionIsPlayable();
        worldUsesFullElevationRange();
        worldRejectsInvalidConfig();
        worldOutOfBoundsThrows();
        worldGenerationIsFast();

        System.out.println("\n-- FoodMap --");
        foodStartsAtCapacity();
        foodRegrowsUpToCapacity();
        foodConsumeNeverExceedsAvailable();
        waterHasNoFood();

        System.out.println("\n-- CreaturePool --");
        poolCountsStayConsistent();
        poolReturnsNullWhenFull();
        poolRecyclesSlots();
        poolRemovalFromMiddleLosesNobody();
        poolDoubleDespawnIsIgnored();

        System.out.println("\n-- FactionRegistry --");
        factionIdsAreSequential();
        newFactionStartsWithOneMember();
        joinAndLeaveAdjustMemberCount();
        leaveDownToZeroMarksExtinctionWithoutGoingNegative();
        unknownFactionIdThrows();

        System.out.println("\n-- Territory --");
        emptyTerritoryStartsUnclaimed();
        singleCreatureClaimsAllReachableLand();
        corridorSplitsAtTheMidpoint();
        waterIsNeverClaimed();
        territoryRoutesAroundWaterAndBreaksTiesByPoolOrder();

        System.out.println("\n-- Especies --");
        differentSpeciesNeverBreed();
        sameSpeciesStillBreed();
        childInheritsSpecies();
        foundersAreNotAllOneSpecies();
        everyLivingCreatureHasASpecies();

        System.out.println("\n-- Dano externo --");
        damageReducesHealthAndRecordsCause();
        lethalBlowReportsItselfAndClampsAtZero();
        damageOnAlreadyDeadIsNotLethalAgain();
        nonPositiveDamageIsIgnored();
        rebornCreatureForgetsPreviousCause();
        hazardousTerrainDrainsHealthAndKills();
        deathByHazardStillLeavesACorpse();
        safeTerrainDoesNoDamage();
        onlyDeepOceanIsHazardous();
        strikeDamagesCreatureOnThatTile();
        repeatedStrikesKillThroughTheSharedDeathPath();
        strikeOnEmptyTileChangesNothing();
        strikeOutsideTheWorldIsIgnored();

        System.out.println("\n-- Simulation --");
        simulationIsDeterministic();
        creaturesStayOnWalkableGround();
        vitalsStayInRange();
        populationSurvivesAcrossSeeds();
        poolSlotsAreReused();
        hugeTimeStepIsClamped();
        foodRespondsToGrazing();
        simulationStepIsFastEnough();
        foundersEachGetTheirOwnFaction();
        noNewFactionsAfterInitialFounding();
        territoryOwnersAreValidFactionsOrUnclaimed();
        territoryNeverClaimsWater();
        territoryRecomputeIsFastEnough();
        generatedWorldNeverInflictsTerrainDamage();

        System.out.printf("%n=== %d passaram, %d falharam ===%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ ruído

    private static void noiseStaysInRange() {
        FractalNoise noise = new FractalNoise(1234L);
        float worst = 0f;
        for (int i = 0; i < 20_000; i++) {
            float value = noise.fbm01((i % 200) * 0.137f, (i / 200f) * 0.211f);
            if (value < 0f || value > 1f) {
                worst = value;
                break;
            }
        }
        check("fbm01 permanece em [0,1]", worst == 0f, "valor fora da faixa: " + worst);
    }

    private static void noiseIsDeterministic() {
        FractalNoise a = new FractalNoise(7L);
        FractalNoise b = new FractalNoise(7L);
        boolean equal = true;
        for (int i = 0; i < 1_000 && equal; i++) {
            equal = a.fbm01(i * 0.0731f, i * -0.0417f) == b.fbm01(i * 0.0731f, i * -0.0417f);
        }
        check("mesma seed devolve os mesmos valores", equal, "divergiu");
    }

    private static void noiseSeedsDiffer() {
        FractalNoise a = new FractalNoise(1L);
        FractalNoise b = new FractalNoise(2L);
        int identical = 0;
        // Deslocamento fracionário de propósito: amostrar sobre os pontos
        // inteiros da rede compararia 0.5 com 0.5 e não testaria nada.
        for (int i = 0; i < 1_000; i++) {
            float x = i * 0.3137f + 0.5f;
            float y = i * 0.1709f + 0.25f;
            if (a.fbm01(x, y) == b.fbm01(x, y)) {
                identical++;
            }
        }
        check("seeds diferentes descorrelacionam", identical == 0, identical + "/1000 iguais");
    }

    private static void noiseIsZeroOnLattice() {
        FractalNoise a = new FractalNoise(1L);
        FractalNoise b = new FractalNoise(987654L);
        boolean allHalf = true;
        for (int i = 0; i < 20 && allHalf; i++) {
            allHalf = a.fbm01(i, i * 2) == 0.5f && b.fbm01(i, i * 2) == 0.5f;
        }
        check("vale 0.5 nos pontos inteiros da rede (propriedade documentada)",
                allHalf, "a propriedade mudou — revisar a documentação de FractalNoise");
    }

    private static void noiseIsContinuous() {
        FractalNoise noise = new FractalNoise(99L);
        float maxDelta = 0f;
        for (int i = 0; i < 2_000; i++) {
            float x = i * 0.05f;
            maxDelta = Math.max(maxDelta, Math.abs(noise.fbm01(x, 0.5f) - noise.fbm01(x + 0.001f, 0.5f)));
        }
        check("campo contínuo (sem saltos)", maxDelta < 0.05f, "maior salto: " + maxDelta);
    }

    private static void noiseIsWellDistributed() {
        FractalNoise noise = new FractalNoise(2026L);
        float sum = 0f;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        int samples = 40_000;
        for (int i = 0; i < samples; i++) {
            float value = noise.fbm01((i % 200) * 0.23f, (i / 200f) * 0.23f);
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        float mean = sum / samples;
        check("média perto de 0.5", mean > 0.42f && mean < 0.58f, "média = " + mean);
        check("usa boa parte da faixa", max - min > 0.45f, "faixa = " + (max - min));
    }

    private static void noiseRejectsInvalidParameters() {
        check("recusa octaves = 0", throwsIae(() -> new FractalNoise(1L, 0, 2f, 0.5f)), "não lançou");
        check("recusa lacunarity = 0", throwsIae(() -> new FractalNoise(1L, 4, 0f, 0.5f)), "não lançou");
        check("recusa gain = 1", throwsIae(() -> new FractalNoise(1L, 4, 2f, 1f)), "não lançou");
    }

    // ------------------------------------------------------------------ mundo

    private static void worldIsReproducible() {
        World a = WorldGenerator.generate(WorldConfig.medium(42L));
        World b = WorldGenerator.generate(WorldConfig.medium(42L));
        check("mesma seed gera mundo idêntico",
                Arrays.equals(a.rawTiles(), b.rawTiles())
                        && Arrays.equals(a.rawElevation(), b.rawElevation()),
                "arrays divergiram");
    }

    private static void worldSeedsDiverge() {
        World a = WorldGenerator.generate(WorldConfig.medium(1L));
        World b = WorldGenerator.generate(WorldConfig.medium(2L));
        check("seeds diferentes geram mundos diferentes",
                !Arrays.equals(a.rawTiles(), b.rawTiles()), "mundos iguais");
    }

    private static void worldRespectsSize() {
        World world = WorldGenerator.generate(new WorldConfig(7L, 64, 48));
        check("respeita as dimensões pedidas",
                world.width() == 64 && world.height() == 48 && world.tileCount() == 64 * 48,
                world.width() + "x" + world.height());
    }

    private static void worldTilesAreValid() {
        World world = WorldGenerator.generate(WorldConfig.small(99L));
        String problem = null;
        outer:
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                if (world.tileAt(x, y) == null) {
                    problem = "tile nulo em (" + x + "," + y + ")";
                    break outer;
                }
                float e = world.elevationAt(x, y);
                if (e < 0f || e > 1f) {
                    problem = "elevação " + e + " em (" + x + "," + y + ")";
                    break outer;
                }
            }
        }
        check("todo tile tem tipo válido e elevação em [0,1]", problem == null, problem);
    }

    private static void worldLandFractionIsPlayable() {
        long[] seeds = {1L, 2L, 3L, 12345L, 2026L, -55L};
        String problem = null;
        for (long seed : seeds) {
            float land = WorldGenerator.generate(WorldConfig.medium(seed)).landFraction();
            if (land <= 0.15f || land >= 0.75f) {
                problem = "seed " + seed + " deu " + land;
                break;
            }
        }
        check("proporção terra/mar jogável em 6 seeds", problem == null, problem);
    }

    private static void worldUsesFullElevationRange() {
        World world = WorldGenerator.generate(WorldConfig.medium(12345L));
        boolean deepOcean = false;
        boolean highGround = false;
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                TileType type = world.tileAt(x, y);
                if (type == TileType.DEEP_OCEAN) {
                    deepOcean = true;
                } else if (type == TileType.MOUNTAIN || type == TileType.SNOW_PEAK) {
                    highGround = true;
                }
            }
        }
        check("gera oceano profundo", deepOcean, "nenhum");
        check("gera montanha", highGround, "nenhuma — normalização de elevação regrediu");
    }

    private static void worldRejectsInvalidConfig() {
        WorldConfig zeroWidth = new WorldConfig(1L, 0, 10);
        WorldConfig badSea = WorldConfig.small(1L);
        badSea.seaLevel = 1.5f;

        check("recusa largura zero", throwsIae(() -> WorldGenerator.generate(zeroWidth)), "não lançou");
        check("recusa seaLevel fora de (0,1)",
                throwsIae(() -> WorldGenerator.generate(badSea)), "não lançou");
    }

    private static void worldOutOfBoundsThrows() {
        World world = WorldGenerator.generate(WorldConfig.small(1L));
        boolean all = throwsOob(() -> world.tileAt(-1, 0))
                && throwsOob(() -> world.tileAt(world.width(), 0))
                && throwsOob(() -> world.tileAt(0, world.height()));
        check("acesso fora dos limites lança exceção", all, "alguma chamada passou batido");
    }

    private static void worldGenerationIsFast() {
        long startedAt = System.nanoTime();
        WorldGenerator.generate(WorldConfig.medium(4L));
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
        check("geração do mundo padrão < 2 s", elapsedMs < 2_000L, elapsedMs + " ms");
        System.out.println("   (tempo medido: " + elapsedMs + " ms)");
    }

    // --------------------------------------------------------------- comida

    private static void foodStartsAtCapacity() {
        World world = WorldGenerator.generate(WorldConfig.small(1L));
        FoodMap food = new FoodMap(world);
        String problem = null;

        for (int i = 0; i < world.tileCount() && problem == null; i++) {
            if (Math.abs(food.amountAt(i) - food.capacityAt(i)) > 1e-6f) {
                problem = "tile " + i + ": " + food.amountAt(i) + " != " + food.capacityAt(i);
            }
        }
        check("mundo novo nasce com comida na capacidade", problem == null, problem);
    }

    private static void foodRegrowsUpToCapacity() {
        World world = WorldGenerator.generate(WorldConfig.small(2L));
        FoodMap food = new FoodMap(world);

        int fertile = -1;
        for (int i = 0; i < world.tileCount(); i++) {
            if (food.capacityAt(i) > 0.3f) {
                fertile = i;
                break;
            }
        }
        if (fertile < 0) {
            check("rebrota respeita a capacidade", false, "nenhum tile fértil no mundo de teste");
            return;
        }

        food.consume(fertile, 999f);
        boolean emptied = food.amountAt(fertile) == 0f;

        for (int i = 0; i < 20_000; i++) {
            food.regrow(1f / 60f);
        }
        float capacity = food.capacityAt(fertile);
        boolean grewBack = food.amountAt(fertile) > 0f;
        boolean neverExceeded = food.amountAt(fertile) <= capacity + 1e-6f;

        check("consumir esvazia o tile", emptied, "sobrou " + food.amountAt(fertile));
        check("comida volta a crescer", grewBack, "continuou zerado");
        check("rebrota nunca passa da capacidade", neverExceeded,
                food.amountAt(fertile) + " > " + capacity);
    }

    private static void foodConsumeNeverExceedsAvailable() {
        World world = WorldGenerator.generate(WorldConfig.small(3L));
        FoodMap food = new FoodMap(world);

        int tile = 0;
        for (int i = 0; i < world.tileCount(); i++) {
            if (food.capacityAt(i) > 0.1f) {
                tile = i;
                break;
            }
        }
        float available = food.amountAt(tile);
        float taken = food.consume(tile, available * 10f);

        check("consumo é limitado ao que existe no tile",
                Math.abs(taken - available) < 1e-6f && food.amountAt(tile) == 0f,
                "pediu " + (available * 10f) + ", levou " + taken);
    }

    private static void waterHasNoFood() {
        World world = WorldGenerator.generate(WorldConfig.medium(4L));
        FoodMap food = new FoodMap(world);
        String problem = null;

        outer:
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                if (world.tileAtUnsafe(x, y).isWater() && food.amountAt(x, y) > 0f) {
                    problem = "água com comida em (" + x + "," + y + ")";
                    break outer;
                }
            }
        }
        check("água não produz comida", problem == null, problem);
    }

    // ------------------------------------------------------------------ pool

    private static void poolCountsStayConsistent() {
        CreaturePool pool = new CreaturePool(10);
        Creature a = pool.spawn(1f, 1f, 0f, 0f);
        pool.spawn(2f, 2f, 0f, 0f);

        boolean afterSpawn = pool.activeCount() == 2 && pool.freeCount() == 8;
        pool.despawn(a);
        boolean afterDespawn = pool.activeCount() == 1 && pool.freeCount() == 9
                && !pool.isAlive(a.slot());

        check("contagens de vivos e livres batem", afterSpawn && afterDespawn,
                pool.activeCount() + " ativos, " + pool.freeCount() + " livres");
    }

    private static void poolReturnsNullWhenFull() {
        CreaturePool pool = new CreaturePool(3);
        for (int i = 0; i < 3; i++) {
            pool.spawn(0f, 0f, 0f, 0f);
        }
        check("pool cheio devolve null", pool.isFull() && pool.spawn(0f, 0f, 0f, 0f) == null,
                "não devolveu null");
    }

    private static void poolRecyclesSlots() {
        CreaturePool pool = new CreaturePool(10);
        boolean ok = true;
        for (int i = 0; i < 1_000 && ok; i++) {
            Creature c = pool.spawn(0f, 0f, 0f, 0f);
            ok = c != null;
            if (ok) {
                pool.despawn(c);
            }
        }
        check("mil nascimentos cabem em dez slots",
                ok && pool.totalSpawns() == 1_000L && pool.activeCount() == 0,
                "pool esgotou ou contagem errada");
    }

    private static void poolRemovalFromMiddleLosesNobody() {
        CreaturePool pool = new CreaturePool(50);
        Creature[] all = new Creature[50];
        for (int i = 0; i < 50; i++) {
            all[i] = pool.spawn(i, i, 0f, 0f);
        }
        for (int i = 0; i < 50; i += 2) {
            pool.despawn(all[i]);
        }

        boolean[] seen = new boolean[50];
        for (int i = 0; i < pool.activeCount(); i++) {
            seen[pool.activeAt(i).slot()] = true;
        }
        boolean allPresent = pool.activeCount() == 25;
        for (int i = 1; i < 50 && allPresent; i += 2) {
            allPresent = seen[all[i].slot()];
        }
        check("remoção por troca não perde ninguém", allPresent,
                pool.activeCount() + " ativos após matar os pares");
    }

    private static void poolDoubleDespawnIsIgnored() {
        CreaturePool pool = new CreaturePool(5);
        Creature c = pool.spawn(0f, 0f, 0f, 0f);
        pool.despawn(c);
        pool.despawn(c);
        check("matar duas vezes não corrompe o pool",
                pool.activeCount() == 0 && pool.freeCount() == 5,
                pool.activeCount() + "/" + pool.freeCount());
    }

    // -------------------------------------------------------- FactionRegistry

    private static void factionIdsAreSequential() {
        FactionRegistry factions = new FactionRegistry();
        boolean ok = factions.create() == 0 && factions.create() == 1 && factions.create() == 2;
        check("ids de facção são sequenciais a partir de zero",
                ok && factions.factionCount() == 3, "factionCount=" + factions.factionCount());
    }

    private static void newFactionStartsWithOneMember() {
        FactionRegistry factions = new FactionRegistry();
        int id = factions.create();
        check("facção nova nasce com um membro, o fundador",
                factions.memberCountOf(id) == 1 && !factions.isExtinct(id),
                "membros=" + factions.memberCountOf(id));
    }

    private static void joinAndLeaveAdjustMemberCount() {
        FactionRegistry factions = new FactionRegistry();
        int id = factions.create();
        factions.join(id);
        factions.join(id);
        boolean afterJoins = factions.memberCountOf(id) == 3;
        factions.leave(id);
        boolean afterLeave = factions.memberCountOf(id) == 2;
        check("join e leave ajustam a contagem de membros", afterJoins && afterLeave,
                "membros=" + factions.memberCountOf(id));
    }

    private static void leaveDownToZeroMarksExtinctionWithoutGoingNegative() {
        FactionRegistry factions = new FactionRegistry();
        int id = factions.create();
        factions.leave(id);
        boolean extinctAtZero = factions.memberCountOf(id) == 0 && factions.isExtinct(id);
        factions.leave(id); // morte "sobrando": não deve derrubar abaixo de zero
        check("leave até zero marca extinção sem ir negativo",
                extinctAtZero && factions.memberCountOf(id) == 0,
                "membros=" + factions.memberCountOf(id));
    }

    private static void unknownFactionIdThrows() {
        FactionRegistry factions = new FactionRegistry();
        factions.create();
        boolean all = throwsOob(() -> factions.memberCountOf(-1))
                && throwsOob(() -> factions.memberCountOf(1))
                && throwsOob(() -> factions.join(99))
                && throwsOob(() -> factions.leave(99));
        check("id de facção inexistente lança exceção em vez de devolver lixo", all,
                "alguma chamada passou batido");
    }

    // -------------------------------------------------------------- Territory

    private static World flatLand(int width, int height) {
        World world = new World(width, height, 1L);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                world.setTile(x, y, TileType.GRASSLAND);
            }
        }
        return world;
    }

    private static void emptyTerritoryStartsUnclaimed() {
        World world = flatLand(4, 4);
        Territory territory = new Territory(world);
        boolean allUnclaimed = true;
        for (int i = 0; i < world.tileCount() && allUnclaimed; i++) {
            allUnclaimed = territory.ownerAt(i) == Territory.UNCLAIMED;
        }
        check("antes do primeiro recálculo, todo tile está sem dono", allUnclaimed, "sobrou dono");
    }

    private static void singleCreatureClaimsAllReachableLand() {
        World world = flatLand(5, 3);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);
        Creature c = pool.spawn(2.5f, 1.5f, 0f, 0f);
        c.factionId = 7;
        territory.recompute(pool);

        boolean allClaimed = true;
        for (int i = 0; i < world.tileCount() && allClaimed; i++) {
            allClaimed = territory.ownerAt(i) == 7;
        }
        check("uma única criatura reivindica toda a terra que alcança",
                allClaimed && territory.tileCountOf(7) == world.tileCount(),
                "tileCountOf(7)=" + territory.tileCountOf(7) + "/" + world.tileCount());
    }

    private static void corridorSplitsAtTheMidpoint() {
        World world = flatLand(10, 1);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);
        Creature a = pool.spawn(0.5f, 0.5f, 0f, 0f);
        a.factionId = 0;
        Creature b = pool.spawn(9.5f, 0.5f, 0f, 0f);
        b.factionId = 1;
        territory.recompute(pool);

        boolean ok = true;
        for (int x = 0; x <= 4 && ok; x++) ok = territory.ownerAt(x, 0) == 0;
        for (int x = 5; x <= 9 && ok; x++) ok = territory.ownerAt(x, 0) == 1;
        check("corredor reto se divide exatamente na metade entre duas facções",
                ok && territory.tileCountOf(0) == 5 && territory.tileCountOf(1) == 5,
                "5=" + territory.tileCountOf(0) + " 5=" + territory.tileCountOf(1));
    }

    private static void waterIsNeverClaimed() {
        World world = flatLand(5, 1);
        world.setTile(2, 0, TileType.OCEAN);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);
        Creature a = pool.spawn(0.5f, 0.5f, 0f, 0f);
        a.factionId = 0;
        Creature b = pool.spawn(4.5f, 0.5f, 0f, 0f);
        b.factionId = 1;
        territory.recompute(pool);

        boolean ok = territory.ownerAt(2, 0) == Territory.UNCLAIMED
                && territory.ownerAt(0, 0) == 0 && territory.ownerAt(1, 0) == 0
                && territory.ownerAt(3, 0) == 1 && territory.ownerAt(4, 0) == 1;
        check("água nunca é reivindicada, mesmo cercada de território dos dois lados", ok, "divergiu");
    }

    private static void territoryRoutesAroundWaterAndBreaksTiesByPoolOrder() {
        World world = flatLand(3, 3);
        world.setTile(1, 1, TileType.OCEAN);
        Territory territory = new Territory(world);
        CreaturePool pool = new CreaturePool(4);
        Creature a = pool.spawn(0.5f, 1.5f, 0f, 0f);
        a.factionId = 0;
        Creature b = pool.spawn(2.5f, 1.5f, 0f, 0f);
        b.factionId = 1;
        territory.recompute(pool);

        // (1,0) e (1,2) estão a distância 2 dos dois lados, contornando a
        // água; o empate vai para A, semeado primeiro na lista de ativos.
        boolean ok = territory.ownerAt(0, 0) == 0 && territory.ownerAt(0, 1) == 0
                && territory.ownerAt(0, 2) == 0 && territory.ownerAt(1, 0) == 0
                && territory.ownerAt(1, 2) == 0 && territory.ownerAt(1, 1) == Territory.UNCLAIMED
                && territory.ownerAt(2, 0) == 1 && territory.ownerAt(2, 1) == 1
                && territory.ownerAt(2, 2) == 1;
        check("território contorna água e empates vão para quem entrou primeiro no pool",
                ok && territory.tileCountOf(0) == 5 && territory.tileCountOf(1) == 3,
                "5=" + territory.tileCountOf(0) + " 3=" + territory.tileCountOf(1));
    }

    // ------------------------------------------------------------ simulação

    private static final float STEP = 1f / 60f;

    private static Simulation simulation(long seed) {
        return new Simulation(WorldGenerator.generate(WorldConfig.medium(seed)), new CreatureConfig());
    }

    private static void run(Simulation sim, float seconds) {
        int steps = (int) (seconds / STEP);
        for (int i = 0; i < steps; i++) {
            sim.step(STEP);
        }
    }

    private static void simulationIsDeterministic() {
        Simulation a = simulation(4242L);
        Simulation b = simulation(4242L);
        run(a, 120f);
        run(b, 120f);

        boolean same = a.population() == b.population()
                && a.births() == b.births()
                && a.deathsByStarvation() == b.deathsByStarvation();

        for (int i = 0; i < a.population() && same; i++) {
            Creature ca = a.creatures().activeAt(i);
            Creature cb = b.creatures().activeAt(i);
            same = ca.id == cb.id && ca.x == cb.x && ca.y == cb.y
                    && ca.hunger == cb.hunger && ca.state == cb.state
                    && ca.factionId == cb.factionId;
        }
        check("mesma semente produz a mesma história", same,
                a.population() + " vs " + b.population() + " criaturas");
    }

    private static void creaturesStayOnWalkableGround() {
        Simulation sim = simulation(31337L);
        World world = sim.world();
        String problem = null;

        for (int step = 0; step < 6_000 && problem == null; step++) {
            sim.step(STEP);
            if (step % 500 != 0) {
                continue;
            }
            for (int i = 0; i < sim.population(); i++) {
                Creature c = sim.creatures().activeAt(i);
                if (!world.inBounds(c.tileX(), c.tileY())) {
                    problem = "fora do mundo em (" + c.x + "," + c.y + ")";
                    break;
                }
                if (!world.tileAtUnsafe(c.tileX(), c.tileY()).walkable()) {
                    problem = "sobre " + world.tileAtUnsafe(c.tileX(), c.tileY());
                    break;
                }
            }
        }
        check("criaturas nunca saem do mundo nem pisam na água", problem == null, problem);
    }

    private static void vitalsStayInRange() {
        Simulation sim = simulation(5150L);
        String problem = null;

        for (int step = 0; step < 4_000 && problem == null; step++) {
            sim.step(STEP);
            if (step % 400 != 0) {
                continue;
            }
            for (int i = 0; i < sim.population(); i++) {
                Creature c = sim.creatures().activeAt(i);
                if (c.hunger < 0f || c.hunger > 1f) {
                    problem = "fome " + c.hunger;
                    break;
                }
                if (c.health <= 0f || c.health > 1f) {
                    problem = "vida " + c.health;
                    break;
                }
                if (c.age < 0f || c.age >= sim.config().maxAgeSeconds) {
                    problem = "idade " + c.age;
                    break;
                }
            }
        }
        check("fome, vida e idade sempre dentro da faixa", problem == null, problem);
    }

    private static void populationSurvivesAcrossSeeds() {
        long[] seeds = {12345L, 2026L, 777L};
        StringBuilder detail = new StringBuilder();
        boolean ok = true;

        for (long seed : seeds) {
            Simulation sim = simulation(seed);
            run(sim, 20f * 60f);
            detail.append(seed).append("=").append(sim.population()).append(" ");
            if (sim.population() <= 0 || sim.population() >= sim.config().maxCreatures) {
                ok = false;
            }
        }
        check("população sobrevive 20 min sem extinguir nem bater no teto", ok, detail.toString());
        System.out.println("   (populações finais: " + detail.toString().trim() + ")");
    }

    private static void poolSlotsAreReused() {
        // Pool pequeno de propósito: com o teto padrão de 3000 a simulação
        // nunca chega a precisar de um slot repetido, e o teste passaria
        // sem exercitar a reciclagem uma única vez.
        CreatureConfig config = new CreatureConfig();
        config.maxCreatures = 300;

        Simulation sim = new Simulation(
                WorldGenerator.generate(WorldConfig.medium(99L)), config);
        run(sim, 30f * 60f);

        check("a simulação recicla slots em vez de alocar",
                sim.creatures().totalSpawns() > sim.creatures().capacity()
                        && sim.population() <= sim.creatures().capacity(),
                sim.creatures().totalSpawns() + " nascimentos em "
                        + sim.creatures().capacity() + " slots");
        System.out.println("   (" + sim.creatures().totalSpawns() + " nascimentos passaram por "
                + sim.creatures().capacity() + " slots; "
                + sim.population() + " vivas no fim)");
    }

    private static void hugeTimeStepIsClamped() {
        Simulation sim = simulation(8L);
        int before = sim.population();
        sim.step(30f);

        check("passo gigante é cortado",
                Math.abs(sim.elapsedSeconds() - Simulation.MAX_STEP_SECONDS) < 1e-6f
                        && sim.population() >= before - 2,
                "decorrido=" + sim.elapsedSeconds() + " pop " + before + "->" + sim.population());
    }

    private static void foodRespondsToGrazing() {
        Simulation sim = simulation(606L);
        float initial = sim.foodMap().totalFood();
        run(sim, 5f * 60f);
        float after = sim.foodMap().totalFood();

        check("a comida cai conforme as criaturas pastam", after < initial,
                initial + " -> " + after);
    }

    private static void simulationStepIsFastEnough() {
        Simulation sim = simulation(12345L);
        run(sim, 10f * 60f);
        int population = sim.population();

        long startedAt = System.nanoTime();
        for (int i = 0; i < 600; i++) {
            sim.step(STEP);
        }
        double msPerStep = (System.nanoTime() - startedAt) / 1_000_000.0 / 600.0;

        check("um passo cabe folgado no quadro de 16,6 ms", msPerStep < 4.0,
                String.format("%.2f ms", msPerStep));
        System.out.printf("   (%.3f ms por passo com %d criaturas)%n", msPerStep, population);
    }

    private static void foundersEachGetTheirOwnFaction() {
        Simulation sim = simulation(2222L);
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < sim.population(); i++) {
            seen.add(sim.creatures().activeAt(i).factionId);
        }
        check("cada criatura fundadora nasce em uma facção só sua",
                seen.size() == sim.population() && sim.factions().factionCount() == sim.population(),
                seen.size() + " facções para " + sim.population() + " fundadoras");
    }

    private static void noNewFactionsAfterInitialFounding() {
        Simulation sim = simulation(555L);
        int founders = sim.factions().factionCount();
        run(sim, 20f * 60f);

        boolean noGrowth = founders == sim.factions().factionCount();
        boolean allInRange = true;
        for (int i = 0; i < sim.population() && allInRange; i++) {
            int f = sim.creatures().activeAt(i).factionId;
            allInRange = f >= 0 && f < founders;
        }
        check("nenhuma facção nova aparece depois da fundação inicial",
                noGrowth && allInRange,
                founders + " -> " + sim.factions().factionCount() + " facções");
    }

    private static void territoryOwnersAreValidFactionsOrUnclaimed() {
        Simulation sim = simulation(2024L);
        run(sim, 5f * 60f);

        Territory territory = sim.territory();
        FactionRegistry factions = sim.factions();
        int tileCount = sim.world().tileCount();
        String problem = null;

        for (int i = 0; i < tileCount && problem == null; i++) {
            int owner = territory.ownerAt(i);
            if (owner != Territory.UNCLAIMED && (owner < 0 || owner >= factions.factionCount())) {
                problem = "tile " + i + " com dono inválido: " + owner;
            }
        }
        check("todo dono de território é uma facção existente, ou ninguém", problem == null, problem);
    }

    private static void territoryNeverClaimsWater() {
        Simulation sim = simulation(909L);
        run(sim, 5f * 60f);

        World world = sim.world();
        Territory territory = sim.territory();
        String problem = null;

        outer:
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                if (world.tileAtUnsafe(x, y).isWater() && territory.ownerAt(x, y) != Territory.UNCLAIMED) {
                    problem = "água reivindicada em (" + x + "," + y + ")";
                    break outer;
                }
            }
        }
        check("território nunca reivindica água", problem == null, problem);
    }

    private static void territoryRecomputeIsFastEnough() {
        Simulation sim = simulation(12345L);
        run(sim, 10f * 60f);
        int population = sim.population();

        long startedAt = System.nanoTime();
        for (int i = 0; i < 30; i++) {
            sim.territory().recompute(sim.creatures());
        }
        double msPerRecompute = (System.nanoTime() - startedAt) / 1_000_000.0 / 30.0;

        // O recálculo só acontece uma vez por segundo (TERRITORY_RECOMPUTE_
        // INTERVAL_SECONDS), então mesmo bem mais caro que um passo normal
        // ele não pode custar perto de um segundo inteiro de quadros.
        check("recálculo de território cabe folgado no intervalo de um segundo",
                msPerRecompute < 50.0, String.format("%.2f ms", msPerRecompute));
        System.out.printf("   (%.3f ms por recálculo com %d criaturas)%n", msPerRecompute, population);
    }

    // ----------------------------------------------------------- dano externo

    private static Creature livingCreature() {
        return new CreaturePool(4).spawn(0f, 0f, 0f, 0f);
    }

    private static void damageReducesHealthAndRecordsCause() {
        Creature c = livingCreature();
        boolean lethal = c.applyDamage(0.25f, Creature.CAUSE_HAZARDOUS_TERRAIN);
        check("dano tira vida e registra a causa",
                Math.abs(c.health - 0.75f) < 1e-6f && !lethal
                        && Creature.CAUSE_HAZARDOUS_TERRAIN.equals(c.lastDamageCause),
                "vida=" + c.health + " causa=" + c.lastDamageCause);
    }

    private static void lethalBlowReportsItselfAndClampsAtZero() {
        Creature c = livingCreature();
        boolean lethal = c.applyDamage(5f, Creature.CAUSE_HAZARDOUS_TERRAIN);
        check("o golpe que zera a vida avisa que foi fatal, e a vida para em zero",
                lethal && c.health == 0f, "fatal=" + lethal + " vida=" + c.health);
    }

    private static void damageOnAlreadyDeadIsNotLethalAgain() {
        Creature c = livingCreature();
        c.applyDamage(1f, Creature.CAUSE_STARVATION);
        boolean lethalAgain = c.applyDamage(1f, "combate");
        check("bater em quem já morreu não conta uma segunda morte",
                !lethalAgain && c.health == 0f
                        && Creature.CAUSE_STARVATION.equals(c.lastDamageCause),
                "fatal=" + lethalAgain + " causa=" + c.lastDamageCause);
    }

    private static void nonPositiveDamageIsIgnored() {
        Creature c = livingCreature();
        c.applyDamage(0.2f, Creature.CAUSE_STARVATION);
        c.applyDamage(0f, "nada");
        c.applyDamage(-0.5f, "cura disfarçada de dano");
        check("dano zero ou negativo é ignorado e não apaga a causa anterior",
                Math.abs(c.health - 0.8f) < 1e-6f
                        && Creature.CAUSE_STARVATION.equals(c.lastDamageCause),
                "vida=" + c.health + " causa=" + c.lastDamageCause);
    }

    private static void rebornCreatureForgetsPreviousCause() {
        CreaturePool pool = new CreaturePool(1);
        Creature c = pool.spawn(0f, 0f, 0f, 0f);
        c.applyDamage(1f, Creature.CAUSE_HAZARDOUS_TERRAIN);
        pool.despawn(c);
        Creature reborn = pool.spawn(0f, 0f, 0f, 0f);
        check("renascer no mesmo slot limpa a causa do dano da vida anterior",
                reborn.health == 1f && reborn.lastDamageCause == null,
                "vida=" + reborn.health + " causa=" + reborn.lastDamageCause);
    }

    /** Campo aberto, sem água, com uma criatura só — terreno sob controle do teste. */
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
        return new Simulation(world, config);
    }

    private static void floodEverything(World world) {
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                world.setTile(x, y, TileType.DEEP_OCEAN);
            }
        }
    }

    private static void hazardousTerrainDrainsHealthAndKills() {
        Simulation sim = loneCreatureOnGrass(4242L);
        if (sim.population() != 1) {
            check("criatura em terreno perigoso perde vida e morre", false,
                    "povoamento inicial não colocou ninguém no mundo");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        floodEverything(sim.world());

        run(sim, 0.5f);
        boolean hurtButAlive = sim.population() == 1 && c.health < 0.9f
                && Creature.CAUSE_HAZARDOUS_TERRAIN.equals(c.lastDamageCause);

        run(sim, 5f);
        boolean died = sim.population() == 0 && sim.deathsByExternalDamage() == 1L
                && sim.deathsByStarvation() == 0L && sim.deathsByOldAge() == 0L;

        check("criatura em terreno perigoso perde vida com o tempo e acaba morrendo",
                hurtButAlive && died,
                "ferida=" + hurtButAlive + " morta=" + died
                        + " (externas=" + sim.deathsByExternalDamage()
                        + " fome=" + sim.deathsByStarvation() + ")");
    }

    private static void deathByHazardStillLeavesACorpse() {
        Simulation sim = loneCreatureOnGrass(77L);
        if (sim.population() != 1) {
            check("morte por terreno vira comida no tile", false, "ninguém para matar");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        int tile = sim.world().index(c.tileX(), c.tileY());

        // Rebrota desligada e tile esvaziado: a única comida que pode
        // aparecer ali depois disso é o cadáver. Só o terreno afunda — o
        // mapa de comida continua achando que ali é campo, porque um tile
        // de água tem capacidade zero e deposit() respeita a capacidade,
        // então um corpo no fundo do mar não vira comida de ninguém.
        sim.foodMap().regrowthPerSecond(0f);
        sim.foodMap().consume(tile, 999f);
        floodEverything(sim.world());

        run(sim, 6f);
        check("morte por terreno vira comida no tile, como qualquer outra morte",
                sim.population() == 0
                        && Math.abs(sim.foodMap().amountAt(tile) - sim.config().corpseFoodValue) < 1e-6f,
                "pop=" + sim.population() + " comida=" + sim.foodMap().amountAt(tile));
    }

    private static void safeTerrainDoesNoDamage() {
        Simulation sim = loneCreatureOnGrass(4242L);
        if (sim.population() != 1) {
            check("criatura em terreno normal não sofre dano", false, "ninguém no mundo");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        run(sim, 5f);
        check("criatura em terreno normal não sofre dano de terreno",
                sim.population() == 1 && c.health == 1f && c.lastDamageCause == null
                        && sim.deathsByExternalDamage() == 0L,
                "vida=" + c.health + " causa=" + c.lastDamageCause);
    }

    private static void onlyDeepOceanIsHazardous() {
        StringBuilder hazards = new StringBuilder();
        for (TileType type : TileType.VALUES) {
            if (type.hazardous()) {
                hazards.append(type).append(' ');
            }
        }
        check("só o oceano profundo é terreno perigoso",
                TileType.DEEP_OCEAN.hazardous() && hazards.toString().trim().equals("DEEP_OCEAN"),
                "perigosos: " + hazards);
    }

    private static void generatedWorldNeverInflictsTerrainDamage() {
        // O movimento recusa terreno não caminhável: em mundo gerado
        // ninguém pisa no oceano profundo, então o dano de terreno não pode
        // mudar em nada o equilíbrio que já existia.
        Simulation sim = simulation(12345L);
        run(sim, 10f * 60f);
        check("mundo gerado normal não produz morte por dano externo",
                sim.deathsByExternalDamage() == 0L,
                sim.deathsByExternalDamage() + " mortes por dano de terreno");
    }

    private static void strikeDamagesCreatureOnThatTile() {
        Simulation sim = loneCreatureOnGrass(4242L);
        if (sim.population() != 1) {
            check("o golpe fere a criatura no tile tocado", false, "ninguém no mundo");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        Creature hit = sim.strikeAt(c.tileX(), c.tileY());

        check("o golpe fere a criatura que está no tile tocado",
                hit == c
                        && Math.abs(c.health - (1f - sim.config().playerStrikeDamage)) < 1e-6f
                        && Creature.CAUSE_PLAYER_STRIKE.equals(c.lastDamageCause)
                        && sim.population() == 1,
                "vida=" + c.health + " causa=" + c.lastDamageCause + " pop=" + sim.population());
    }

    private static void repeatedStrikesKillThroughTheSharedDeathPath() {
        Simulation sim = loneCreatureOnGrass(4242L);
        if (sim.population() != 1) {
            check("golpes repetidos matam pelo caminho compartilhado", false, "ninguém no mundo");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        int tileX = c.tileX();
        int tileY = c.tileY();
        int tile = sim.world().index(tileX, tileY);

        // Sem rebrota e com o tile vazio, a comida que aparecer ali só pode
        // ter vindo do cadáver — prova que a morte saiu pelo die() de sempre.
        sim.foodMap().regrowthPerSecond(0f);
        sim.foodMap().consume(tile, 999f);

        int strikes = 0;
        while (sim.population() > 0 && strikes < 10) {
            sim.strikeAt(tileX, tileY);
            strikes++;
        }

        check("golpes repetidos matam pelo mesmo caminho de morte da fome e do terreno",
                sim.population() == 0
                        && sim.deathsByExternalDamage() == 1L
                        && sim.deathsByStarvation() == 0L
                        && sim.deathsByOldAge() == 0L
                        && Math.abs(sim.foodMap().amountAt(tile) - sim.config().corpseFoodValue) < 1e-6f,
                "pop=" + sim.population() + " externas=" + sim.deathsByExternalDamage()
                        + " comida=" + sim.foodMap().amountAt(tile) + " golpes=" + strikes);
    }

    private static void strikeOnEmptyTileChangesNothing() {
        Simulation sim = loneCreatureOnGrass(4242L);
        if (sim.population() != 1) {
            check("golpe em tile vazio não muda nada", false, "ninguém no mundo");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        int otherX = (c.tileX() + 3) % sim.world().width();
        int otherY = (c.tileY() + 3) % sim.world().height();
        Creature hit = sim.strikeAt(otherX, otherY);

        check("golpe em tile vazio não muda nada e não estoura",
                hit == null && sim.population() == 1 && c.health == 1f
                        && c.lastDamageCause == null && sim.deathsByExternalDamage() == 0L,
                "hit=" + hit + " vida=" + c.health + " causa=" + c.lastDamageCause);
    }

    private static void strikeOutsideTheWorldIsIgnored() {
        Simulation sim = loneCreatureOnGrass(4242L);
        if (sim.population() != 1) {
            check("golpe fora do mundo é ignorado", false, "ninguém no mundo");
            return;
        }
        Creature c = sim.creatures().activeAt(0);
        World world = sim.world();

        boolean allNull = sim.strikeAt(-1, 0) == null
                && sim.strikeAt(0, -1) == null
                && sim.strikeAt(world.width(), 0) == null
                && sim.strikeAt(0, world.height()) == null;

        check("golpe fora do mundo é ignorado sem exceção",
                allNull && sim.population() == 1 && c.health == 1f,
                "pop=" + sim.population() + " vida=" + c.health);
    }

    // -------------------------------------------------------------- espécies

    /** Campo aberto e vazio: o teste coloca quem quiser, onde quiser. */
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
        return new Simulation(world, config);
    }

    /** Adulta, saciada, saudável e já procurando parceiro. */
    private static Creature readyToMate(Simulation sim, float x, float y, Species species) {
        Creature c = sim.creatures().spawn(x, y, 0f, 0f);
        c.factionId = sim.factions().create();
        c.species = species;
        c.age = sim.config().adultAgeSeconds + 1f;
        c.state = CreatureState.SEEKING_MATE;
        return c;
    }

    private static void differentSpeciesNeverBreed() {
        Simulation sim = emptyGrassWorld(1234L);
        Creature a = readyToMate(sim, 5.5f, 5.5f, Species.ALPHA);
        Creature b = readyToMate(sim, 6.0f, 5.5f, Species.BETA);
        sim.step(STEP);

        // A distância entra na checagem para o teste não passar por engano
        // caso as duas tenham se afastado em vez de terem sido barradas.
        check("duas criaturas de espécies diferentes, grudadas e prontas, não têm filho",
                sim.births() == 0L && sim.population() == 2
                        && a.distanceTo(b) <= sim.config().matingDistanceTiles,
                "nascimentos=" + sim.births() + " distancia=" + a.distanceTo(b));
    }

    private static void sameSpeciesStillBreed() {
        Simulation sim = emptyGrassWorld(1234L);
        readyToMate(sim, 5.5f, 5.5f, Species.ALPHA);
        readyToMate(sim, 6.0f, 5.5f, Species.ALPHA);
        sim.step(STEP);

        check("as mesmas duas criaturas, mesma espécie, têm filho no mesmo passo",
                sim.births() == 1L && sim.population() == 3,
                "nascimentos=" + sim.births() + " pop=" + sim.population());
    }

    private static void childInheritsSpecies() {
        Simulation sim = emptyGrassWorld(99L);
        readyToMate(sim, 5.5f, 5.5f, Species.BETA);
        readyToMate(sim, 6.0f, 5.5f, Species.BETA);
        sim.step(STEP);

        int betas = 0;
        for (int i = 0; i < sim.population(); i++) {
            if (sim.creatures().activeAt(i).species == Species.BETA) {
                betas++;
            }
        }
        check("o filho herda a espécie dos pais",
                sim.births() == 1L && betas == 3,
                "nascimentos=" + sim.births() + " da especie dos pais=" + betas);
    }

    private static void foundersAreNotAllOneSpecies() {
        Simulation sim = simulation(2222L);
        Set<Species> seen = new HashSet<>();
        for (int i = 0; i < sim.population(); i++) {
            seen.add(sim.creatures().activeAt(i).species);
        }
        check("o povoamento inicial nasce com mais de uma espécie",
                seen.size() == Species.VALUES.length, "especies sorteadas: " + seen);
    }

    private static void everyLivingCreatureHasASpecies() {
        CreatureConfig config = new CreatureConfig();
        config.maxCreatures = 300;
        Simulation sim = new Simulation(
                WorldGenerator.generate(WorldConfig.medium(99L)), config);
        String problem = null;

        for (int step = 0; step < 108_000 && problem == null; step++) {
            sim.step(STEP);
            if (step % 5_000 != 0) {
                continue;
            }
            for (int i = 0; i < sim.population(); i++) {
                if (sim.creatures().activeAt(i).species == null) {
                    problem = "criatura viva sem espécie no passo " + step;
                    break;
                }
            }
        }
        check("nenhuma criatura viva fica sem espécie, nem depois de reciclar slots",
                problem == null, problem);
    }

    // ----------------------------------------------------------------- apoio

    private static boolean throwsIae(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    private static boolean throwsOob(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IndexOutOfBoundsException expected) {
            return true;
        }
    }

    private static void check(String name, boolean condition, String detail) {
        if (condition) {
            passed++;
            System.out.println("  [OK]    " + name);
        } else {
            failed++;
            System.out.println("  [FALHA] " + name + " -> " + detail);
        }
    }
}
