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

    /**
     * Facção a que pertence: herdada de um dos pais, ou nova ao nascer sem
     * eles — hoje, só a população inicial do mundo. -1 é o valor de um
     * slot que ainda não recebeu vida nenhuma; nenhuma criatura ativa deve
     * carregar esse valor. Quem atribui é {@code Simulation}, não o pool:
     * o pool não sabe o que é uma facção, só devolve um slot em branco.
     */
    public int factionId = -1;

    /** Causa do último dano sofrido, ou {@code null} se ainda não levou nenhum. */
    public String lastDamageCause;

    /** Fome: o dano que já existia antes de haver dano externo. */
    public static final String CAUSE_STARVATION = "fome";

    /** Terreno que fere quem está em cima — hoje, oceano profundo. */
    public static final String CAUSE_HAZARDOUS_TERRAIN = "terreno perigoso";

    /**
     * O jogador tocou nesta criatura. Primeira causa de dano que vem de
     * fora da simulação — as outras duas são o mundo agindo sozinho.
     */
    public static final String CAUSE_PLAYER_STRIKE = "golpe do jogador";

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
        this.factionId = -1;
        this.lastDamageCause = null;
    }

    /**
     * Tira vida da criatura, por qualquer motivo.
     *
     * <p>É a única porta de entrada de dano do jogo: fome entra por aqui,
     * terreno perigoso entra por aqui, e combate e desastre vão entrar por
     * aqui sem que nada precise ser reescrito. A criatura não sabe o que a
     * feriu — só registra a causa, para quem contabiliza a morte poder
     * separar quem morreu de quê.
     *
     * <p><b>Ela não mata ninguém.</b> Morrer envolve virar comida no tile,
     * sair da facção e devolver o slot ao pool, e nada disso mora aqui: o
     * pacote {@code creature} não conhece mundo, comida nem facção, e essa
     * separação é o que deixa a criatura testável sozinha. O que este
     * método faz é avisar, pelo retorno, que o golpe foi fatal; quem trata
     * a morte é {@code Simulation}, no mesmo ponto onde já tratava a morte
     * por fome. Um só lugar mata — não há caminho de morte duplicado.
     *
     * <p>A causa é uma {@code String} livre em vez de um enum fechado
     * porque combate vai querer dizer de quem levou a pancada, não só que
     * levou. Para as causas que a própria simulação usa existem constantes
     * aqui do lado, que é o que evita erro de digitação virar estatística
     * errada.
     *
     * @param amount quanto de vida tirar, em [0,1]; valores &le; 0 são
     *               ignorados, para um dano de zero não apagar a causa da
     *               pancada anterior
     * @param cause  o que causou — use as constantes {@code CAUSE_*} para
     *               as causas da própria simulação
     * @return {@code true} se <em>este</em> golpe zerou a vida; {@code false}
     *         se a criatura sobreviveu, ou se já estava com a vida zerada
     *         antes dele — assim dois golpes na mesma criatura não contam
     *         duas mortes
     */
    public boolean applyDamage(float amount, String cause) {
        if (amount <= 0f || health <= 0f) {
            return false;
        }
        this.lastDamageCause = cause;
        this.health = Math.max(0f, health - amount);
        return health <= 0f;
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
                + " idade=" + String.format("%.0f", age) + "s"
                + " facção=" + factionId + "}";
    }
}
