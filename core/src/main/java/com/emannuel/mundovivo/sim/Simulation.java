package com.emannuel.mundovivo.sim;

import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.creature.CreaturePool;
import com.emannuel.mundovivo.sim.creature.CreatureState;
import com.emannuel.mundovivo.sim.ecology.FoodMap;
import com.emannuel.mundovivo.sim.util.Rng;
import com.emannuel.mundovivo.sim.world.World;

/**
 * O mundo vivo: terreno, comida e criaturas avançando no tempo.
 *
 * <p>Esta classe é o coração do jogo e não conhece libGDX. Ela pode ser
 * rodada por mil passos dentro de um teste, sem tela, sem emulador e sem
 * placa de vídeo — que é como o comportamento das criaturas foi calibrado.
 *
 * <p><b>Determinismo.</b> Todo sorteio passa por um único {@link Rng}
 * semeado a partir da semente do mundo, e a ordem de percurso das criaturas
 * é fixa. Mesma semente e mesma sequência de {@code dt} produzem
 * exatamente a mesma história. Sem isso, um bug de população só reproduz
 * por sorte.
 *
 * <p><b>Tempo.</b> Tudo é medido em segundos, nunca em quadros. A
 * simulação precisa se comportar igual a 30 e a 60 quadros por segundo.
 */
public final class Simulation {

    /**
     * Teto para o passo de tempo.
     *
     * <p>Quando o app volta do segundo plano, o {@code dt} do primeiro
     * quadro pode ser de vários segundos. Sem o teto, toda criatura
     * teleporta, morre de fome de uma vez e o mundo esvazia entre dois
     * quadros. Melhor a simulação andar em câmera lenta por um instante do
     * que dar um salto.
     */
    public static final float MAX_STEP_SECONDS = 0.1f;

    private final World world;
    private final FoodMap foodMap;
    private final CreaturePool pool;
    private final CreatureConfig config;
    private final Rng rng;

    private float elapsedSeconds;

    private long births;
    private long deathsByStarvation;
    private long deathsByOldAge;

    public Simulation(World world, CreatureConfig config) {
        config.validate();

        this.world = world;
        this.config = config;
        this.foodMap = new FoodMap(world);
        this.pool = new CreaturePool(config.maxCreatures);
        // Semente derivada da do mundo: dois mundos iguais geram a mesma
        // história, e mundos diferentes não compartilham a sequência.
        this.rng = new Rng(world.seed() ^ 0x5EED_1EAFL);

        spawnInitialPopulation();
    }

    public World world() {
        return world;
    }

    public FoodMap foodMap() {
        return foodMap;
    }

    public CreaturePool creatures() {
        return pool;
    }

    public CreatureConfig config() {
        return config;
    }

    public float elapsedSeconds() {
        return elapsedSeconds;
    }

    public int population() {
        return pool.activeCount();
    }

    public long births() {
        return births;
    }

    public long deathsByStarvation() {
        return deathsByStarvation;
    }

    public long deathsByOldAge() {
        return deathsByOldAge;
    }

    // ------------------------------------------------------------------ passo

    /**
     * Avança a simulação.
     *
     * @param deltaSeconds tempo decorrido; valores acima de
     *                     {@link #MAX_STEP_SECONDS} são cortados
     */
    public void step(float deltaSeconds) {
        if (deltaSeconds <= 0f) {
            return;
        }
        float dt = Math.min(deltaSeconds, MAX_STEP_SECONDS);
        elapsedSeconds += dt;

        foodMap.regrow(dt);

        // De trás para frente: mortes removem por troca com o último e
        // nascimentos entram no fim. Nesse sentido, nenhum dos dois casos
        // faz uma criatura ser pulada ou processada duas vezes.
        for (int i = pool.activeCount() - 1; i >= 0; i--) {
            stepCreature(pool.activeAt(i), dt);
        }
    }

    private void stepCreature(Creature c, float dt) {
        c.age += dt;
        c.reproductionCooldown -= dt;
        c.hunger += config.hungerPerSecond * dt;

        if (c.hunger >= 1f) {
            c.hunger = 1f;
            c.health -= config.starvationDamagePerSecond * dt;
        } else if (c.hunger <= config.hungerFullThreshold && c.health < 1f) {
            c.health = Math.min(1f, c.health + config.healthRegenPerSecond * dt);
        }

        if (c.health <= 0f) {
            deathsByStarvation++;
            die(c);
            return;
        }
        if (c.age >= config.maxAgeSeconds) {
            deathsByOldAge++;
            die(c);
            return;
        }

        switch (c.state) {
            case WANDERING:    stepWandering(c, dt); break;
            case SEEKING_FOOD: stepSeekingFood(c, dt); break;
            case EATING:       stepEating(c, dt); break;
            case SEEKING_MATE: stepSeekingMate(c, dt); break;
            default: throw new IllegalStateException("estado desconhecido: " + c.state);
        }
    }

