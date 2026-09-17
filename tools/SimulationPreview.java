import com.emannuel.mundovivo.sim.Simulation;
import com.emannuel.mundovivo.sim.creature.Creature;
import com.emannuel.mundovivo.sim.creature.CreatureConfig;
import com.emannuel.mundovivo.sim.creature.CreatureState;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Gera um PNG do mundo com as criaturas em cima, depois de rodar a
 * simulação por um tempo.
 *
 * <p>Complementa o {@code SimulationReport}: o relatório mostra os números,
 * este mostra onde as criaturas estão. É como se vê que elas se juntam no
 * terreno fértil e somem do deserto, coisa que nenhuma média revela.
 *
 * <p>Mesmas cores do jogo: branco vagando, amarelo procurando comida, verde
 * comendo, rosa procurando parceiro.
 *
 * <p>Uso:
 * {@code java -cp build/sim:build/tools SimulationPreview [seed] [minutos] [escala] [saida.png]}
 */
public final class SimulationPreview {

    private static final float STEP = 1f / 60f;

    private static final int COLOR_WANDERING = 0xFFEBEBF5;
    private static final int COLOR_SEEKING_FOOD = 0xFFFFC747;
    private static final int COLOR_EATING = 0xFF6BEB73;
    private static final int COLOR_SEEKING_MATE = 0xFFFF73B3;

    public static void main(String[] args) throws IOException {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 12345L;
        float minutes = args.length > 1 ? Float.parseFloat(args[1]) : 10f;
        int scale = args.length > 2 ? Integer.parseInt(args[2]) : 3;
        String output = args.length > 3 ? args[3] : "previa-simulacao-" + seed + ".png";

        World world = WorldGenerator.generate(WorldConfig.medium(seed));
        Simulation sim = new Simulation(world, new CreatureConfig());

        int steps = (int) (minutes * 60f / STEP);
        for (int i = 0; i < steps; i++) {
            sim.step(STEP);
        }

        BufferedImage image = new BufferedImage(
                world.width() * scale, world.height() * scale, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                fill(image, x * scale, y * scale, scale, world.tileAt(x, y).colorArgb());
            }
        }

        int dot = Math.max(2, scale);
        for (int i = 0; i < sim.population(); i++) {
            Creature c = sim.creatures().activeAt(i);
            int px = (int) (c.x * scale) - dot / 2;
            int py = (int) (c.y * scale) - dot / 2;
            fill(image, px, py, dot, colorFor(c.state));
        }

        ImageIO.write(image, "png", new File(output));

        System.out.printf("seed=%d  %.0f min simulados  populacao=%d  nascimentos=%d%n",
                seed, minutes, sim.population(), sim.births());
        System.out.printf("mortes: %d de fome, %d de velhice   comida restante=%.0f%n",
                sim.deathsByStarvation(), sim.deathsByOldAge(), sim.foodMap().totalFood());
        System.out.println("PNG: " + output);
    }

    private static void fill(BufferedImage image, int x0, int y0, int size, int argb) {
        for (int y = y0; y < y0 + size; y++) {
            for (int x = x0; x < x0 + size; x++) {
                if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
                    image.setRGB(x, y, argb);
                }
            }
        }
    }

    private static int colorFor(CreatureState state) {
        switch (state) {
            case SEEKING_FOOD: return COLOR_SEEKING_FOOD;
            case EATING:       return COLOR_EATING;
            case SEEKING_MATE: return COLOR_SEEKING_MATE;
            default:           return COLOR_WANDERING;
        }
    }
}
