package com.emannuel.mundovivo.sim.noise;

/**
 * Ruído fractal (fBm) sobre ruído de gradiente 2D.
 *
 * <p>Implementação própria, sem tabela de permutação pré-calculada: os
 * gradientes saem de um hash inteiro das coordenadas. Isso significa:
 *
 * <ul>
 *   <li>determinismo total — mesma seed e mesmas coordenadas produzem
 *       exatamente o mesmo valor em qualquer JVM;</li>
 *   <li>domínio infinito — não há repetição a cada N unidades;</li>
 *   <li>zero alocação por chamada — pode ser usado dentro do laço de
 *       geração sem gerar lixo para o GC.</li>
 * </ul>
 *
 * <p>Instâncias são imutáveis e podem ser compartilhadas entre threads.
 *
 * <p><b>Característica conhecida:</b> como todo ruído de gradiente, o valor
 * bruto é exatamente zero nos pontos de coordenadas inteiras — ou seja,
 * {@code fbm01(3, 2)} devolve 0.5 para qualquer semente. Isso não afeta a
 * geração de mundo, porque as oitavas seguintes usam frequências diferentes
 * e não zeram no mesmo lugar, mas é uma armadilha ao escrever testes:
 * amostrar em passos que caem sobre a rede compara sempre o mesmo 0.5 e dá
 * a impressão falsa de que sementes distintas produzem campos iguais.
 */
public final class FractalNoise {

    private static final long HASH_X = 0x9E3779B97F4A7C15L;
    private static final long HASH_Y = 0xC2B2AE3D27D4EB4FL;
    private static final long OCTAVE_STRIDE = 0x9E3779B9L;

    /** Compensa a magnitude máxima do conjunto de gradientes (~1/raiz(2)). */
    private static final float AMPLITUDE_FIX = 1.4142136f;

    private final long seed;
    private final int octaves;
    private final float lacunarity;
    private final float gain;

    public FractalNoise(long seed) {
        this(seed, 6, 2.0f, 0.5f);
    }

    /**
     * @param seed       semente do gerador
     * @param octaves    número de camadas somadas (mais = mais detalhe, mais custo)
     * @param lacunarity multiplicador de frequência por oitava (típico: 2.0)
     * @param gain       multiplicador de amplitude por oitava (típico: 0.5)
     */
    public FractalNoise(long seed, int octaves, float lacunarity, float gain) {
        if (octaves < 1) {
            throw new IllegalArgumentException("octaves deve ser >= 1, recebido: " + octaves);
        }
        if (lacunarity <= 0f) {
            throw new IllegalArgumentException("lacunarity deve ser > 0, recebido: " + lacunarity);
        }
        if (gain <= 0f || gain >= 1f) {
            throw new IllegalArgumentException("gain deve estar em (0,1), recebido: " + gain);
        }
        this.seed = seed;
        this.octaves = octaves;
        this.lacunarity = lacunarity;
        this.gain = gain;
    }

    public long seed() {
        return seed;
    }

    public int octaves() {
        return octaves;
    }

    /** fBm normalizado para [0,1]. */
    public float fbm01(float x, float y) {
        return clamp01(fbm(x, y) * 0.5f + 0.5f);
    }

    /** fBm bruto, aproximadamente em [-1,1]. */
    public float fbm(float x, float y) {
        float amplitude = 1f;
        float frequency = 1f;
        float sum = 0f;
        float normaliser = 0f;

        for (int i = 0; i < octaves; i++) {
            sum += amplitude * gradientNoise(x * frequency, y * frequency, seed + i * OCTAVE_STRIDE);
            normaliser += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }
        return (sum / normaliser) * AMPLITUDE_FIX;
    }

    /** Uma única oitava de ruído de gradiente, em ~[-0.71, 0.71]. */
    static float gradientNoise(float x, float y, long seed) {
        int x0 = floor(x);
        int y0 = floor(y);
        float fx = x - x0;
        float fy = y - y0;

        float u = fade(fx);
        float v = fade(fy);

        float n00 = grad(hash(x0, y0, seed), fx, fy);
        float n10 = grad(hash(x0 + 1, y0, seed), fx - 1f, fy);
        float n01 = grad(hash(x0, y0 + 1, seed), fx, fy - 1f);
        float n11 = grad(hash(x0 + 1, y0 + 1, seed), fx - 1f, fy - 1f);

        return lerp(lerp(n00, n10, u), lerp(n01, n11, u), v);
    }

    private static long hash(int x, int y, long seed) {
        long h = seed ^ (x * HASH_X) ^ (y * HASH_Y);
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return h;
    }

    /** Oito direções de gradiente, todas com magnitude 1. */
    private static float grad(long h, float dx, float dy) {
        switch ((int) (h & 7L)) {
            case 0:  return dx;
            case 1:  return -dx;
            case 2:  return dy;
            case 3:  return -dy;
            case 4:  return (dx + dy) * 0.70710678f;
            case 5:  return (dx - dy) * 0.70710678f;
            case 6:  return (-dx + dy) * 0.70710678f;
            default: return (-dx - dy) * 0.70710678f;
        }
    }

    /** Curva de suavização de Perlin: 6t^5 - 15t^4 + 10t^3. */
    private static float fade(float t) {
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static int floor(float value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private static float clamp01(float value) {
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }
}
