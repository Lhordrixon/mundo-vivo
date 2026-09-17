package com.emannuel.mundovivo.sim.world;

import com.emannuel.mundovivo.sim.noise.FractalNoise;

/**
 * Gera um mundo a partir de uma {@link WorldConfig}.
 *
 * <p>O pipeline tem duas passagens sobre a grade:
 *
 * <ol>
 *   <li><b>Passagem 1 — relevo bruto.</b> Ruído fractal preenche o campo de
 *       elevação e registra o mínimo e o máximo obtidos.</li>
 *   <li><b>Passagem 2 — normalização e clima.</b> A elevação é reescalada
 *       para ocupar [0,1] inteiro, recebe curva de contraste, cristas de
 *       montanha e afundamento de borda; em seguida temperatura e umidade
 *       classificam cada tile.</li>
 * </ol>
 *
 * <p>A normalização entre as passagens não é detalhe de implementação: o
 * fBm é uma soma de oitavas e por isso concentra seus valores no meio da
 * faixa, quase nunca chegando perto de 0 ou de 1. Sem reescalar, nenhum
 * ponto do mapa alcança a altura de montanha e o mundo sai todo plano.
 *
 * <p>Separar clima de bioma (em vez de pintar bioma direto da altura) é o
 * que faz surgir deserto ao lado de selva na mesma latitude, em vez de
 * faixas horizontais uniformes.
 *
 * <p>Toda a classe é determinística: mesma config, mesmo mundo, em
 * qualquer aparelho.
 */
public final class WorldGenerator {

    /** Deslocamentos de semente para que os campos de ruído não se correlacionem. */
    private static final long SEED_ELEVATION = 0x0000000000000000L;
    private static final long SEED_MOISTURE = 0x5DEECE66DL;
    private static final long SEED_TEMPERATURE = 0x2545F4914F6CDD1DL;
    private static final long SEED_RIDGES = 0x14057B7EF767814FL;

    /** Deslocamento espacial extra, para os campos não compartilharem a origem. */
    private static final float MOISTURE_OFFSET_X = 137.31f;
    private static final float MOISTURE_OFFSET_Y = -71.17f;
    private static final float TEMPERATURE_OFFSET_X = -49.73f;
    private static final float TEMPERATURE_OFFSET_Y = 211.09f;

    /** Altura a partir da qual as cristas de montanha começam a aparecer. */
    private static final float RIDGE_ONSET = 0.55f;

    private WorldGenerator() {
    }

    public static World generate(WorldConfig config) {
        config.validate();

        final int width = config.width;
        final int height = config.height;

        World world = new World(width, height, config.seed);
        float[] elevation = world.rawElevation();

        FractalNoise elevationNoise =
                new FractalNoise(config.seed ^ SEED_ELEVATION, config.octaves, 2.0f, 0.5f);
        FractalNoise ridgeNoise =
                new FractalNoise(config.seed ^ SEED_RIDGES, 4, 2.0f, 0.5f);
        FractalNoise moistureNoise =
                new FractalNoise(config.seed ^ SEED_MOISTURE, 4, 2.0f, 0.5f);
        FractalNoise temperatureNoise =
                new FractalNoise(config.seed ^ SEED_TEMPERATURE, 3, 2.0f, 0.5f);

        // --- Passagem 1: relevo bruto + faixa real de valores ---
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;

        for (int y = 0; y < height; y++) {
            float ny = (float) y / height;
            int row = y * width;

            for (int x = 0; x < width; x++) {
                float nx = (float) x / width;
                float raw = elevationNoise.fbm01(
                        nx * config.elevationFrequency,
                        ny * config.elevationFrequency);

                elevation[row + x] = raw;
                if (raw < min) min = raw;
                if (raw > max) max = raw;
            }
        }

        float span = max - min;
        if (span < 1e-6f) {
            span = 1f;
        }

        // --- Passagem 2: normalização, relevo final e classificação ---
        for (int y = 0; y < height; y++) {
            float ny = (float) y / height;
            int row = y * width;

            for (int x = 0; x < width; x++) {
                float nx = (float) x / width;

                float e = (elevation[row + x] - min) / span;
                e = (float) Math.pow(e, config.elevationContrast);

                if (config.mountainRidges > 0f) {
                    float ridge = 1f - Math.abs(ridgeNoise.fbm(
                            nx * config.elevationFrequency * 1.7f,
                            ny * config.elevationFrequency * 1.7f));
                    e += config.mountainRidges * ridge * smoothstep(RIDGE_ONSET, 1.0f, e);
                }

                e = applyEdgeFalloff(clamp01(e), nx, ny, config.islandFalloff);

                float moisture = moistureNoise.fbm01(
                        nx * config.moistureFrequency + MOISTURE_OFFSET_X,
                        ny * config.moistureFrequency + MOISTURE_OFFSET_Y);

                float jitter = temperatureNoise.fbm01(
                        nx * config.temperatureFrequency + TEMPERATURE_OFFSET_X,
                        ny * config.temperatureFrequency + TEMPERATURE_OFFSET_Y);

                float temperature = temperature(ny, e, jitter, config);

                elevation[row + x] = e;
                world.setTile(x, y, classify(e, temperature, moisture, config.seaLevel));
            }
        }
        return world;
    }

