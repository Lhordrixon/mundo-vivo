package com.emannuel.mundovivo.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.emannuel.mundovivo.MundoVivoGame;

/**
 * Executa o jogo no computador.
 *
 * <p>Existe para encurtar o ciclo de teste: {@code ./gradlew desktop:run}
 * abre o jogo em segundos, enquanto instalar um APK no celular leva
 * minutos. É o mesmo código do Android — só o launcher muda.
 */
public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mundo Vivo");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new MundoVivoGame(), config);
    }
}
