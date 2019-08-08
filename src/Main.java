
import java.awt.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {
    private static String refPath = "";
    private static String compPath = "database";

    private static int INF = 200000000;

    private static int PIC_CNT = 5640;
    private static int[][] MAX_RANGE = {{70, 50}, {50, 20}}; //{base, deviation}
    private static int MAX_HSIZE = 500;
    private static int LIM = 130;
    private static int[] dx = {0, 1, 0, -1};
    private static int[] dy = {-1, 0, 1, 0};

    private static int width, height;
    private static BufferedImage ref, collage, sections;

    private static boolean[][] visited;
    private static File fRef;
    private static File[] imgs;
    private static Color[] avgs = new Color[PIC_CNT + 1];

    private static JFrame f = new JFrame();
    private static JLabel jl;
    private static JFrame fS = new JFrame();
    private static JLabel jSL;

    public static void main(String[] args) {
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fS.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Scanner sc = new Scanner(System.in);
        refPath = sc.nextLine();
        try {
            fRef = new File(refPath);
            ref = ImageIO.read(fRef);
            System.out.println("Loading success");

        } catch (IOException e) {
            System.out.println("Loading failed");
            return;
        }

        width = ref.getWidth(); height = ref.getHeight();
        visited = new boolean[width + 1][height + 1];


        try {
            File f = new File(compPath); imgs = f.listFiles();
            PIC_CNT = imgs.length;
            BufferedReader in = new BufferedReader(new FileReader("src/avgs.txt"));
            for (int i = 0; i < PIC_CNT; i++) {
                StringTokenizer st = new StringTokenizer(in.readLine());
                avgs[i] = new Color(Integer.parseInt(st.nextToken()),
                                  Integer.parseInt(st.nextToken()),
                                  Integer.parseInt(st.nextToken()));
            }

        } catch (IOException e) {
            System.out.println("Failed to load avgs.txt");
            return;
        }

        collage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        sections = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        jl = new JLabel(new ImageIcon(collage));
        jSL = new JLabel(new ImageIcon(sections));
        f.getContentPane().add(jl); fS.getContentPane().add(jSL);
        f.pack(); fS.pack();
        f.setVisible(true); fS.setVisible(true);

        System.out.println("Generating collage");
        try {
            boolean d = false;
            while (!d) {
                d = true;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        if (!visited[i][j]) {
                            bfs(new Coord(i, j));
                            d = false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to generate collage");
            return;
        }
        System.out.println();

        System.out.print("Save? (y/n) ");
        String s = sc.nextLine();
        if (s.equals("y")) {
            File out = new File("out/production/artwork/" + fRef.getName());
            try {
                ImageIO.write(collage, "jpg", out);
                System.out.println("Saving success");
            } catch (IOException e) {
                System.out.println("Saving failed");
            }
        }
    }

    static void bfs(Coord s) throws IOException {
        ArrayList<Coord> pixels = new ArrayList<>();
        int[] sum = {0, 0, 0};
        Coord tl = new Coord(INF, INF), br = new Coord(-INF, -INF);

        int k = (int)(Math.random() * MAX_RANGE.length);
        int size = 0;
        Queue<Coord> q = new LinkedList<>();
        q.add(s);
        while (!q.isEmpty()) {
            Coord cur = q.remove();
            if (visited[cur.x][cur.y]) continue;
            if (getDist(s, cur) > MAX_RANGE[k][0] + Math.random() * (MAX_RANGE[k][1])) continue;

            System.out.print("\r");
            System.out.print(size);

            tl.x = Math.min(tl.x, cur.x); tl.y = Math.min(tl.y, cur.y);
            br.x = Math.max(br.x, cur.x); br.y = Math.max(br.y, cur.y);
            Color curC = new Color(ref.getRGB(cur.x, cur.y));
            size++; pixels.add(cur);
            changeVisited(cur.x, cur.y, true);
            sum[0] += curC.getRed();
            sum[1] += curC.getGreen();
            sum[2] += curC.getBlue();

            outer : for (int i = 0; i < 4; i++) {
                Coord next = new Coord(cur.x + dx[i], cur.y + dy[i]);
                if (next.x < 0 || next.x >= width || next.y < 0 || next.y >= height) continue;
                if (visited[next.x][next.y]) continue;
                for (Coord p : pixels)
                    if (diff(new Color(ref.getRGB(next.x, next.y)), new Color(ref.getRGB(p.x, p.y))) > LIM)
                        continue outer;
                q.add(next);
            }
        }

        //fill small holes
        for (int i = tl.x; i <= br.x; i++) {
            outer : for (int j = tl.y; j <= br.y; j++) {
                ArrayList<Coord> hPixels = new ArrayList<>();

                if (visited[i][j]) continue;
                int hSize = 0;
                q = new LinkedList<>();
                q.add(new Coord(i, j));
                while (!q.isEmpty()) {
                    Coord cur = q.remove();
                    if (cur.x < 0 || cur.x >= width || cur.y < 0 || cur.y >= height) continue;
                    if (visited[cur.x][cur.y]) continue;

                    hSize++;
                    hPixels.add(cur);
                    changeVisited(cur.x, cur.y, true);

                    if (hSize > MAX_HSIZE) {
                        for (Coord p : hPixels) {
                            changeVisited(p.x, p.y, false);
                        }
                        continue outer;
                    }
                    for (int l = 0; l < 4; l++) {
                        q.add(new Coord(cur.x + dx[l], cur.y + dy[l]));
                    }
                }
                for (Coord p : hPixels) {
                    pixels.add(p);
                }
            }
        }

        Color avg = new Color(sum[0] / size, sum[1] / size, sum[2] / size);
        BufferedImage img = ImageIO.read(imgs[getPic(avg)]);

        Coord shift = new Coord(0, 0);
        shift.x = (int)(Math.random() * Math.max(0, img.getWidth() - br.x - 1));
        shift.y = (int)(Math.random() * Math.max(0, img.getHeight() - br.y - 1));

        for (Coord p : pixels) {
            Coord mapped = new Coord(p.x - tl.x + shift.x, p.y - tl.y + shift.y);
            if (mapped.x >= img.getWidth() || mapped.y >= img.getHeight() || mapped.x < 0 || mapped.y < 0)
                changeVisited(p.x, p.y, false);
            else collage.setRGB(p.x, p.y, img.getRGB(mapped.x, mapped.y));
        }
        jl.setIcon(new ImageIcon(collage));
        jSL.setIcon(new ImageIcon(sections));
    }

    static int getPic(Color avg) {
        int ret = 0;
        for (int i = 0; i < PIC_CNT; i++)
            if (diff(avgs[i], avg) < diff(avgs[ret], avg)) ret = i;
        return ret;
    }
    static int getDist(Coord a, Coord b) {
        return (int)Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    static int diff(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) +
               Math.abs(a.getGreen() - b.getGreen()) +
               Math.abs(a.getBlue() - b.getBlue());
    }
    static void changeVisited(int x, int y, boolean s) {
        visited[x][y] = s;
        sections.setRGB(x, y, ((s) ? 16077122 : 0));
    }

    static class Coord {
        int x, y;
        Coord(int a, int b) {
            x = a; y = b;
        }
    }
}
