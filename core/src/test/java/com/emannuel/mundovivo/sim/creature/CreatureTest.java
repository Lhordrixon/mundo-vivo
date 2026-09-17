package com.emannuel.mundovivo.sim.creature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatureTest {

    private static Creature living() {
        return new CreaturePool(4).spawn(0f, 0f, 0f, 0f);
    }

    @Test
    @DisplayName("dano tira vida e registra a causa")
    void damageReducesHealthAndRecordsCause() {
        Creature c = living();

        boolean lethal = c.applyDamage(0.25f, Creature.CAUSE_HAZARDOUS_TERRAIN);

        assertAll(
                () -> assertEquals(0.75f, c.health, 1e-6f),
                () -> assertEquals(Creature.CAUSE_HAZARDOUS_TERRAIN, c.lastDamageCause),
                () -> assertFalse(lethal, "0.25 de dano em vida cheia não deveria matar")
        );
    }

    @Test
    @DisplayName("dano acumula entre golpes")
    void damageAccumulates() {
        Creature c = living();

        c.applyDamage(0.3f, "combate");
        c.applyDamage(0.3f, "combate");

        assertEquals(0.4f, c.health, 1e-6f);
    }

    @Test
    @DisplayName("o golpe que zera a vida avisa que foi fatal, e a vida para em zero")
    void lethalBlowReportsItselfAndClampsAtZero() {
        Creature c = living();

        boolean lethal = c.applyDamage(5f, Creature.CAUSE_HAZARDOUS_TERRAIN);

        assertAll(
                () -> assertTrue(lethal, "dano maior que a vida deveria ser fatal"),
                () -> assertEquals(0f, c.health, 0f, "vida não deveria ficar negativa")
        );
    }

    @Test
    @DisplayName("bater em quem já está com a vida zerada não conta uma segunda morte")
    void damageOnAlreadyDeadIsNotLethalAgain() {
        Creature c = living();
        assertTrue(c.applyDamage(1f, Creature.CAUSE_STARVATION));

        boolean lethalAgain = c.applyDamage(1f, "combate");

        assertAll(
                () -> assertFalse(lethalAgain, "a mesma criatura não pode morrer duas vezes"),
                () -> assertEquals(0f, c.health, 0f),
                // A causa também não muda: quem matou foi a fome, e uma
                // pancada no cadáver não reescreve a estatística.
                () -> assertEquals(Creature.CAUSE_STARVATION, c.lastDamageCause)
        );
    }

    @Test
    @DisplayName("dano zero ou negativo é ignorado e não apaga a causa anterior")
    void nonPositiveDamageIsIgnored() {
        Creature c = living();
        c.applyDamage(0.2f, Creature.CAUSE_STARVATION);

        c.applyDamage(0f, "nada");
        c.applyDamage(-0.5f, "cura disfarçada de dano");

        assertAll(
                () -> assertEquals(0.8f, c.health, 1e-6f, "vida mudou com dano não positivo"),
                () -> assertEquals(Creature.CAUSE_STARVATION, c.lastDamageCause)
        );
    }

    @Test
    @DisplayName("renascer no mesmo slot limpa a causa do dano da vida anterior")
    void rebornCreatureForgetsPreviousCause() {
        CreaturePool pool = new CreaturePool(1);
        Creature c = pool.spawn(0f, 0f, 0f, 0f);
        c.applyDamage(1f, Creature.CAUSE_HAZARDOUS_TERRAIN);
        pool.despawn(c);

        Creature reborn = pool.spawn(0f, 0f, 0f, 0f);

        assertAll(
                () -> assertEquals(1f, reborn.health, 0f),
                () -> assertNull(reborn.lastDamageCause, "causa vazou de uma vida para outra")
        );
    }
}