    /**
     * Reduz a elevação perto das bordas do mapa.
     *
     * <p>A distância é medida a partir do centro e normalizada pela
     * diagonal, então o efeito é radial: o mar aparece por todos os lados
     * de forma parecida, em vez de só à esquerda e à direita.
     */
    static float applyEdgeFalloff(float elevation, float nx, float ny, float strength) {
        if (strength <= 0f) {
            return elevation;
        }
        float dx = (nx - 0.5f) * 2f;
        float dy = (ny - 0.5f) * 2f;
        float distance = (float) Math.sqrt(dx * dx + dy * dy) / 1.4142136f;
        return clamp01(elevation - strength * smoothstep(0.30f, 1.0f, distance));
    }

    /**
     * Temperatura em [0,1]: quente no equador (meio do mapa), fria nos
     * polos (topo e base), perturbada por ruído e derrubada pela altitude.
     */
    static float temperature(float ny, float elevation, float noise, WorldConfig config) {
        float latitudeWarmth = 1f - Math.abs(ny * 2f - 1f);
        float t = latitudeWarmth * (1f - config.temperatureJitter)
                + noise * config.temperatureJitter;
        float aboveSea = Math.max(0f, elevation - config.seaLevel);
        return clamp01(t - aboveSea * config.altitudeCooling);
    }

    /** Converte (elevação, temperatura, umidade) no tipo de terreno. */
    static TileType classify(float elevation, float temperature, float moisture, float seaLevel) {
        if (elevation < seaLevel - 0.12f)  return TileType.DEEP_OCEAN;
        if (elevation < seaLevel - 0.04f)  return TileType.OCEAN;
        if (elevation < seaLevel)          return TileType.SHALLOW_WATER;
        if (elevation < seaLevel + 0.015f) return TileType.BEACH;

        if (elevation > 0.90f) return TileType.SNOW_PEAK;
        if (elevation > 0.80f) return TileType.MOUNTAIN;
        if (elevation > 0.72f) return TileType.ROCK;

        if (temperature < 0.18f) {
            return TileType.SNOW;
        }
        if (temperature < 0.32f) {
            return moisture < 0.45f ? TileType.TUNDRA : TileType.TAIGA;
        }
        if (temperature < 0.60f) {
            if (moisture < 0.35f) return TileType.GRASSLAND;
            if (moisture < 0.70f) return TileType.FOREST;
            return TileType.SWAMP;
        }
        if (moisture < 0.25f) return TileType.DESERT;
        if (moisture < 0.50f) return TileType.SAVANNA;
        if (moisture < 0.80f) return TileType.JUNGLE;
        return TileType.SWAMP;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3f - 2f * t);
    }

    private static float clamp01(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }
}
