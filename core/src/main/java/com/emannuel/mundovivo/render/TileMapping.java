package com.emannuel.mundovivo.render;

/**
 * Conversão de coordenada de mundo para índice de tile.
 *
 * <p>É a metade do caminho entre o dedo e a simulação. O dedo chega em
 * coordenada de tela; a câmera converte tela em mundo; esta classe converte
 * mundo em tile. O trecho da câmera é aritmética do libGDX e não tem como
 * ser conferido sem uma janela aberta — este aqui tem, e é justamente o
 * trecho onde mora a pegadinha: a inversão vertical.
 *
 * <p><b>Por que isso não vive dentro do {@link WorldRenderer}.</b> A conta
 * é aritmética pura e não toca em nada de gráfico, mas o
 * {@code WorldRenderer} carrega um {@code Pixmap} e uma {@code Texture} no
 * construtor, que exigem biblioteca nativa e contexto de vídeo. Enquanto a
 * conta morasse lá dentro, nenhum teste conseguia alcançá-la sem abrir uma
 * janela — e o README registrava, com razão, que a orientação do mapa
 * nunca tinha sido provada. Aqui ela é testável em um JVM sem tela.
 * {@code WorldRenderer.tileX} e {@code tileY} continuam existindo e
 * continuam sendo o que o jogo chama; eles delegam para cá.
 *
 * <p><b>A inversão vertical.</b> A linha 0 do mundo é o topo, mas a origem
 * do desenho na GPU fica embaixo — por isso o mapa é desenhado com
 * {@code flipY}. Sem desfazer esse flip na volta, tocar no alto da tela
 * acertaria o tile do rodapé do mundo, e o erro passaria despercebido em
 * qualquer mapa vagamente simétrico.
 */
public final class TileMapping {

    /** Tamanho de um tile em unidades de mundo (não em pixels de tela). */
    public static final float TILE_SIZE = 8f;

    private TileMapping() {
    }

    /**
     * Coluna do tile em uma coordenada X de mundo.
     *
     * <p>Pode devolver valor fora da grade — quem chama confere com
     * {@code World.inBounds} antes de usar.
     */
    public static int tileX(float worldX) {
        return (int) Math.floor(worldX / TILE_SIZE);
    }

    /**
     * Linha do tile em uma coordenada Y de mundo, desfazendo a inversão
     * vertical aplicada no desenho.
     *
     * @param worldY            coordenada Y em unidades de mundo
     * @param worldHeightInTiles altura do mundo, em tiles
     */
    public static int tileY(float worldY, int worldHeightInTiles) {
        return (int) Math.floor((worldHeightInTiles * TILE_SIZE - worldY) / TILE_SIZE);
    }
}
