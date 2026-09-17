package com.emannuel.mundovivo.sim.creature;

/**
 * Pool de criaturas pré-alocadas.
 *
 * <p>Todas as instâncias nascem no construtor e nunca mais são criadas.
 * Nascimento é tirar um índice da pilha de livres; morte é devolvê-lo.
 * Depois da construção, a simulação não aloca nada — que é a condição para
 * rodar milhares de criaturas a 60 quadros sem o coletor de lixo
 * interromper o jogo no meio de uma guerra.
 *
 * <p>A lista de ativos é mantida compacta: remover alguém move o último
 * para o buraco, em O(1). Como cada criatura sabe o próprio índice na
 * lista, não é preciso procurá-la para removê-la.
 *
 * <p><b>Ordem de iteração:</b> percorra de trás para frente. A remoção por
 * troca traz o último elemento para a posição atual, e nascimentos entram
 * no fim — de trás para frente, nenhum dos dois casos faz um elemento ser
 * pulado ou processado duas vezes no mesmo passo.
 */
public final class CreaturePool {

    private final Creature[] slots;
    private final Creature[] active;
    private final int[] freeStack;

    private int freeCount;
    private int activeCount;
    private long nextId = 1L;

    /** Contador de reaproveitamentos — existe para o teste provar que o pool funciona. */
    private long totalSpawns;

    public CreaturePool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacidade deve ser > 0, recebido: " + capacity);
        }
        this.slots = new Creature[capacity];
        this.active = new Creature[capacity];
        this.freeStack = new int[capacity];

        for (int i = 0; i < capacity; i++) {
            slots[i] = new Creature(i);
            // Empilhado ao contrário para que os primeiros nascimentos usem
            // os slots 0, 1, 2... e os testes fiquem legíveis.
            freeStack[i] = capacity - 1 - i;
        }
        this.freeCount = capacity;
    }

    public int capacity() {
        return slots.length;
    }

    public int activeCount() {
        return activeCount;
    }

    public int freeCount() {
        return freeCount;
    }

    public boolean isFull() {
        return freeCount == 0;
    }

    public long totalSpawns() {
        return totalSpawns;
    }

    /**
     * Criatura ativa na posição dada da lista compacta.
     * A posição de uma criatura muda quando outra morre — não guarde este
     * índice entre passos; guarde o slot.
     */
    public Creature activeAt(int index) {
        if (index < 0 || index >= activeCount) {
            throw new IndexOutOfBoundsException(
                    "índice ativo " + index + " fora de [0," + activeCount + ")");
        }
        return active[index];
    }

    /** Criatura pelo slot, esteja viva ou não. Use {@link #isAlive(int)} antes. */
    public Creature bySlot(int slot) {
        return slots[slot];
    }

    public boolean isAlive(int slot) {
        return slot >= 0 && slot < slots.length && slots[slot].activeIndex >= 0;
    }

    /**
     * Traz uma criatura à vida.
     *
     * @return a criatura, ou {@code null} se o pool estiver cheio — o
     *         chamador decide se isso é um limite natural de população ou
     *         um erro
     */
    public Creature spawn(float x, float y, float startingHunger, float cooldown) {
        if (freeCount == 0) {
            return null;
        }
        int slot = freeStack[--freeCount];
        Creature creature = slots[slot];
        creature.reset(nextId++, x, y, startingHunger, cooldown);

        creature.activeIndex = activeCount;
        active[activeCount++] = creature;
        totalSpawns++;

        return creature;
    }

    /** Mata a criatura e devolve o slot ao pool. Ignora quem já está morto. */
    public void despawn(Creature creature) {
        int index = creature.activeIndex;
        if (index < 0) {
            return;
        }

        int last = activeCount - 1;
        Creature moved = active[last];
        active[index] = moved;
        moved.activeIndex = index;

        active[last] = null;
        activeCount = last;

        creature.activeIndex = -1;
        freeStack[freeCount++] = creature.slot;
    }

    /** Mata todo mundo, sem desalocar nada. */
    public void clear() {
        for (int i = 0; i < activeCount; i++) {
            active[i].activeIndex = -1;
            active[i] = null;
        }
        activeCount = 0;
        freeCount = slots.length;
        for (int i = 0; i < slots.length; i++) {
            freeStack[i] = slots.length - 1 - i;
        }
    }
}
