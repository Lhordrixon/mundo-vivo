package com.emannuel.mundovivo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.ScreenUtils;
import com.emannuel.mundovivo.render.CameraController;
import com.emannuel.mundovivo.render.WorldRenderer;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

/**
 * Ponto de entrada do jogo, comum a Android e desktop.
 *
 * <p>Estado atual: sistema 1 (grade e geração de mundo) e sistema 2
 * (câmera navegável). Não há unidades, facções nem poderes ainda — este é
 * o alicerce sobre o qual eles entram.
 *
 * <p>Toque longo gera um mundo novo. É um atalho de desenvolvimento para
 * avaliar muitas seeds rápido; sai quando a interface de verdade entrar.
 */
public final class MundoVivoGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private CameraController controller;
    private WorldRenderer renderer;

    /** Semente do mundo atual — precisa ir junto no save. */
    private long seed;

    /** O enquadramento inicial só acontece uma vez; girar a tela não deve resetar o zoom. */
    private boolean framed;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        seed = System.nanoTime();
        World world = WorldGenerator.generate(WorldConfig.medium(seed));
        renderer = new WorldRenderer(world);

        controller = new CameraController(camera, renderer.worldPixelWidth(), renderer.worldPixelHeight());
        controller.onLongPress(this::regenerate);

        Gdx.input.setInputProcessor(new InputMultiplexer(new GestureDetector(controller), controller));

        Gdx.app.log("MundoVivo", "mundo " + world.width() + "x" + world.height()
                + " seed=" + seed
                + " terra=" + Math.round(world.landFraction() * 100f) + "%");
    }

    /** Descarta o mundo atual e gera outro com uma semente nova. */
    public void regenerate() {
        seed = System.nanoTime();
        World world = WorldGenerator.generate(WorldConfig.medium(seed));

        // A textura antiga precisa ser liberada explicitamente: ela vive na
        // GPU e o coletor de lixo do Java não a alcança.
        renderer.dispose();
        renderer = new WorldRenderer(world);

        // A câmera é reaproveitada porque o tamanho do mundo não muda entre
        // gerações. Se um dia o jogador puder escolher o tamanho do mapa,
        // este é o ponto que precisa recriar o CameraController.

        Gdx.app.log("MundoVivo", "novo mundo seed=" + seed
                + " terra=" + Math.round(world.landFraction() * 100f) + "%");
    }

    @Override
    public void resize(int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        controller.resize(width, height);
        if (!framed) {
            controller.fitWorld();
            framed = true;
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.04f, 0.06f, 0.09f, 1f);

        controller.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderer.render(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
