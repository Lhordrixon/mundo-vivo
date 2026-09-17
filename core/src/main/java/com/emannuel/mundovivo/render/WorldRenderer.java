package com.emannuel.mundovivo.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.emannuel.mundovivo.sim.world.TileType;
import com.emannuel.mundovivo.sim.world.World;

/**
 * Desenha o mundo.
 *
 * <p>Estratégia: o mundo inteiro vive em um único {@link Pixmap} de um
 * pixel por tile, enviado à GPU como uma {@link Texture} e desenhado como
 * um único quad esticado, com filtro {@code Nearest}.
 *
 * <p>A alternativa óbvia — um sprite por tile — custaria 49 mil chamadas
 * de desenho por frame em um mundo 256x192. Aqui é uma. O preço é que
 * alterar o terreno exige reenviar a textura, e é por isso que existe o
 * controle de "sujo": a transferência só acontece em frames nos quais algo
 * realmente mudou, e não a cada frame.
 *
 * <p>Esta classe é a única do projeto que conhece ao mesmo tempo o mundo e
 * o libGDX; a simulação não sabe que ela existe.
 */
public final class WorldRenderer implements Disposable {

    /**
     * Tamanho de um tile em unidades de mundo (não em pixels de tela).
     * Mora em {@link TileMapping} porque a conversão de volta, de mundo
     * para tile, precisa dele e precisa ser testável sem abrir tela.
     */
    public static final float TILE_SIZE = TileMapping.TILE_SIZE;

    private final World world;
    private final Pixmap pixmap;
    private final Texture texture;

    private boolean dirty;

    public WorldRenderer(World world) {
        this.world = world;
        this.pixmap = new Pixmap(world.width(), world.height(), Pixmap.Format.RGBA8888);
        this.pixmap.setBlending(Pixmap.Blending.None);

        paintAll();

        this.texture = new Texture(pixmap);
        this.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.dirty = false;
    }

    public World world() {
        return world;
    }

    /** Largura do mundo em unidades de mundo. */
    public float worldPixelWidth() {
        return world.width() * TILE_SIZE;
    }

    /** Altura do mundo em unidades de mundo. */
    public float worldPixelHeight() {
        return world.height() * TILE_SIZE;
    }

    /** Redesenha o pixmap inteiro a partir do estado atual do mundo. */
    public void paintAll() {
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                pixmap.drawPixel(x, y, world.tileAtUnsafe(x, y).colorRgba8888());
            }
        }
        dirty = true;
    }

    /**
     * Altera um tile no mundo e no pixmap de uma vez.
     * É por aqui que os poderes de deus deverão modificar o terreno.
     */
    public void setTile(int x, int y, TileType type) {
        world.setTile(x, y, type);
        pixmap.drawPixel(x, y, type.colorRgba8888());
        dirty = true;
    }

    /** Marca o mundo como alterado por fora (ex.: carregamento de save). */
    public void markDirty() {
        dirty = true;
    }

    public void render(SpriteBatch batch) {
        if (dirty) {
            texture.draw(pixmap, 0, 0);
            dirty = false;
        }
        // flipY = true: a linha 0 do pixmap é o topo do mundo, mas a
        // textura na GPU tem origem embaixo. Sem o flip o mapa aparece
        // espelhado e toda conversão toque -> tile sai invertida.
        batch.draw(texture,
                0f, 0f, worldPixelWidth(), worldPixelHeight(),
                0, 0, world.width(), world.height(),
                false, true);
    }

    /**
     * Converte uma coordenada X de mundo em coluna de tile.
     * Pode devolver valor fora da grade — verifique com
     * {@link World#inBounds(int, int)} antes de usar.
     */
    public int tileX(float worldX) {
        return TileMapping.tileX(worldX);
    }

    /**
     * Converte uma coordenada Y de mundo em linha de tile, já compensando
     * a inversão vertical aplicada no desenho.
     */
    public int tileY(float worldY) {
        return TileMapping.tileY(worldY, world.height());
    }

    @Override
    public void dispose() {
        texture.dispose();
        pixmap.dispose();
    }
}
