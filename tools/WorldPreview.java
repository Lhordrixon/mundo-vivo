import com.emannuel.mundovivo.sim.world.TileType;
import com.emannuel.mundovivo.sim.world.World;
import com.emannuel.mundovivo.sim.world.WorldConfig;
import com.emannuel.mundovivo.sim.world.WorldGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Gera um PNG de prévia do mundo sem abrir o jogo.
 *
 * <p>Serve para ajustar os parâmetros de {@link WorldConfig} em segundos,
 * em vez de recompilar o APK e olhar no celular. Só usa Java puro — não
 * depende de libGDX nem do SDK do Android, então roda em qualquer máquina
 * com JDK instalado.
 *
 * <p>Uso:
 * <pre>
 *   javac -d build/tools -cp build/sim tools/WorldPreview.java
 *   java  -cp build/sim:build/tools WorldPreview [seed] [escala] [saida.png]
 * </pre>
 */
public final class WorldPreview {

    public static void main(String[] args) throws IOException {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 12345L;
        int scale = args.length > 1 ? Integer.parseInt(args[1]) : 3;
        String output = args.length > 2 ? args[2] : "preview-" + seed + ".png";

        WorldConfig config = WorldConfig.medium(seed);

        long startedAt = System.nanoTime();
        World world = WorldGenerator.generate(config);
        double elapsedMs = (System.nanoTime() - startedAt) / 1_000_000.0;

        writePng(world, scale, output);
        printReport(world, config, elapsedMs, output);
    }

    private static void writePng(World world, int scale, String output) throws IOException {
        BufferedImage image = new BufferedImage(
                world.width() * scale, world.height() * scale, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                int argb = world.tileAt(x, y).colorArgb();
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        image.setRGB(x * scale + sx, y * scale + sy, argb);
                    }
                }
            }
        }
        ImageIO.write(image, "png", new File(output));
    }

    private static void printReport(World world, WorldConfig config, double elapsedMs, String output) {
        Map<TileType, Integer> histogram = new EnumMap<>(TileType.class);
        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                histogram.merge(world.tileAt(x, y), 1, Integer::sum);
            }
        }

        System.out.println("=== PREVIA DE MUNDO ===");
        System.out.println(config);
        System.out.printf("Gerado em %.1f ms (%d tiles)%n", elapsedMs, world.tileCount());
        System.out.printf("Terra: %.1f%%   Agua: %.1f%%%n",
                world.landFraction() * 100f, (1f - world.landFraction()) * 100f);
        System.out.println("PNG: " + output);
        System.out.println("--- distribuicao de biomas ---");
        for (TileType type : TileType.VALUES) {
            int count = histogram.getOrDefault(type, 0);
            if (count == 0) {
                continue;
            }
            double percent = 100.0 * count / world.tileCount();
            System.out.printf("%-14s %6.2f%%  %s%n", type, percent, bar(percent));
        }
    }

    private static String bar(double percent) {
        int length = (int) Math.round(percent / 2.0);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('#');
        }
        return sb.toString();
    }
}
