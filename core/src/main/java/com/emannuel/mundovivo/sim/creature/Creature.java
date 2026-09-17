package com.emannuel.mundovivo.sim.creature;

/**
 * Uma criatura.
 *
 * <p>Objeto mutável e reaproveitado: instâncias nunca são criadas durante a
 * simulação, só emprestadas e devolvidas ao {@link CreaturePool}. É por
 * isso que os campos são públicos e não há construtor com argumentos —
 * nascer é {@link #reset}, morrer é voltar para a pilha de livres.
 *
 * <p>Se um dia a contagem de criaturas passar da casa dos milhares e o
 * perfil acusar falha de cache, o caminho é trocar este objeto por arrays
 * paralelos. Enquanto não acusar, objeto legível vence array esperto.
 */
public final class Creature {

    /** Posição no pool. Nunca muda; é a identidade da instância. */
    final int slot;

    /** Onde está na lista de ativos. Mantido pelo pool para remoção em O(1). */
    int activeIndex = -1;

    /** Identificador único e crescente, atribuído a cada nascimento. */
    public long id;

    /** Posição contínua em tiles — não em pixels. */
    public float x;
    public float y;

    /** Segundos de vida. */
    public float age;

    /** Fome em [0,1]: 0 é saciada, 1 é morrendo de inanição. */
    public float hunger;

    /** Vida em [0,1]. */
    public float health;

    public CreatureState state = CreatureState.WANDERING;

    /** Tile de destino, ou -1 quando não há. */
    public int targetTile = -1;

    /** Outra criatura sendo perseguida para reprodução, ou -1. */
    public int targetMateSlot = -1;

    /** Segundos restantes até poder reproduzir de novo. */
    public float reproductionCooldown;

    Creature(int slot) {
        this.slot = slot;
    }

    public int slot() {
        return slot;
    }

    /** Prepara a instância para uma nova vida. */
    void reset(long id, float x, float y, float startingHunger, float cooldown) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.age = 0f;
        this.hunger = startingHunger;
        this.health = 1f;
        this.state = CreatureState.WANDERING;
        this.targetTile = -1;
        this.targetMateSlot = -1;
        this.reproductionCooldown = cooldown;
    }

    public int tileX() {
        return (int) x;
    }

    public int tileY() {
        return (int) y;
    }

    public boolean isAdult(CreatureConfig config) {
        return age >= config.adultAgeSeconds;
    }

    /** Adulta, saciada, saudável e fora do período de espera. */
    public boolean canReproduce(CreatureConfig config) {
        return isAdult(config)
                && reproductionCooldown <= 0f
                && hunger <= config.reproductionHungerMax
                && health > 0.5f;
    }

    public float distanceTo(Creature other) {
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "Creature#" + id + "{" + state
                + " em (" + String.format("%.1f", x) + "," + String.format("%.1f", y) + ")"
                + " fome=" + String.format("%.2f", hunger)
                + " vida=" + String.format("%.2f", health)
                + " idade=" + String.format("%.0f", age) + "s}";
    }
}