    private void stepWandering(Creature c, float dt) {
        if (c.hunger >= config.hungerSeekThreshold) {
            c.state = CreatureState.SEEKING_FOOD;
            c.targetTile = -1;
            return;
        }
        if (c.canReproduce(config)) {
            c.state = CreatureState.SEEKING_MATE;
            c.targetMateSlot = -1;
            return;
        }
        wander(c, dt);
    }

    private void stepSeekingFood(Creature c, float dt) {
        if (c.targetTile < 0 || !foodMap.isEdible(c.targetTile)) {
            c.targetTile = findFoodNear(c.tileX(), c.tileY());
        }
        if (c.targetTile < 0) {
            // Nada comestível por perto: continua andando em vez de parar,
            // porque parada quem tem fome só morre mais devagar.
            wander(c, dt);
            return;
        }
        if (moveToTile(c, c.targetTile, dt)) {
            c.state = CreatureState.EATING;
        }
    }

    private void stepEating(Creature c, float dt) {
        int tile = currentTile(c);
        float eaten = foodMap.consume(tile, config.eatingRate * dt);
        c.hunger = Math.max(0f, c.hunger - eaten * config.hungerReliefPerFood);

        if (c.hunger <= config.hungerFullThreshold || !foodMap.isEdible(tile)) {
            c.state = CreatureState.WANDERING;
            c.targetTile = -1;
        }
    }

    private void stepSeekingMate(Creature c, float dt) {
        if (c.hunger >= config.hungerSeekThreshold) {
            c.state = CreatureState.SEEKING_FOOD;
            c.targetTile = -1;
            c.targetMateSlot = -1;
            return;
        }
        if (!c.canReproduce(config)) {
            c.state = CreatureState.WANDERING;
            c.targetMateSlot = -1;
            return;
        }

        Creature mate = resolveMate(c);
        if (mate == null) {
            wander(c, dt);
            return;
        }
        if (c.distanceTo(mate) <= config.matingDistanceTiles) {
            reproduce(c, mate);
        } else {
            moveToward(c, mate.x, mate.y, dt);
        }
    }

    // ------------------------------------------------------------ comportamento

    private void wander(Creature c, float dt) {
        if (c.targetTile < 0 || moveToTile(c, c.targetTile, dt)) {
            c.targetTile = randomWalkableNear(c.tileX(), c.tileY(), 5);
        }
    }

    private void reproduce(Creature a, Creature b) {
        a.reproductionCooldown = config.reproductionCooldownSeconds;
        b.reproductionCooldown = config.reproductionCooldownSeconds;
        a.hunger = Math.min(1f, a.hunger + config.reproductionHungerCost);
        b.hunger = Math.min(1f, b.hunger + config.reproductionHungerCost);
        a.state = CreatureState.WANDERING;
        b.state = CreatureState.WANDERING;
        a.targetMateSlot = -1;
        b.targetMateSlot = -1;

        int tile = randomWalkableNear((int) a.x, (int) a.y, 2);
        if (tile < 0) {
            return;
        }
        float x = tile % world.width() + 0.5f;
        float y = tile / world.width() + 0.5f;

        // Pool cheio devolve null: é o teto de população, não um erro.
        if (pool.spawn(x, y, config.newbornHunger, 0f) != null) {
            births++;
        }
    }

    private void die(Creature c) {
        foodMap.deposit(currentTile(c), config.corpseFoodValue);
        pool.despawn(c);
    }

    /**
     * Encontra um parceiro para {@code c}.
     *
     * <p>Reaproveita o alvo anterior enquanto ele continuar vivo, disponível
     * e por perto; só varre a lista de novo quando o alvo se perde.
     *
     * <p>A varredura é linear sobre todas as criaturas vivas. Com poucas
     * centenas de criaturas isso é barato porque apenas quem está
     * procurando parceiro varre, e a maioria não está. Se a população
     * passar de alguns milhares, a correção é indexar as criaturas em uma
     * grade espacial — está anotado como dívida técnica no README.
     */
    private Creature resolveMate(Creature c) {
        if (c.targetMateSlot >= 0 && pool.isAlive(c.targetMateSlot)) {
            Creature previous = pool.bySlot(c.targetMateSlot);
            if (previous != c
                    && previous.state == CreatureState.SEEKING_MATE
                    && c.distanceTo(previous) <= config.mateSearchRadiusTiles) {
                return previous;
            }
        }

        Creature best = null;
        float bestDistance = Float.MAX_VALUE;

        for (int i = 0, n = pool.activeCount(); i < n; i++) {
            Creature other = pool.activeAt(i);
            if (other == c || other.state != CreatureState.SEEKING_MATE) {
                continue;
            }
            float distance = c.distanceTo(other);
            if (distance < bestDistance && distance <= config.mateSearchRadiusTiles) {
                best = other;
                bestDistance = distance;
            }
        }

        c.targetMateSlot = best == null ? -1 : best.slot();
        return best;
    }

