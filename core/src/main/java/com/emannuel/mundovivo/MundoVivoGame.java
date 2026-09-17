package com.emannuel.mundovivo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
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
 *
 * <p>Toque simples fere a criatura tocada. É o primeiro poder do jogador, e
 * é aqui que as três peças se encontram: a câmera converte tela em mundo, o
 * {@link WorldRenderer} converte mundo em tile, e a simulação resolve o
 * resto. Sem seleção de poder, sem espera, sem custo — um poder só, para
 * provar o caminho inteiro antes de existir interface para escolher outro.
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

    /**
     * Reaproveitado a cada toque em vez de alocado: {@code unproject}
     * escreve no vetor que recebe, e o resto do jogo evita lixo por quadro
     * pelo mesmo motivo.
     */
    private final Vector3 touchPoint = new Vector3();

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        buildWorld(System.nanoTime());

        controller = new CameraController(
                camera, worldRenderer.worldPixelWidth(), worldRenderer.worldPixelHeight());
        controller.onLongPress(this::regenerate);
        controller.onTap(this::strikeAt);

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

    /**
     * Fere a criatura no tile tocado, se houver alguma.
     *
     * <p>O caminho completo do dedo até a simulação: a coordenada chega em
     * pixels de tela, a câmera a converte para coordenada de mundo levando
     * em conta arraste e zoom atuais, o renderizador a converte para tile
     * desfazendo a inversão vertical do desenho, e a simulação procura
     * quem está ali.
     *
     * <p>Toque no vazio, no mar ou fora do mundo não faz nada — e não é
     * erro. Errar o alvo é parte de mirar.
     *
     * @param screenX coordenada X do toque, em pixels de tela
     * @param screenY coordenada Y do toque, em pixels de tela
     */
    private void strikeAt(float screenX, float screenY) {
        touchPoint.set(screenX, screenY, 0f);
        camera.unproject(touchPoint);

        simulation.strikeAt(
                worldRenderer.tileX(touchPoint.x),
                worldRenderer.tileY(touchPoint.y));
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
