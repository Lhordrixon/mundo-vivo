package com.emannuel.mundovivo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.utils.ScreenUtils;
import com.emannuel.mundovivo.render.CameraController;
import com.emannuel.mundovivo.render.CreatureRenderer;
import com.emannuel.mundovivo.render.WorldRenderer;
import com.emannuel.mundovivo.sim.Simulation;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

/**
 * Ponto de entrada do jogo, comum a Android e desktop.
 *
 * <p>Estado atual: sistemas 1 a 3 — grade e geração de mundo, câmera
 * navegável, e criaturas autônomas que procuram comida, comem, procuram
 * parceiro, se reproduzem e morrem. Ainda não há facções, poderes de deus
 * nem save.
 *
 * <p>Toque longo gera um mundo novo. É um atalho de desenvolvimento para
 * avaliar muitas sementes rápido; sai quando a interface de verdade entrar.
 */
public final class MundoVivoGame extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private CameraController controller;
    private WorldRenderer worldRenderer;
    private CreatureRenderer creatureRenderer;
    private Simulation simulation;

    /** O enquadramento inicial só acontece uma vez; girar a tela não deve resetar o zoom. */
    private boolean framed;

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        buildWorld(System.nanoTime());

        controller = new CameraController(
                camera, worldRenderer.worldPixelWidth(), worldRenderer.worldPixelHeight());
        controller.onLongPress(this::regenerate);

        Gdx.input.setInputProcessor(new InputMultiplexer(new GestureDetector(controller), controller));
    }

    private void buildWorld(long seed) {
        World world = WorldGenerator.generate(WorldConfig.medium(seed));

        worldRenderer = new WorldRenderer(world);
        creatureRenderer = new CreatureRenderer(worldRenderer);
        simulation = new Simulation(world, new CreatureConfig());

        Gdx.app.log("MundoVivo", "mundo " + world.width() + "x" + world.height()
                + " seed=" + seed
                + " terra=" + Math.round(world.landFraction() * 100f) + "%"
                + " populacao=" + simulation.population());
    }

    /** Descarta o mundo atual e gera outro com uma semente nova. */
    public void regenerate() {
        // As texturas antigas precisam ser liberadas explicitamente: elas
        // vivem na GPU e o coletor de lixo do Java não as alcança.
        worldRenderer.dispose();
        creatureRenderer.dispose();

        buildWorld(System.nanoTime());

        // A câmera é reaproveitada porque o tamanho do mundo não muda entre
        // gerações. Se um dia o jogador puder escolher o tamanho do mapa,
        // este é o ponto que precisa recriar o CameraController.
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
        simulation.step(Gdx.graphics.getDeltaTime());

        ScreenUtils.clear(0.04f, 0.06f, 0.09f, 1f);

        controller.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldRenderer.render(batch);
        creatureRenderer.render(batch, simulation);
        batch.end();
    }

    @Override
    public void dispose() {
        if (worldRenderer != null) {
            worldRenderer.dispose();
        }
        if (creatureRenderer != null) {
            creatureRenderer.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
