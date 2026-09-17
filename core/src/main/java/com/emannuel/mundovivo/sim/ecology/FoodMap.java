package com.emannuel.mundovivo.sim.ecology;

import com.emannuel.mundovivo.sim.world.TileType;
import com.emannuel.mundovivo.sim.world.World;

/**
 * Quanta comida existe em cada tile, e como ela volta a crescer.
 *
 * <p>A capacidade de um tile é a fertilidade do seu bioma: selva sustenta
 * muito, deserto quase nada, água nenhuma. A rebrota é proporcional à
 * capacidade, então terreno rico se recupera rápido e terreno pobre demora
 * — é essa diferença que vai fazer as criaturas se concentrarem em certas
 * regiões sem que ninguém programe isso explicitamente.
 *
 * <p>Um array plano de {@code float}, um valor por tile, pelos mesmos
 * motivos do {@link World}.
 */
public final class FoodMap {

    /** Fração da capacidade que volta a cada segundo. */
    public static final float DEFAULT_REGROWTH_PER_SECOND = 0.003f;

    /** Abaixo disso o tile é considerado pelado e não vale a pena procurar. */
    public static final float EDIBLE_THRESHOLD = 0.02f;

    private final World world;
    private final float[] amount;
    private final float[] capacity;

    private float regrowthPerSecond = DEFAULT_REGROWTH_PER_SECOND;

    /**
     * Cria o mapa já cheio: cada tile começa na capacidade do seu bioma.
     * Um mundo recém-gerado é um mundo virgem, não um mundo faminto.
     */
    public FoodMap(World world) {
        this.world = world;
        int size = world.tileCount();
        this.amount = new float[size];
        this.capacity = new float[size];

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                int index = world.index(x, y);
                float fertility = world.tileAtUnsafe(x, y).fertility();
                capacity[index] = fertility;
                amount[index] = fertility;
            }
        }
    }

    public float regrowthPerSecond() {
        return regrowthPerSecond;
    }

    public void regrowthPerSecond(float value) {
        if (value < 0f) {
            throw new IllegalArgumentException("rebrota não pode ser negativa: " + value);
        }
        this.regrowthPerSecond = value;
    }

    public float amountAt(int index) {
        return amount[index];
    }

    public float amountAt(int x, int y) {
        return amount[world.index(x, y)];
    }

    public float capacityAt(int index) {
        return capacity[index];
    }

    public boolean isEdible(int index) {
        return amount[index] >= EDIBLE_THRESHOLD;
    }

    /**
     * Consome até {@code requested} de comida do tile.
     *
     * @return quanto foi efetivamente consumido, que pode ser menos que o
     *         pedido se o tile estava quase pelado
     */
    public float consume(int index, float requested) {
        if (requested <= 0f) {
            return 0f;
        }
        float available = amount[index];
        float taken = Math.min(available, requested);
        amount[index] = available - taken;
        return taken;
    }

    /** Devolve comida ao tile, respeitando a capacidade (ex.: um cadáver). */
    public void deposit(int index, float value) {
        amount[index] = Math.min(capacity[index], amount[index] + value);
    }

    /**
     * Faz a comida crescer em todo o mapa.
     *
     * <p>Percorre os 49 mil tiles do mundo padrão a cada passo. Custa
     * frações de milissegundo hoje; se um dia virar gargalo, a correção é
     * dividir o mapa em fatias e crescer uma fatia por frame com o dt
     * multiplicado — não é preciso que cada tile cresça todo frame.
     */
    public void regrow(float deltaSeconds) {
        if (deltaSeconds <= 0f) {
            return;
        }
        float rate = regrowthPerSecond * deltaSeconds;

        for (int i = 0; i < amount.length; i++) {
            float max = capacity[i];
            if (max <= 0f) {
                continue;
            }
            float current = amount[i] + max * rate;
            amount[i] = current > max ? max : current;
        }
    }

    /**
     * Recalcula a capacidade de um tile depois que o terreno mudou.
     * Os poderes de deus vão precisar disto: transformar floresta em
     * deserto tem que secar a comida junto.
     */
    public void retile(int x, int y, TileType newType) {
        int index = world.index(x, y);
        capacity[index] = newType.fertility();
        if (amount[index] > capacity[index]) {
            amount[index] = capacity[index];
        }
    }

    /** Soma de toda a comida do mundo — usado em testes e telas de estatística. */
    public float totalFood() {
        float total = 0f;
        for (float value : amount) {
            total += value;
        }
        return total;
    }

    /** Acesso direto para serialização do save. Não escreva por fora do load. */
    public float[] rawAmount() {
        return amount;
    }
}
