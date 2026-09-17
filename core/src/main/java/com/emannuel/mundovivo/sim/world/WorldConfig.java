package com.emannuel.mundovivo.sim.world;

/**
 * Parâmetros de geração de mundo.
 *
 * <p>Campos são públicos e mutáveis de propósito: isto é configuração de
 * jogo, ajustada em tela de opções e em testes, não um objeto de domínio.
 * Use {@link #validate()} antes de gerar.
 *
 * <p>As frequências são expressas em <em>ciclos ao longo do mapa</em>, não
 * em unidades de ruído. Consequência prática: mudar o tamanho do mapa não
 * muda a escala dos continentes — um mapa grande tem mais terreno, não um
 * terreno esticado.
 */
public final class WorldConfig {

    /** Semente. Mesma semente + mesmos parâmetros = mundo idêntico. */
    public long seed;

    public int width = 256;
    public int height = 192;

    /** Altura abaixo da qual o tile é água, em [0,1]. */
    public float seaLevel = 0.34f;

    /** Camadas de ruído da elevação. Mais oitavas = costas mais recortadas. */
    public int octaves = 6;

    /** Ciclos de ruído de elevação ao longo da largura do mapa. */
    public float elevationFrequency = 3.2f;

    /** Ciclos de ruído de umidade ao longo da largura do mapa. */
    public float moistureFrequency = 2.4f;

    /** Ciclos de ruído de temperatura ao longo da largura do mapa. */
    public float temperatureFrequency = 1.8f;

    /**
     * Curva aplicada à elevação já normalizada.
     * &gt; 1 afunda as terras baixas (mais mar e planície); &lt; 1 levanta tudo.
     */
    public float elevationContrast = 1.15f;

    /**
     * Peso do ruído de cristas somado ao terreno alto, em [0,1].
     * 0 desliga e as montanhas viram manchas arredondadas; valores em
     * torno de 0.35 produzem cordilheiras alongadas.
     */
    public float mountainRidges = 0.35f;

    /**
     * Força do afundamento das bordas, em [0,1].
     * 0 = terra encosta na borda do mapa; 1 = ilha isolada no centro.
     */
    public float islandFalloff = 0.38f;

    /** Quanto o ruído bagunça a faixa de temperatura por latitude, em [0,1]. */
    public float temperatureJitter = 0.14f;

    /** Quanto a altitude esfria o clima. */
    public float altitudeCooling = 1.10f;

    public WorldConfig() {
    }

    public WorldConfig(long seed, int width, int height) {
        this.seed = seed;
        this.width = width;
        this.height = height;
    }

    /** 128x96 — mundo rápido, bom para testes e aparelhos fracos. */
    public static WorldConfig small(long seed) {
        return new WorldConfig(seed, 128, 96);
    }

    /** 256x192 — padrão do jogo. */
    public static WorldConfig medium(long seed) {
        return new WorldConfig(seed, 256, 192);
    }

    /** 384x288 — mundo grande; conferir desempenho antes de liberar. */
    public static WorldConfig large(long seed) {
        return new WorldConfig(seed, 384, 288);
    }

    public WorldConfig copy() {
        WorldConfig c = new WorldConfig(seed, width, height);
        c.seaLevel = seaLevel;
        c.octaves = octaves;
        c.elevationFrequency = elevationFrequency;
        c.moistureFrequency = moistureFrequency;
        c.temperatureFrequency = temperatureFrequency;
        c.elevationContrast = elevationContrast;
        c.mountainRidges = mountainRidges;
        c.islandFalloff = islandFalloff;
        c.temperatureJitter = temperatureJitter;
        c.altitudeCooling = altitudeCooling;
        return c;
    }

    /** @throws IllegalArgumentException se algum parâmetro estiver fora da faixa aceita */
    public void validate() {
        require(width > 0 && height > 0, "width e height devem ser > 0");
        require((long) width * height <= 4_000_000L, "mundo grande demais: " + width + "x" + height);
        require(seaLevel > 0f && seaLevel < 1f, "seaLevel deve estar em (0,1)");
        require(octaves >= 1 && octaves <= 12, "octaves deve estar em [1,12]");
        require(elevationFrequency > 0f, "elevationFrequency deve ser > 0");
        require(moistureFrequency > 0f, "moistureFrequency deve ser > 0");
        require(temperatureFrequency > 0f, "temperatureFrequency deve ser > 0");
        require(elevationContrast > 0f, "elevationContrast deve ser > 0");
        require(mountainRidges >= 0f && mountainRidges <= 1f, "mountainRidges deve estar em [0,1]");
        require(islandFalloff >= 0f && islandFalloff <= 1f, "islandFalloff deve estar em [0,1]");
        require(temperatureJitter >= 0f && temperatureJitter <= 1f, "temperatureJitter deve estar em [0,1]");
        require(altitudeCooling >= 0f, "altitudeCooling deve ser >= 0");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String toString() {
        return "WorldConfig{seed=" + seed + ", " + width + "x" + height
                + ", seaLevel=" + seaLevel + ", octaves=" + octaves
                + ", islandFalloff=" + islandFalloff + '}';
    }
}
