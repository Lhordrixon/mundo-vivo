package com.emannuel.mundovivo.sim.world;

/**
 * Tipos de terreno do mundo.
 *
 * <p>Cada tipo carrega a cor usada na renderização (RGBA8888, o formato
 * nativo do Pixmap do libGDX) e dois atributos consumidos pela simulação:
 * se é caminhável por unidades terrestres e quanta comida o tile produz.
 *
 * <p>O array {@link #VALUES} existe porque {@code values()} clona o array
 * a cada chamada — chamá-lo dentro do laço de renderização geraria lixo
 * para o GC em cada frame.
 */
public enum TileType {

    //            cor RGBA8888    caminhável  fertilidade
    DEEP_OCEAN    (0x123A5EFF,    false,      0.00f),
    OCEAN         (0x1C5080FF,    false,      0.00f),
    SHALLOW_WATER (0x2F79A8FF,    false,      0.00f),
    BEACH         (0xD9C89BFF,    true,       0.05f),
    DESERT        (0xE0C878FF,    true,       0.02f),
    SAVANNA       (0xBFB14FFF,    true,       0.35f),
    GRASSLAND     (0x74A845FF,    true,       0.65f),
    FOREST        (0x3F7A34FF,    true,       0.80f),
    JUNGLE        (0x2A6B2AFF,    true,       0.95f),
    SWAMP         (0x4A6B4AFF,    true,       0.55f),
    TAIGA         (0x3B6352FF,    true,       0.45f),
    TUNDRA        (0x8A9A8AFF,    true,       0.15f),
    SNOW          (0xE8EEF2FF,    true,       0.05f),
    ROCK          (0x7A756EFF,    true,       0.05f),
    MOUNTAIN      (0x5C5852FF,    false,      0.00f),
    SNOW_PEAK     (0xF4F8FBFF,    false,      0.00f);

    /** Cache imutável de {@code values()} — ver nota na documentação da classe. */
    public static final TileType[] VALUES = values();

    private final int colorRgba8888;
    private final boolean walkable;
    private final float fertility;

    TileType(int colorRgba8888, boolean walkable, float fertility) {
        this.colorRgba8888 = colorRgba8888;
        this.walkable = walkable;
        this.fertility = fertility;
    }

    /** Cor no formato RGBA8888 (aceito diretamente por {@code Pixmap.drawPixel}). */
    public int colorRgba8888() {
        return colorRgba8888;
    }

    /** Cor no formato ARGB (para ferramentas Java puras, ex.: geração de PNG de prévia). */
    public int colorArgb() {
        int rgb = colorRgba8888 >>> 8;
        int alpha = colorRgba8888 & 0xFF;
        return (alpha << 24) | rgb;
    }

    /** Unidades terrestres conseguem atravessar este tile? */
    public boolean walkable() {
        return walkable;
    }

    /** Quanta comida o tile é capaz de sustentar, em [0,1]. */
    public float fertility() {
        return fertility;
    }

    /** Água de qualquer profundidade. */
    public boolean isWater() {
        return this == DEEP_OCEAN || this == OCEAN || this == SHALLOW_WATER;
    }

    public boolean isLand() {
        return !isWater();
    }

    /** Converte o id salvo em disco de volta para o enum. */
    public static TileType byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            throw new IllegalArgumentException("id de tile inválido: " + id);
        }
        return VALUES[id];
    }
}
