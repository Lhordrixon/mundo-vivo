package com.emannuel.mundovivo.render;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Câmera navegável por toque: arrastar move, pinçar aproxima.
 *
 * <p>Implementa dois contratos de entrada de uma vez. {@code GestureListener}
 * cobre o celular (arraste, pinça, toque longo); {@code InputAdapter} cobre
 * a roda do mouse no desktop, que o detector de gestos não enxerga. O jogo
 * registra a mesma instância nos dois lugares através de um
 * {@code InputMultiplexer}.
 *
 * <p>O zoom máximo não é uma constante: ele é recalculado a cada
 * {@link #resize(int, int)} para caber exatamente o mundo inteiro na tela,
 * o que evita o mapa "flutuando" cercado de vazio em telas largas.
 */
public final class CameraController extends InputAdapter implements GestureDetector.GestureListener {

    /** Zoom mínimo: quanto menor, mais perto. 0.2 deixa um tile com ~40 px. */
    private static final float MIN_ZOOM = 0.2f;

    /** Folga além do enquadramento exato do mundo, para não colar nas bordas. */
    private static final float MAX_ZOOM_MARGIN = 1.05f;

    private static final float SCROLL_STEP = 0.12f;

    private final OrthographicCamera camera;
    private final float worldWidth;
    private final float worldHeight;

    /** Chamado no toque longo. Opcional — pode ser {@code null}. */
    private Runnable onLongPress;

    /** Chamado no toque simples, com a coordenada tocada. Opcional. */
    private TapListener onTap;

    private float maxZoom = 1f;
    private float zoomAtGestureStart = 1f;

    public CameraController(OrthographicCamera camera, float worldWidth, float worldHeight) {
        this.camera = camera;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.camera.position.set(worldWidth / 2f, worldHeight / 2f, 0f);
    }

    public void onLongPress(Runnable action) {
        this.onLongPress = action;
    }

    /**
     * Registra quem recebe o toque simples.
     *
     * <p>A coordenada sai daqui como veio do detector de gestos, em
     * pixels de tela. Esta classe move câmera e não sabe o que existe no
     * mundo — traduzir toque em alvo é trabalho de quem tem a câmera e a
     * simulação na mão, não dela.
     */
    public void onTap(TapListener action) {
        this.onTap = action;
    }

    public OrthographicCamera camera() {
        return camera;
    }

    /** Reajusta o enquadramento. Chamar de {@code ApplicationListener.resize}. */
    public void resize(int screenWidth, int screenHeight) {
        camera.viewportWidth = screenWidth;
        camera.viewportHeight = screenHeight;

        maxZoom = Math.max(worldWidth / screenWidth, worldHeight / screenHeight) * MAX_ZOOM_MARGIN;
        if (maxZoom < MIN_ZOOM) {
            maxZoom = MIN_ZOOM;
        }
        camera.zoom = MathUtils.clamp(camera.zoom, MIN_ZOOM, maxZoom);

        clampPosition();
        camera.update();
    }

    /** Aplica os limites e atualiza a matriz. Chamar uma vez por frame. */
    public void update() {
        clampPosition();
        camera.update();
    }

    /** Enquadra o mundo inteiro. */
    public void fitWorld() {
        camera.zoom = maxZoom;
        camera.position.set(worldWidth / 2f, worldHeight / 2f, 0f);
        clampPosition();
        camera.update();
    }

    /**
     * Impede que a câmera saia do mundo. Quando o mundo é menor que a
     * viewport em um eixo, ele é centralizado nesse eixo em vez de grudar
     * em uma das bordas.
     */
    private void clampPosition() {
        float halfWidth = camera.viewportWidth * camera.zoom * 0.5f;
        float halfHeight = camera.viewportHeight * camera.zoom * 0.5f;

        if (halfWidth * 2f >= worldWidth) {
            camera.position.x = worldWidth / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, halfWidth, worldWidth - halfWidth);
        }

        if (halfHeight * 2f >= worldHeight) {
            camera.position.y = worldHeight / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, halfHeight, worldHeight - halfHeight);
        }
    }

    // ---------------------------------------------------------- GestureListener

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        zoomAtGestureStart = camera.zoom;
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // O dedo empurra o mundo: arrastar para a direita revela o que está
        // à esquerda, então a câmera anda no sentido contrário ao dedo.
        camera.position.x -= deltaX * camera.zoom;
        camera.position.y += deltaY * camera.zoom;
        clampPosition();
        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (distance <= 0f) {
            return false;
        }
        camera.zoom = MathUtils.clamp(
                zoomAtGestureStart * (initialDistance / distance), MIN_ZOOM, maxZoom);
        clampPosition();
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        if (onLongPress != null) {
            onLongPress.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (onTap != null) {
            onTap.onTap(x, y);
            return true;
        }
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
                         Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
        // sem estado a limpar
    }

    // ------------------------------------------------------------ InputAdapter

    @Override
    public boolean scrolled(float amountX, float amountY) {
        camera.zoom = MathUtils.clamp(
                camera.zoom * (1f + amountY * SCROLL_STEP), MIN_ZOOM, maxZoom);
        clampPosition();
        return true;
    }
}
