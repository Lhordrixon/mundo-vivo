package com.emannuel.mundovivo.sim.world;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldGeneratorTest {

    @Test
    @DisplayName("mesma seed produz mundos byte a byte idênticos")
    void sameSeedIsReproducible() {
        World a = WorldGenerator.generate(WorldConfig.medium(42L));
        World b = WorldGenerator.generate(WorldConfig.medium(42L));

        assertAll(
                () -> assertArrayEquals(a.rawTiles(), b.rawTiles(), "tipos de terreno divergiram"),
                () -> assertArrayEquals(a.rawElevation(), b.rawElevation(), 0f, "elevações divergiram")
        );
    }

    @Test
    @DisplayName("seeds diferentes produzem mundos diferentes")
    void differentSeedsDiverge() {
        World a = WorldGenerator.generate(WorldConfig.medium(1L));
        World b = WorldGenerator.generate(WorldConfig.medium(2L));

        assertFalse(java.util.Arrays.equals(a.rawTiles(), b.rawTiles()),
                "duas seeds distintas geraram o mesmo mundo");
    }

    @Test
    @DisplayName("o mundo respeita as dimensões pedidas")
    void respectsRequestedSize() {
        WorldConfig config = new WorldConfig(7L, 64, 48);
        World world = WorldGenerator.generate(config);

        assertAll(
                () -> assertEquals(64, world.width()),
                () -> assertEquals(48, world.height()),
                () -> assertEquals(64 * 48, world.tileCount())
        );
    }

    @Test
    @DisplayName("todo tile recebe um tipo válido e elevação em [0,1]")
    void everyTileIsValid() {
        World world = WorldGenerator.generate(WorldConfig.small(99L));

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                TileType type = world.tileAt(x, y);
                assertNotNull(type, "tile nulo em (" + x + "," + y + ")");

                float e = world.elevationAt(x, y);
                assertTrue(e >= 0f && e <= 1f,
                        "elevação fora de [0,1] em (" + x + "," + y + "): " + e);
            }
        }
    }

    @ParameterizedTest(name = "seed {0} gera terra e mar em proporção jogável")
    @ValueSource(longs = {1L, 2L, 3L, 12345L, 2026L, -55L})
    void landFractionIsPlayable(long seed) {
        World world = WorldGenerator.generate(WorldConfig.medium(seed));
        float land = world.landFraction();

        assertTrue(land > 0.15f && land < 0.75f,
                "fração de terra fora da faixa jogável para a seed " + seed + ": " + land);
    }

    @Test
    @DisplayName("o mundo usa a faixa inteira de elevação: oceano profundo e montanha")
    void containsFullElevationRange() {
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

        final boolean hasDeepOcean = deepOcean;
        final boolean hasHighGround = highGround;

        assertAll(
                () -> assertTrue(hasDeepOcean, "nenhum tile de oceano profundo foi gerado"),
                () -> assertTrue(hasHighGround,
                        "nenhuma montanha gerada — a normalização de elevação regrediu")
        );
    }

    @Test
    @DisplayName("configuração inválida é rejeitada antes de gerar")
    void rejectsInvalidConfig() {
        WorldConfig negativeSize = new WorldConfig(1L, 0, 10);
        WorldConfig badSeaLevel = WorldConfig.small(1L);
        badSeaLevel.seaLevel = 1.5f;

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> WorldGenerator.generate(negativeSize)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> WorldGenerator.generate(badSeaLevel))
        );
    }

    @Test
    @DisplayName("acesso fora dos limites falha em vez de devolver lixo")
    void outOfBoundsThrows() {
        World world = WorldGenerator.generate(WorldConfig.small(1L));

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> world.tileAt(-1, 0)),
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> world.tileAt(world.width(), 0)),
                () -> assertThrows(IndexOutOfBoundsException.class,
                        () -> world.tileAt(0, world.height()))
        );
    }

    @Test
    @DisplayName("gerar o mundo padrão leva menos de 2 segundos")
    void generationIsFastEnough() {
        long startedAt = System.nanoTime();
        WorldGenerator.generate(WorldConfig.medium(4L));
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;

        assertTrue(elapsedMs < 2_000L, "geração demorou " + elapsedMs + " ms");
    }
}
