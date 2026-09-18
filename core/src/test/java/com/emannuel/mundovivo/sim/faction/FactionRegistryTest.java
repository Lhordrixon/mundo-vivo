package com.emannuel.mundovivo.sim.faction;

import com.emannuel.mundovivo.sim.util.Rng;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactionRegistryTest {

    @Test
    @DisplayName("ids são sequenciais a partir de zero")
    void idsAreSequential() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(1234L);

        assertEquals(0, factions.create(rng));
        assertEquals(1, factions.create(rng));
        assertEquals(2, factions.create(rng));
        assertEquals(3, factions.factionCount());
    }

    @Test
    @DisplayName("uma facção recém-criada nasce com um membro: o fundador")
    void newFactionStartsWithOneMember() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(1234L);
        int id = factions.create(rng);

        assertAll(
                () -> assertEquals(1, factions.memberCountOf(id)),
                () -> assertFalse(factions.isExtinct(id))
        );
    }

    @Test
    @DisplayName("join e leave alteram a contagem de membros")
    void joinAndLeaveAdjustMemberCount() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(1234L);
        int id = factions.create(rng);

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
        Rng rng = new Rng(1234L);
        int id = factions.create(rng);

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
        Rng rng = new Rng(1234L);
        int a = factions.create(rng);
        int b = factions.create(rng);
        factions.create(rng);

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
        Rng rng = new Rng(1234L);
        int[] ids = new int[200];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = factions.create(rng);
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
        Rng rng = new Rng(1234L);
        factions.create(rng);

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.memberCountOf(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.memberCountOf(1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.join(99)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.leave(99))
        );
    }

    // ------------------------------------------------------ nome e cor

    @Test
    @DisplayName("a mesma semente funda os mesmos nomes e as mesmas cores")
    void nameAndColorAreDeterministicPerSeed() {
        FactionRegistry first = new FactionRegistry();
        FactionRegistry second = new FactionRegistry();
        Rng rngA = new Rng(4242L);
        Rng rngB = new Rng(4242L);

        for (int i = 0; i < 240; i++) {
            first.create(rngA);
            second.create(rngB);
        }

        String problem = null;
        for (int id = 0; id < 240 && problem == null; id++) {
            if (!first.nameOf(id).equals(second.nameOf(id))) {
                problem = "nome divergiu na facção " + id + ": "
                        + first.nameOf(id) + " != " + second.nameOf(id);
            } else if (first.colorOf(id) != second.colorOf(id)) {
                problem = "cor divergiu na facção " + id;
            }
        }
        assertNull(problem, String.valueOf(problem));
    }

    @Test
    @DisplayName("sementes diferentes fundam nomes diferentes")
    void differentSeedsProduceDifferentNames() {
        FactionRegistry first = new FactionRegistry();
        FactionRegistry second = new FactionRegistry();
        Rng rngA = new Rng(1L);
        Rng rngB = new Rng(2L);

        int identical = 0;
        for (int i = 0; i < 60; i++) {
            int a = first.create(rngA);
            int b = second.create(rngB);
            if (first.nameOf(a).equals(second.nameOf(b))) {
                identical++;
            }
        }
        // Coincidência acontece: são 384 nomes possíveis. O que não pode é
        // a semente não influenciar nada e os 60 saírem iguais.
        assertTrue(identical < 20, identical + "/60 nomes iguais entre duas sementes");
    }

    @Test
    @DisplayName("o nome parece nome, não índice")
    void nameLooksLikeANameNotAnIndex() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(99L);

        String problem = null;
        for (int i = 0; i < 240 && problem == null; i++) {
            String name = factions.nameOf(factions.create(rng));
            if (name == null || name.length() < 4) {
                problem = "nome curto demais: " + name;
            } else if (name.matches(".*\\d.*")) {
                problem = "nome com dígito, que é índice disfarçado: " + name;
            } else if (!Character.isUpperCase(name.charAt(0))) {
                problem = "nome sem maiúscula inicial: " + name;
            }
        }
        assertNull(problem, String.valueOf(problem));
    }

    @Test
    @DisplayName("cores são opacas e bem espalhadas pelo círculo de matiz")
    void colorsAreOpaqueAndSpreadOut() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(7L);
        for (int i = 0; i < 240; i++) {
            factions.create(rng);
        }

        // Alfa cheio em todas: cor de reino translúcida sumiria no fundo.
        String opacity = null;
        for (int id = 0; id < 240 && opacity == null; id++) {
            if ((factions.colorOf(id) & 0xFF) != 0xFF) {
                opacity = "facção " + id + " sem alfa cheio";
            }
        }
        assertNull(opacity, String.valueOf(opacity));

        // Vizinhas em id têm que ser obviamente diferentes — é o que o giro
        // pela razão áurea garante, e é a regressão que pegaria alguém
        // trocando o espalhamento por um sorteio.
        int tooClose = 0;
        for (int id = 1; id < 240; id++) {
            if (channelDistance(factions.colorOf(id - 1), factions.colorOf(id)) < 60) {
                tooClose++;
            }
        }
        assertEquals(0, tooClose, tooClose + " pares de facções consecutivas com cor quase igual");
    }

    @Test
    @DisplayName("duas facções não compartilham a mesma cor exata")
    void coloursDoNotRepeatExactly() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(31337L);
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 240; i++) {
            seen.add(factions.colorOf(factions.create(rng)));
        }
        assertEquals(240, seen.size(), "houve cor repetida entre as 240 facções");
    }

    @Test
    @DisplayName("nameOf e colorOf recusam id inexistente, como memberCountOf")
    void nameAndColorRejectUnknownId() {
        FactionRegistry factions = new FactionRegistry();
        Rng rng = new Rng(1234L);
        factions.create(rng);

        assertAll(
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.nameOf(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.nameOf(1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.colorOf(-1)),
                () -> assertThrows(IndexOutOfBoundsException.class, () -> factions.colorOf(1)),
                // E a facção que existe responde às duas.
                () -> assertNotNull(factions.nameOf(0)),
                () -> assertNotEquals(0, factions.colorOf(0))
        );
    }

    /** Soma das diferenças de canal entre duas cores RGBA8888. */
    private static int channelDistance(int a, int b) {
        int dr = Math.abs(((a >>> 24) & 0xFF) - ((b >>> 24) & 0xFF));
        int dg = Math.abs(((a >>> 16) & 0xFF) - ((b >>> 16) & 0xFF));
        int db = Math.abs(((a >>> 8) & 0xFF) - ((b >>> 8) & 0xFF));
        return dr + dg + db;
    }
}
