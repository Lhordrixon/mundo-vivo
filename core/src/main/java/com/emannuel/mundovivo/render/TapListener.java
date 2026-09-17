package com.emannuel.mundovivo.render;

/**
 * Recebe um toque simples, em coordenadas de tela.
 *
 * <p>Existe porque o toque longo cabia em um {@link Runnable} — ele não
 * carrega informação nenhuma além de "aconteceu" — e o toque simples não
 * cabe: ele precisa dizer <em>onde</em>. Deliberadamente em coordenada de
 * tela, crua, do jeito que o detector de gestos entrega: converter para
 * mundo exige a câmera, e a câmera não é assunto do
 * {@link CameraController} decidir por quem escuta.
 */
@FunctionalInterface
public interface TapListener {

    /**
     * @param screenX coordenada X do toque, em pixels de tela
     * @param screenY coordenada Y do toque, em pixels de tela, crescendo
     *                para baixo — é a convenção do libGDX para entrada
     */
    void onTap(float screenX, float screenY);
}
