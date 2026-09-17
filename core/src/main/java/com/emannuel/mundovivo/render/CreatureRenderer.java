package com.emannuel.mundovivo.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.emannuel.mundovivo.sim.Simulation;
import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureState;

/**
 * Desenha as criaturas por cima do terreno.
 *
 * <p>Ao contrário do terreno, que é uma textura só, cada criatura é um quad
 * — elas se movem toda hora e reenviar a textura do mundo a cada passo
 * seria muito pior. Algumas centenas de quads por quadro é trabalho
 * confortável para o {@code SpriteBatch}, que os agrupa em poucas chamadas
 * de desenho por partilharem a mesma textura de um pixel.
 *
 * <p>A cor indica o estado, que é o que torna a simulação legível de
 * relance: dá para ver a fome se espalhar por uma região antes de a
 * população cair.
 */
public final class CreatureRenderer implements Disposable {

    /** Lado do quadrado de uma criatura, em unidades de mundo. */
    private static final float SIZE = WorldRenderer.TILE_SIZE * 0.75f;

    private static final Color COLOR_WANDERING = new Color(0.92f, 0.92f, 0.96f, 1f);
    private static final Color COLOR_SEEKING_FOOD = new Color(1.00f, 0.78f, 0.28f, 1f);
    private static final Color COLOR_EATING = new Color(0.42f, 0.92f, 0.45f, 1f);
    private static final Color COLOR_SEEKING_MATE = new Color(1.00f, 0.45f, 0.70f, 1f);

    private final Texture pixel;
    private final float worldPixelHeight;

    public CreatureRenderer(WorldRenderer worldRenderer) {
        this.worldPixelHeight = worldRenderer.worldPixelHeight();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.drawPixel(0, 0, 0xFFFFFFFF);
        this.pixel = new Texture(pixmap);
        this.pixel.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
    }

    public void render(SpriteBatch batch, Simulation simulation) {
        for (int i = 0, n = simulation.population(); i < n; i++) {
            Creature c = simulation.creatures().activeAt(i);

            batch.setColor(colorFor(c.state));
            // O eixo Y do mundo cresce para baixo (linha 0 é o topo) e o da
            // tela cresce para cima — a mesma inversão que o WorldRenderer
            // aplica ao desenhar o terreno.
            float screenX = c.x * WorldRenderer.TILE_SIZE - SIZE * 0.5f;
            float screenY = worldPixelHeight - c.y * WorldRenderer.TILE_SIZE - SIZE * 0.5f;

            batch.draw(pixel, screenX, screenY, SIZE, SIZE);
        }
        batch.setColor(Color.WHITE);
    }

    private static Color colorFor(CreatureState state) {
        switch (state) {
            case SEEKING_FOOD: return COLOR_SEEKING_FOOD;
            case EATING:       return COLOR_EATING;
            case SEEKING_MATE: return COLOR_SEEKING_MATE;
            default:           return COLOR_WANDERING;
        }
    }

    @Override
    public void dispose() {
        pixel.dispose();
    }
}
