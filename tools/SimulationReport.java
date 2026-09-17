import com.emannuel.mundovivo.sim.Simulation;
import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.creature.CreatureState;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

import java.util.EnumMap;
import java.util.Map;

/**
 * Roda a simulação por um tempo e mostra como a população se comporta.
 *
 * <p>Serve para calibrar {@link CreatureConfig} sem abrir o jogo: se a
 * população morre toda no primeiro minuto ou bate no teto do pool e fica
 * lá, aparece aqui em segundos.
 *
 * <p>Uso: {@code java -cp build/sim:build/tools SimulationReport [seed] [minutos]}
 */
public final class SimulationReport {

    private static final float STEP = 1f / 60f;

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 12345L;
        float minutes = args.length > 1 ? Float.parseFloat(args[1]) : 5f;

        World world = WorldGenerator.generate(WorldConfig.medium(seed));
        Simulation sim = new Simulation(world, new CreatureConfig());

        System.out.println("=== SIMULACAO ===");
        System.out.printf("mundo %dx%d  seed=%d  terra=%.0f%%  comida inicial=%.0f%n",
                world.width(), world.height(), seed, world.landFraction() * 100f,
                sim.foodMap().totalFood());
        System.out.printf("populacao inicial: %d%n%n", sim.population());
        System.out.printf("%8s %7s %8s %8s %9s %8s %8s%n",
                "tempo", "pop", "nasc.", "fome+", "velhice+", "comida", "ms/passo");

        int totalSteps = (int) (minutes * 60f / STEP);
        int reportEvery = totalSteps / 12;
        long busyNanos = 0L;

        for (int i = 1; i <= totalSteps; i++) {
            long startedAt = System.nanoTime();
            sim.step(STEP);
            busyNanos += System.nanoTime() - startedAt;

            if (i % reportEvery == 0) {
                double msPerStep = busyNanos / 1_000_000.0 / reportEvery;
                busyNanos = 0L;
                System.out.printf("%7.0fs %7d %8d %8d %9d %8.0f %8.3f%n",
                        sim.elapsedSeconds(), sim.population(), sim.births(),
                        sim.deathsByStarvation(), sim.deathsByOldAge(),
                        sim.foodMap().totalFood(), msPerStep);
            }
        }

        System.out.println("\n--- estados no fim ---");
        Map<CreatureState, Integer> states = new EnumMap<>(CreatureState.class);
        float avgHunger = 0f;
        float avgAge = 0f;
        for (int i = 0; i < sim.population(); i++) {
            Creature c = sim.creatures().activeAt(i);
            states.merge(c.state, 1, Integer::sum);
            avgHunger += c.hunger;
            avgAge += c.age;
        }
        for (CreatureState state : CreatureState.VALUES) {
            System.out.printf("%-14s %5d%n", state, states.getOrDefault(state, 0));
        }
        if (sim.population() > 0) {
            System.out.printf("fome media: %.2f   idade media: %.0fs%n",
                    avgHunger / sim.population(), avgAge / sim.population());
        }
        System.out.printf("reaproveitamento do pool: %d nascimentos em %d slots%n",
                sim.creatures().totalSpawns(), sim.creatures().capacity());
    }
}
