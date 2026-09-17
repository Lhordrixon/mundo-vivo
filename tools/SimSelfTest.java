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
