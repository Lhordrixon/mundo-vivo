package com.emannuel.mundovivo.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Entrada da câmera, sem abrir janela.
 *
 * <p><b>O que este teste evita de propósito:</b> {@code resize()} e
 * qualquer caminho que chame {@code camera.update()}. A multiplicação e a
 * inversão de matriz do libGDX são métodos nativos, e a biblioteca nativa
 * não é carregada em um teste sem backend — chamá-los aqui derrubaria a
 * suíte por motivo de ambiente, não de lógica. Arraste, pinça e toque não
 * passam por matriz nenhuma: mexem em posição e zoom, que são campos.
 */
class CameraControllerTest {

    private static final float WORLD_WIDTH = 2048f;
    private static final float WORLD_HEIGHT = 1536f;

    private static CameraController controller(OrthographicCamera camera) {
        return new CameraController(camera, WORLD_WIDTH, WORLD_HEIGHT);
    }

    @Test
    @DisplayName("sem ouvinte registrado, o toque continua não fazendo nada")
    void tapWithoutListenerIsIgnored() {
        CameraController controller = controller(new OrthographicCamera());

        assertFalse(controller.tap(10f, 20f, 1, 0),
                "toque sem ouvinte deveria devolver false, como antes");
    }

    @Test
    @DisplayName("o toque entrega as coordenadas de tela ao ouvinte e se dá por tratado")
    void tapDeliversScreenCoordinates() {
        CameraController controller = controller(new OrthographicCamera());
        float[] received = {Float.NaN, Float.NaN};
        int[] calls = {0};

        controller.onTap((x, y) -> {
            received[0] = x;
            received[1] = y;
            calls[0]++;
        });

        boolean handled = controller.tap(123.5f, 456.25f, 1, 0);

        assertAll(
                () -> assertTrue(handled, "toque tratado deveria devolver true"),
                () -> assertEquals(1, calls[0], "ouvinte chamado o número errado de vezes"),
                () -> assertEquals(123.5f, received[0], 0f),
                () -> assertEquals(456.25f, received[1], 0f)
        );
    }

    @Test
    @DisplayName("o toque longo continua funcionando junto do toque simples")
    void longPressStillWorksAlongsideTap() {
        CameraController controller = controller(new OrthographicCamera());
        int[] longPresses = {0};
        int[] taps = {0};

        controller.onLongPress(() -> longPresses[0]++);
        controller.onTap((x, y) -> taps[0]++);

        assertAll(
                () -> assertTrue(controller.longPress(1f, 1f)),
                () -> assertEquals(1, longPresses[0]),
                () -> assertEquals(0, taps[0], "toque longo não pode disparar o toque simples")
        );
    }

    @Test
    @DisplayName("arrastar continua movendo a câmera no sentido contrário ao dedo")
    void panStillMovesTheCamera() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        CameraController controller = controller(camera);

        float startX = camera.position.x;
        float startY = camera.position.y;

        boolean handled = controller.pan(0f, 0f, 100f, 50f);

        assertAll(
                () -> assertTrue(handled),
                // O dedo empurra o mundo: arrastar para a direita revela o
                // que está à esquerda.
                () -> assertEquals(startX - 100f, camera.position.x, 1e-4f),
                () -> assertEquals(startY + 50f, camera.position.y, 1e-4f)
        );
    }

    @Test
    @DisplayName("pinçar continua aproximando, a partir do zoom em que o gesto começou")
    void pinchStillZoomsFromTheGestureBaseline() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.viewportWidth = 800f;
        camera.viewportHeight = 600f;
        CameraController controller = controller(camera);

        // O gesto guarda o zoom de partida no touchDown; sem isso a pinça
        // se acumularia a cada quadro em vez de ser proporcional.
        controller.touchDown(0f, 0f, 0, 0);
        controller.zoom(100f, 200f);

        assertEquals(0.5f, camera.zoom, 1e-4f, "afastar os dedos deveria aproximar a câmera");
    }

    @Test
    @DisplayName("o zoom respeita o limite mínimo")
    void zoomIsClampedAtTheMinimum() {
        OrthographicCamera camera = new OrthographicCamera();
        CameraController controller = controller(camera);

        controller.touchDown(0f, 0f, 0, 0);
        controller.zoom(1f, 1000f);

        assertTrue(camera.zoom >= 0.2f, "zoom passou do mínimo: " + camera.zoom);
    }
}
