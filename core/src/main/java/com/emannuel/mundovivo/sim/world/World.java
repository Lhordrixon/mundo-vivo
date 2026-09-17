package com.emannuel.mundovivo.sim.world;

/**
 * O mundo: uma grade de tiles.
 *
 * <p>Armazenado em arrays planos ({@code byte[]} para o tipo de terreno,
 * {@code float[]} para a elevação) em vez de um array de objetos Tile.
 * A diferença importa: um mundo 384x288 teria 110 mil objetos Tile, cada
 * um com cabeçalho próprio, espalhados pela heap. Em arrays planos são
 * dois blocos contíguos, cerca de 550 KB no total, percorridos em ordem
 * de memória — que é o que permite varrer o mundo inteiro por frame sem
 * pressionar o GC.
 *
 * <p>Esta classe é pura: não conhece libGDX, não desenha nada e não tem
 * estado gráfico. Isso é o que torna a simulação testável sem abrir tela.
 */
public final class World {

    private final int width;
    private final int height;
    private final long seed;

    private final byte[] tiles;
    private final float[] elevation;

    public World(int width, int height, long seed) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("dimensões inválidas: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.seed = seed;
        this.tiles = new byte[width * height];
        this.elevation = new float[width * height];
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int tileCount() {
        return tiles.length;
    }

    /** Semente que gerou este mundo — precisa ser salva junto do estado. */
    public long seed() {
        return seed;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /** Índice linear do tile. Não valida os limites: use em laços já delimitados. */
    public int index(int x, int y) {
        return y * width + x;
    }

    public TileType tileAt(int x, int y) {
        checkBounds(x, y);
        return TileType.VALUES[tiles[index(x, y)] & 0xFF];
    }

    /** Versão sem verificação de limites, para laços quentes de renderização. */
    public TileType tileAtUnsafe(int x, int y) {
        return TileType.VALUES[tiles[y * width + x] & 0xFF];
    }

    public void setTile(int x, int y, TileType type) {
        checkBounds(x, y);
        tiles[index(x, y)] = (byte) type.ordinal();
    }

    public float elevationAt(int x, int y) {
        checkBounds(x, y);
        return elevation[index(x, y)];
    }

    public void setElevation(int x, int y, float value) {
        checkBounds(x, y);
        elevation[index(x, y)] = value;
    }

    public boolean isWater(int x, int y) {
        return tileAt(x, y).isWater();
    }

    public boolean isLand(int x, int y) {
        return tileAt(x, y).isLand();
    }

    /** Quantidade de tiles de terra firme. */
    public int landTileCount() {
        int count = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (TileType.VALUES[tiles[i] & 0xFF].isLand()) {
                count++;
            }
        }
        return count;
    }

    /** Fração do mundo que é terra, em [0,1]. */
    public float landFraction() {
        return (float) landTileCount() / tiles.length;
    }

    /**
     * Acesso direto ao array de tipos, para serialização do save.
     * Devolve a referência real — não copie por frame, e não escreva nela
     * fora do código de carregamento.
     */
    public byte[] rawTiles() {
        return tiles;
    }

    /** Acesso direto ao array de elevação. Mesma ressalva de {@link #rawTiles()}. */
    public float[] rawElevation() {
        return elevation;
    }

    private void checkBounds(int x, int y) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException(
                    "tile (" + x + "," + y + ") fora do mundo " + width + "x" + height);
        }
    }
}