    // ---------------------------------------------------------------- movimento

    /** @return {@code true} quando a criatura chegou ao centro do tile */
    private boolean moveToTile(Creature c, int tile, float dt) {
        float targetX = tile % world.width() + 0.5f;
        float targetY = tile / world.width() + 0.5f;
        return moveToward(c, targetX, targetY, dt);
    }

    private boolean moveToward(Creature c, float targetX, float targetY, float dt) {
        float dx = targetX - c.x;
        float dy = targetY - c.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= config.arrivalDistanceTiles) {
            return true;
        }

        float stride = config.speedTilesPerSecond * dt;
        if (stride >= distance) {
            moveTo(c, targetX, targetY);
            return true;
        }
        moveTo(c, c.x + dx / distance * stride, c.y + dy / distance * stride);
        return false;
    }

    /**
     * Move a criatura, desde que o destino seja terreno caminhável.
     *
     * <p>Sem esta checagem as criaturas atravessariam o oceano em linha
     * reta ao perseguir um alvo do outro lado. Quando o passo é barrado, o
     * destino é descartado para que o próximo passo escolha outro caminho —
     * é o que evita que fiquem presas empurrando a costa para sempre.
     */
    private void moveTo(Creature c, float x, float y) {
        int tileX = (int) x;
        int tileY = (int) y;

        if (!world.inBounds(tileX, tileY) || !world.tileAtUnsafe(tileX, tileY).walkable()) {
            c.targetTile = -1;
            c.targetMateSlot = -1;
            return;
        }
        c.x = x;
        c.y = y;
    }

    private int currentTile(Creature c) {
        int x = clamp(c.tileX(), 0, world.width() - 1);
        int y = clamp(c.tileY(), 0, world.height() - 1);
        return world.index(x, y);
    }

    // ------------------------------------------------------------------ buscas

    /**
     * Procura comida em anéis quadrados crescentes ao redor do ponto.
     *
     * <p>Anéis, e não um quadrado inteiro varrido de uma vez, porque assim
     * o primeiro acerto é sempre o mais próximo e a busca termina cedo — no
     * caso comum, com comida ao redor, ela para no primeiro ou no segundo
     * anel e nunca chega perto do custo do raio inteiro.
     */
    private int findFoodNear(int centerX, int centerY) {
        int radius = config.searchRadiusTiles;

        if (isEdibleTile(centerX, centerY)) {
            return world.index(centerX, centerY);
        }

        for (int r = 1; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                int found = firstEdible(centerX + dx, centerY - r);
                if (found >= 0) return found;
                found = firstEdible(centerX + dx, centerY + r);
                if (found >= 0) return found;
            }
            for (int dy = -r + 1; dy <= r - 1; dy++) {
                int found = firstEdible(centerX - r, centerY + dy);
                if (found >= 0) return found;
                found = firstEdible(centerX + r, centerY + dy);
                if (found >= 0) return found;
            }
        }
        return -1;
    }

    private int firstEdible(int x, int y) {
        return isEdibleTile(x, y) ? world.index(x, y) : -1;
    }

    private boolean isEdibleTile(int x, int y) {
        if (!world.inBounds(x, y) || !world.tileAtUnsafe(x, y).walkable()) {
            return false;
        }
        return foodMap.isEdible(world.index(x, y));
    }

    /** Tile caminhável sorteado perto do ponto, ou -1 se não houver nenhum. */
    private int randomWalkableNear(int centerX, int centerY, int radius) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = centerX + rng.range(-radius, radius);
            int y = centerY + rng.range(-radius, radius);
            if (world.inBounds(x, y) && world.tileAtUnsafe(x, y).walkable()) {
                return world.index(x, y);
            }
        }
        return -1;
    }

    // ------------------------------------------------------------- povoamento

    private void spawnInitialPopulation() {
        int spawned = 0;
        // Teto de tentativas: um mundo quase todo submerso não deve travar
        // o jogo procurando terra que não existe.
        int maxAttempts = config.initialPopulation * 200;

        for (int attempt = 0; attempt < maxAttempts && spawned < config.initialPopulation; attempt++) {
            int x = rng.nextInt(world.width());
            int y = rng.nextInt(world.height());

            if (!world.tileAtUnsafe(x, y).walkable()) {
                continue;
            }
            // Idades e fomes iniciais espalhadas de propósito: uma população
            // inteira nascida no mesmo instante morreria de velhice junta e
            // o mundo pulsaria em ondas em vez de se estabilizar.
            Creature c = pool.spawn(x + 0.5f, y + 0.5f, rng.range(0f, 0.35f), 0f);
            if (c == null) {
                break;
            }
            c.age = rng.range(0f, config.adultAgeSeconds * 1.5f);
            spawned++;
        }
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
