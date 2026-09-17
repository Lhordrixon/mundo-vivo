package com.emannuel.mundovivo.sim.faction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactionRegistryTest {

    @Test
    @DisplayName("ids são sequenciais a partir de zero")
    void idsAreSequential() {
        FactionRegistry factions = new FactionRegistry();

        assertEquals(0, factions.create());
        assertEquals(1, factions.create());
        assertEquals(2, factions.create());
        assertEquals(3, factions.factionCount());
    }

    @Test
    @DisplayName("uma facção recém-criada nasce com um membro: o fundador")
    void newFactionStartsWithOneMember() {
        FactionRegistry factions = new FactionRegistry();
        int id = factions.create();

        assertAll(
                () -> assertEquals(1, factions.memberCountOf(id)),
                () -> assertFalse(factions.isExtinct(id))
        );
    }

    @Test
    @DisplayName("join e leave alteram a contagem de membros")
    void joinAndLeaveAdjustMemberCount() {
        FactionRegistry factions = new FactionRegistry();
        int id = factions.create();

        factions.join(id);
        factions.join(id);
        assertEquals(3, factions.memberCountOf(id));

        factions.leave(id);
        assertEquals(2, factions.memberCountOf(id));
    }

    @Test
    @DisplayName("leave até zero marca a facção como extinta, sem ir negativo")
    void leaveDownToZeroMarksExtinction() {
        FactionRegistry factions = new FactionRegistry();
        int id = factions.create();

        factions.leave(id);
        assertAll(
                () -> assertEquals(0, factions.memberCountOf(id)),
                () -> assertTrue(factions.isExtinct(id))
        );

        // Mais uma morte "sobrando" não deve derrubar a contagem abaixo de
        // zero — não deveria acontecer na simulação real, mas o registro
        // não pode corromper o próprio estado se acontecer.
        factions.leave(id);
        assertEquals(0, factions.memberCountOf(id));
    }

    @Test
    @DisplayName("livingFactionCount ignora as extintas")
    void livingFactionCountIgnoresExtinctOnes() {
        FactionRegistry factions = new FactionRegistry();
        int a = factions.create();
        int b = factions.create();
        factions.create();

        factions.leave(a);

        assertAll(
                () -> assertEquals(3, factions.factionCount()),
                () -> assertEquals(2, factions.livingFactionCount())
        );
        assertTrue(factions.memberCountOf(b) > 0);
    }

    @Test
    @DisplayName("cresce além da capacidade inicial sem perder contagens")
    void growsPastInitialCapacityWithoutLosingCounts() {
        FactionRegistry factions = new FactionRegistry();
        int[] ids = new int[200];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = factions.create();
            factions.join(ids[i]); // 2 membros cada
        }

        boolean correctSoFar = true;
        for (int id : ids) {
            correctSoFar &= factions.memberCountOf(id) == 2;
        }
        // Variável nova, atribuída uma vez só: a lambda de assertAll exige
        // que tudo que ela capture seja efetivamente final, e a de cima foi
        // reatribuída dentro do laço.
        final boolean allCorrect = correctSoFar;
        assertAll(
                () -> assertEquals(200, factions.factionCount()),
                () -> assertTrue(allCorrect, "alguma contagem se perdeu ao crescer o array")
        );
    }

    @Test
    @DisplayName("id inexistente lança exceção em vez de devolver lixo")
    void unknownIdThrows() {
        FactionRegistry factions = new FactionRegistry();
        factions.create();

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.memberCountOf(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.memberCountOf(1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.join(99)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.leave(99))
        );
    }
}
