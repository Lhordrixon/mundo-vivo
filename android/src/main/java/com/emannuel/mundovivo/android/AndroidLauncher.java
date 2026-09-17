package com.emannuel.mundovivo.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.emannuel.mundovivo.MundoVivoGame;

/** Activity única do jogo no Android. */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        // Sensores desligados: o jogo não os usa e mantê-los ativos gasta bateria.
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;

        initialize(new MundoVivoGame(), config);
    }
}
