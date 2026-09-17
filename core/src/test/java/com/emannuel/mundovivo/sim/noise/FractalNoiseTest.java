package com.emannuel.mundovivo.sim.noise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FractalNoiseTest {

    @Test
    @DisplayName("fbm01 nunca sai de [0,1]")
    void staysInRange() {
        FractalNoise noise = new FractalNoise(1234L);

        for (int i = 0; i < 20_000; i++) {
            float x = (i % 200) * 0.137f;
            float y = (i / 200f) * 0.211f;
            float value = noise.fbm01(x, y);

            assertTrue(value >= 0f && value <= 1f,
                    "fbm01(" + x + "," + y + ") = " + value);
        }
    }

    @Test
    @DisplayName("mesma seed e mesmas coordenadas devolvem o mesmo valor")
    void isDeterministic() {
        FractalNoise a = new FractalNoise(7L);
        FractalNoise b = new FractalNoise(7L);

        for (int i = 0; i < 1_000; i++) {
            float x = i * 0.0731f;
            float y = i * -0.0417f;
            assertEquals(a.fbm01(x, y), b.fbm01(x, y), 0f);
        }
    }

    @Test
    @DisplayName("seeds diferentes descorrelacionam o campo")
    void differentSeedsDiffer() {
        FractalNoise a = new FractalNoise(1L);
        FractalNoise b = new FractalNoise(2L);

        int identical = 0;
        // O deslocamento fracionário é obrigatório: amostrar sobre os pontos
        // inteiros da rede compararia 0.5 com 0.5 e o teste passaria sem
        // testar nada. Ver a nota sobre a rede na documentação de FractalNoise.
        for (int i = 0; i < 1_000; i++) {
            float x = i * 0.3137f + 0.5f;
            float y = i * 0.1709f + 0.25f;
            if (a.fbm01(x, y) == b.fbm01(x, y)) {
                identical++;
            }
        }
        assertEquals(0, identical, "campos coincidiram em " + identical + " de 1000 amostras");
    }

    @Test
    @DisplayName("o ruído vale 0.5 nos pontos inteiros da rede, para qualquer seed")
    void isZeroOnLatticePoints() {
        FractalNoise a = new FractalNoise(1L);
        FractalNoise b = new FractalNoise(987_654L);

        // Propriedade estrutural do ruído de gradiente, documentada de
        // propósito: se ela mudar, a documentação de FractalNoise mente.
        for (int i = 0; i < 20; i++) {
            assertEquals(0.5f, a.fbm01(i, i * 2), 0f);
            assertEquals(0.5f, b.fbm01(i, i * 2), 0f);
        }
    }

    @Test
    @DisplayName("o campo é contínuo: amostras vizinhas variam pouco")
    void isContinuous() {
        FractalNoise noise = new FractalNoise(99L);
        float step = 0.001f;

        for (int i = 0; i < 2_000; i++) {
            float x = i * 0.05f;
            float delta = Math.abs(noise.fbm01(x, 0.5f) - noise.fbm01(x + step, 0.5f));

            assertTrue(delta < 0.05f,
                    "salto de " + delta + " em x=" + x + " — o ruído não está contínuo");
        }
    }

    @Test
    @DisplayName("a média do campo fica perto de 0.5 e usa boa parte da faixa")
    void isWellDistributed() {
        FractalNoise noise = new FractalNoise(2026L);

        float sum = 0f;
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        int samples = 40_000;

        for (int i = 0; i < samples; i++) {
            float value = noise.fbm01((i % 200) * 0.23f, (i / 200f) * 0.23f);
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        float mean = sum / samples;
        assertTrue(mean > 0.42f && mean < 0.58f, "média deslocada: " + mean);
        assertTrue(max - min > 0.45f, "faixa usada estreita demais: " + (max - min));
    }

    @Test
    @DisplayName("parâmetros inválidos são recusados no construtor")
    void rejectsInvalidParameters() {
        assertThrows(IllegalArgumentException.class, () -> new FractalNoise(1L, 0, 2f, 0.5f));
        assertThrows(IllegalArgumentException.class, () -> new FractalNoise(1L, 4, 0f, 0.5f));
        assertThrows(IllegalArgumentException.class, () -> new FractalNoise(1L, 4, 2f, 0f));
        assertThrows(IllegalArgumentException.class, () -> new FractalNoise(1L, 4, 2f, 1f));
    }

    @Test
    @DisplayName("mais oitavas mudam o resultado")
    void octavesMatter() {
        FractalNoise coarse = new FractalNoise(5L, 1, 2f, 0.5f);
        FractalNoise detailed = new FractalNoise(5L, 6, 2f, 0.5f);

        assertNotEquals(coarse.fbm01(3.7f, 2.1f), detailed.fbm01(3.7f, 2.1f));
    }
}
