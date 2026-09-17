import com.emannuel.mundovivo.sim.Simulation;
import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.creature.CreaturePool;
import com.emannuel.mundovivo.sim.ecology.FoodMap;
import com.emannuel.mundovivo.sim.noise.FractalNoise;
import com.emannuel.mundovivo.sim.world.TileType;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

import java.util.Arrays;

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

        System.out.println("\n-- Simulation --");
        simulationIsDeterministic();
        creaturesStayOnWalkableGround();
        vitalsStayInRange();
        populationSurvivesAcrossSeeds();
        poolSlotsAreReused();
        hugeTimeStepIsClamped();
        foodRespondsToGrazing();
        simulationStepIsFastEnough();

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
                    && ca.hunger == cb.hunger && ca.state == cb.state;
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
