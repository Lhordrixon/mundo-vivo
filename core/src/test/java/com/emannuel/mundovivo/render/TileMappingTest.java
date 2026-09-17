package com.emannuel.mundovivo.render;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TileMappingTest {

    private static final int WORLD_HEIGHT_TILES = 192;
    private static final float TILE = TileMapping.TILE_SIZE;

    @Test
    @DisplayName("a coluna é a divisão inteira pela largura do tile")
    void columnIsFlooredDivision() {
        assertAll(
                () -> assertEquals(0, TileMapping.tileX(0f)),
                () -> assertEquals(0, TileMapping.tileX(TILE - 0.001f)),
                () -> assertEquals(1, TileMapping.tileX(TILE)),
                () -> assertEquals(1, TileMapping.tileX(TILE + 0.001f)),
                () -> assertEquals(7, TileMapping.tileX(7.5f * TILE))
        );
    }

    @Test
    @DisplayName("a linha desfaz a inversão vertical do desenho")
    void rowUndoesTheVerticalFlip() {
        float worldTop = WORLD_HEIGHT_TILES * TILE;

        assertAll(
                // O topo do desenho é a linha 0 do mundo — é exatamente o que
                // a inversão existe para corrigir.
                () -> assertEquals(0, TileMapping.tileY(worldTop, WORLD_HEIGHT_TILES)),
                () -> assertEquals(0, TileMapping.tileY(worldTop - 0.001f, WORLD_HEIGHT_TILES)),
                () -> assertEquals(1, TileMapping.tileY(worldTop - TILE, WORLD_HEIGHT_TILES)),
                // E a última linha fica junto do chão do desenho.
                () -> assertEquals(WORLD_HEIGHT_TILES - 1,
                        TileMapping.tileY(0.001f, WORLD_HEIGHT_TILES))
        );
    }

    @Test
    @DisplayName("sem a inversão, tocar em cima acertaria embaixo")
    void flipIsNotAnIdentity() {
        float worldTop = WORLD_HEIGHT_TILES * TILE;

        int rowAtTop = TileMapping.tileY(worldTop - 0.001f, WORLD_HEIGHT_TILES);
        int rowAtBottom = TileMapping.tileY(0.001f, WORLD_HEIGHT_TILES);

        // Este é o teste que pega a regressão mais provável desta conversão:
        // se alguém "simplificar" para floor(worldY / TILE_SIZE), os dois
        // valores trocam de lugar e o mapa inteiro fica de cabeça para baixo
        // sem que nada estoure.
        assertAll(
                () -> assertEquals(0, rowAtTop),
                () -> assertEquals(WORLD_HEIGHT_TILES - 1, rowAtBottom)
        );
    }

    /**
     * Ida e volta contra a fórmula de desenho.
     *
     * <p>É a prova que interessa: o {@code CreatureRenderer} coloca uma
     * criatura da linha {@code r} em {@code worldPixelHeight - c.y *
     * TILE_SIZE}; converter essa coordenada de volta tem que devolver
     * {@code r}. Se desenho e toque discordarem, o jogador mira em um bicho
     * e acerta outro — e nenhum teste de fórmula isolada pegaria isso.
     */
    @Test
    @DisplayName("ida e volta: onde a criatura é desenhada é onde o toque a encontra")
    void roundTripsAgainstTheDrawingFormula() {
        float worldTop = WORLD_HEIGHT_TILES * TILE;
        String problem = null;

        for (int row = 0; row < WORLD_HEIGHT_TILES && problem == null; row++) {
            for (int col = 0; col < 16; col++) {
                // Centro do tile, que é onde a criatura fica: x = col + 0.5.
                float creatureX = col + 0.5f;
                float creatureY = row + 0.5f;

                float drawnX = creatureX * TILE;
                float drawnY = worldTop - creatureY * TILE;

                int backCol = TileMapping.tileX(drawnX);
                int backRow = TileMapping.tileY(drawnY, WORLD_HEIGHT_TILES);

                if (backCol != col || backRow != row) {
                    problem = "tile (" + col + "," + row + ") voltou como ("
                            + backCol + "," + backRow + ")";
                    break;
                }
            }
        }
        assertEquals(null, problem, String.valueOf(problem));
    }

    @Test
    @DisplayName("coordenada fora do mundo devolve tile fora da grade, sem estourar")
    void outOfWorldCoordinatesReturnOutOfGridTiles() {
        assertAll(
                () -> assertEquals(-1, TileMapping.tileX(-0.001f)),
                () -> assertEquals(-1, TileMapping.tileY(
                        WORLD_HEIGHT_TILES * TILE + 0.001f, WORLD_HEIGHT_TILES)),
                () -> assertEquals(WORLD_HEIGHT_TILES,
                        TileMapping.tileY(-0.001f, WORLD_HEIGHT_TILES))
        );
    }
}
