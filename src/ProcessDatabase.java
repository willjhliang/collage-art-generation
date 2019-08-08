import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class ProcessDatabase {
    private static String compPath = "src/database";

    private static int PIC_CNT = 5640;

    private static File[] imgs;
    public static void main(String[] args) {
        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter("src/avgs.txt")));
        } catch (IOException e) {
            System.out.println("Failed to create avgs.txt");
            return;
        }
        File f = new File(compPath);
        imgs = f.listFiles();

        PIC_CNT = imgs.length;
        for (int l = 0; l < PIC_CNT; l++) {
            System.out.println(imgs[l].getName());
            BufferedImage img;
            try {
                img = ImageIO.read(imgs[l]);
            } catch (IOException e) {
                System.out.println("Failed to load image");
                return;
            }
            int[] sum = {0, 0, 0};
            int area = img.getWidth() * img.getHeight();
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color c = new Color(img.getRGB(i, j));
                    sum[0] += c.getRed(); sum[1] += c.getGreen(); sum[2] += c.getBlue();
                }
            }
            out.println((sum[0] / area) + " " + (sum[1] / area) + " " + (sum[2] / area));
        }
        out.close();
    }
}
