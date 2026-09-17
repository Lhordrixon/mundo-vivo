package com.emannuel.mundovivo.sim.util;

/**
 * Gerador de números pseudoaleatórios determinístico (SplitMix64).
 *
 * <p>Usado em vez de {@link java.util.Random} por três motivos:
 * é mais rápido, não usa sincronização, e o resultado é idêntico em
 * qualquer JVM/plataforma — o que torna a geração de mundo reprodutível
 * a partir de uma seed, requisito para testes e para o save/load.
 *
 * <p>Não é thread-safe e não serve para criptografia.
 */
public final class Rng {

    private static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;

    private long state;

    public Rng(long seed) {
        this.state = seed;
    }

    /** Estado interno atual — permite serializar o RNG no save do jogo. */
    public long state() {
        return state;
    }

    /** Restaura um estado salvo. */
    public void state(long state) {
        this.state = state;
    }

    public long nextLong() {
        long z = (state += GOLDEN_GAMMA);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** Inteiro em [0, bound). Tem viés desprezível para bounds pequenos (uso de jogo). */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound deve ser positivo, recebido: " + bound);
        }
        return (int) ((nextLong() >>> 33) % bound);
    }

    /** Inteiro em [min, max]. */
    public int range(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("max (" + max + ") < min (" + min + ")");
        }
        return min + nextInt(max - min + 1);
    }

    /** Float em [0, 1). */
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1p-24f;
    }

    /** Float em [min, max). */
    public float range(float min, float max) {
        return min + nextFloat() * (max - min);
    }

    /** {@code true} com a probabilidade dada (0..1). */
    public boolean chance(float probability) {
        return nextFloat() < probability;
    }
}
