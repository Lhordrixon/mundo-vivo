package com.emannuel.mundovivo.sim.faction;

import java.util.Arrays;

/**
 * Emite ids de facção e conta quantos membros vivos cada uma tem.
 *
 * <p>Uma facção não carrega mais estado do que isso — nem cor, nem nome,
 * nem posição. Isso é proposital, pela mesma razão que o mundo é um array
 * de {@code byte} em vez de um array de objetos {@code Tile}: o id já é a
 * identidade da facção, e o índice dele neste registro é o próprio id, sem
 * tabela de busca no meio.
 *
 * <p>Facções não são recicladas como criaturas. Uma criatura nasce em uma
 * facção herdada de um dos pais; uma facção nova só aparece quando alguém
 * nasce sem pais — hoje, apenas a população inicial do mundo. Isso limita
 * a contagem de facções a um punhado de fundadoras, então não há pressão
 * de memória que justifique uma pilha de livres como a do
 * {@link com.emannuel.mundovivo.sim.creature.CreaturePool}: um array que
 * dobra de tamanho quando enche é suficiente.
 */
public final class FactionRegistry {

    private int[] memberCount = new int[16];
    private int count;

    /**
     * Funda uma facção nova, com um membro — a própria criatura fundadora.
     *
     * @return o id da facção recém-criada
     */
    public int create() {
        ensureCapacity(count + 1);
        memberCount[count] = 1;
        return count++;
    }

    /** Quantas facções já existiram, vivas ou extintas. */
    public int factionCount() {
        return count;
    }

    /** Membros vivos da facção agora. */
    public int memberCountOf(int factionId) {
        checkId(factionId);
        return memberCount[factionId];
    }

    /** Facção sem nenhum membro vivo. */
    public boolean isExtinct(int factionId) {
        return memberCountOf(factionId) <= 0;
    }

    /** Quantas facções ainda têm pelo menos um membro vivo. */
    public int livingFactionCount() {
        int living = 0;
        for (int i = 0; i < count; i++) {
            if (memberCount[i] > 0) {
                living++;
            }
        }
        return living;
    }

    /** Chamado a cada nascimento herdado: a facção do escolhido ganha um membro. */
    public void join(int factionId) {
        checkId(factionId);
        memberCount[factionId]++;
    }

    /**
     * Chamado a cada morte: a facção perde um membro.
     *
     * <p>Ignora silenciosamente uma facção já em zero. Não deveria acontecer
     * — cada morte corresponde a um nascimento ou fundação anterior — mas
     * não é motivo para a simulação inteira quebrar se acontecer.
     */
    public void leave(int factionId) {
        checkId(factionId);
        if (memberCount[factionId] > 0) {
            memberCount[factionId]--;
        }
    }

    private void ensureCapacity(int needed) {
        if (needed > memberCount.length) {
            memberCount = Arrays.copyOf(memberCount, Math.max(needed, memberCount.length * 2));
        }
    }

    private void checkId(int factionId) {
        if (factionId < 0 || factionId >= count) {
            throw new IndexOutOfBoundsException(
                    "facção " + factionId + " não existe (0.." + count + ")");
        }
    }
}
