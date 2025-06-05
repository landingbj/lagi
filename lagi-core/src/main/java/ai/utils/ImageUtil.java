package ai.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class ImageUtil {

    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    public static String getFileContentAsBase64(String path) throws IOException {
        byte[] b = Files.readAllBytes(Paths.get(path));
        return Base64.getEncoder().encodeToString(b);
    }

    public static File base64ToFile(String base64) {
        if (base64.contains("data:image")) {
            base64 = base64.substring(base64.indexOf(",") + 1);
        }
        base64 = base64.toString().replace("\r\n", "");
        //创建文件目录
        String prefix = ".jpeg";
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = File.createTempFile(UUID.randomUUID().toString(), prefix);
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes = decoder.decodeBuffer(base64);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            log.error("base64ToFile error", e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error("base64ToFile close io error", e);
                }
            }
        }
        return file;
    }


    public static byte[] getFileStream(String url) {
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            return readInputStream(inStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    public static BufferedImage keepRedPart(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = src.getRGB(x, y);
                if (isRed(argb)) {
                    dest.setRGB(x, y, argb);
                } else {
                    dest.setRGB(x, y, 0xFFFFFFFF);
                }
            }
        }
        return dest;
    }

    public static List<Rectangle> getRedBoundingBoxes(BufferedImage src, int mergeDistance) {
        int width = src.getWidth();
        int height = src.getHeight();

        boolean[][] isRed = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = src.getRGB(x, y);
                if (isRed(rgb)) {
                    isRed[y][x] = true;
                }
            }
        }

        boolean[][] visited = new boolean[height][width];
        java.util.List<Rectangle> boxes = new ArrayList<>();
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isRed[y][x] && !visited[y][x]) {
                    int minX = x, maxX = x;
                    int minY = y, maxY = y;

                    Deque<int[]> queue = new ArrayDeque<>();
                    queue.addLast(new int[]{x, y});
                    visited[y][x] = true;

                    while (!queue.isEmpty()) {
                        int[] p = queue.removeFirst();
                        int curX = p[0], curY = p[1];

                        if (curX < minX) minX = curX;
                        if (curX > maxX) maxX = curX;
                        if (curY < minY) minY = curY;
                        if (curY > maxY) maxY = curY;

                        for (int k = 0; k < 4; k++) {
                            int nx = curX + dx[k];
                            int ny = curY + dy[k];
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                if (isRed[ny][nx] && !visited[ny][nx]) {
                                    visited[ny][nx] = true;
                                    queue.addLast(new int[]{nx, ny});
                                }
                            }
                        }
                    }
                    int widthRect = maxX - minX;
                    int heightRect = maxY - minY;
                    boxes.add(new Rectangle(minX, minY, widthRect, heightRect));
                }
            }
        }

        List<Rectangle> merged = mergeCloseRectangles(boxes, mergeDistance);
        List<Rectangle> result = new ArrayList<>();

        for (Rectangle rect : merged) {
            if (rect.width < 3 || rect.height < 3) {
                continue;
            }
            result.add(expandRectangle(rect, 3, 3));
        }

        return result;
    }

    public static Rectangle expandRectangle(Rectangle rect, int expandX, int expandY) {
        return new Rectangle(
                rect.x - expandX,
                rect.y - expandY,
                rect.width + 2 * expandX,
                rect.height + 2 * expandY
        );
    }

    public static List<BufferedImage> cropImageByRect(BufferedImage src, List<Rectangle> rectangles) {
        List<BufferedImage> result = new ArrayList<>();
        for (Rectangle rect : rectangles) {
            Rectangle boundedRect = rect.intersection(new Rectangle(0, 0, src.getWidth(), src.getHeight()));
            if (boundedRect.isEmpty()) {
                continue;
            }
            BufferedImage subImg = src.getSubimage(
                    boundedRect.x, boundedRect.y, boundedRect.width, boundedRect.height
            );
            result.add(subImg);
        }
        return result;
    }

    private static List<Rectangle> mergeCloseRectangles(List<Rectangle> boxes, int mergeDistance) {
        List<Rectangle> list = new ArrayList<>(boxes);
        boolean mergedSomething = true;

        while (mergedSomething) {
            mergedSomething = false;
            int size = list.size();
            boolean[] merged = new boolean[size];
            List<Rectangle> newList = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                if (merged[i]) continue;
                Rectangle r1 = list.get(i);
                for (int j = i + 1; j < size; j++) {
                    if (merged[j]) continue;
                    Rectangle r2 = list.get(j);
                    if (areClose(r1, r2, mergeDistance)) {
                        Rectangle combined = r1.union(r2);
                        merged[j] = true;
                        r1 = combined;
                        mergedSomething = true;
                    }
                }
                merged[i] = true;
                newList.add(r1);
            }
            list = newList;
        }

        return list;
    }

    private static boolean areClose(Rectangle r1, Rectangle r2, int mergeDistance) {
        Rectangle expanded = new Rectangle(
                r1.x - mergeDistance,
                r1.y - mergeDistance,
                r1.width + 2 * mergeDistance,
                r1.height + 2 * mergeDistance
        );
        return expanded.intersects(r2);
    }


    public static BufferedImage expandImage(BufferedImage src, int expandX, int expandY) {
        int newWidth = src.getWidth() + expandX * 2;
        int newHeight = src.getHeight() + expandY * 2;
        BufferedImage expanded = new BufferedImage(newWidth, newHeight, src.getType());
        Graphics2D g = expanded.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        g.drawImage(src, expandX, expandY, null);
        g.dispose();
        return expanded;
    }

    public static BufferedImage removeSmallRedAreas(BufferedImage img, int minArea) {
        int width = img.getWidth();
        int height = img.getHeight();
        boolean[][] visited = new boolean[width][height];
        BufferedImage result = new BufferedImage(width, height, img.getType());

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                result.setRGB(x, y, Color.WHITE.getRGB());

        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!visited[x][y] && isRed(img.getRGB(x, y))) {
                    LinkedList<int[]> areaPixels = new LinkedList<>();
                    Queue<int[]> queue = new LinkedList<>();
                    queue.offer(new int[]{x, y});
                    visited[x][y] = true;

                    while (!queue.isEmpty()) {
                        int[] p = queue.poll();
                        areaPixels.add(p);
                        for (int[] d : directions) {
                            int nx = p[0] + d[0];
                            int ny = p[1] + d[1];
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height
                                    && !visited[nx][ny] && isRed(img.getRGB(nx, ny))) {
                                visited[nx][ny] = true;
                                queue.offer(new int[]{nx, ny});
                            }
                        }
                    }
                    if (areaPixels.size() >= minArea) {
                        for (int[] p : areaPixels) {
                            result.setRGB(p[0], p[1], img.getRGB(p[0], p[1]));
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isRed(int rgb) {
        Color color = new Color(rgb, true);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return isRed(r, g, b);
    }

    private static boolean isRed(int r, int g, int b) {
        return r > 120 && r > g * 1.1 && r > b * 1.1;
    }

    public static BufferedImage mergeImages(List<BufferedImage> images, int gap, int padX, int padY) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("图片列表不能为空");
        }

        // 计算最大高度和总宽度
        int maxHeight = 0;
        int totalWidth = 0;

        for (BufferedImage image : images) {
            if (image != null) {
                maxHeight = Math.max(maxHeight, image.getHeight());
                totalWidth += image.getWidth();
            }
        }

        // 计算最终图片尺寸
        int finalWidth = totalWidth + (images.size() - 1) * gap + 2 * padX;
        int finalHeight = maxHeight + 2 * padY;

        // 创建新的BufferedImage
        BufferedImage mergedImage = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = mergedImage.createGraphics();

        // 设置背景色为白色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, finalWidth, finalHeight);

        // 启用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 绘制图片
        int currentX = padX;
        for (int i = 0; i < images.size(); i++) {
            BufferedImage image = images.get(i);
            if (image != null) {
                // 计算垂直居中位置
                int y = padY + (maxHeight - image.getHeight()) / 2;

                // 绘制图片
                g2d.drawImage(image, currentX, y, null);

                // 更新X坐标
                currentX += image.getWidth();

                // 如果不是最后一张图片，添加间隔
                if (i < images.size() - 1) {
                    currentX += gap;
                }
            }
        }

        g2d.dispose();
        return mergedImage;
    }

    public static BufferedImage binaryImageOptimized(BufferedImage image, int threshold) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        if (threshold < 0 || threshold > 255) {
            throw new IllegalArgumentException("Threshold must be between 0 and 255");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                if (gray >= threshold) {
                    binaryImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    binaryImage.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        return binaryImage;
    }

    public static void main(String[] args) {
        try {
            File inputFile = new File("E:\\Desktop\\络明芯规则\\bd_1.png");
            BufferedImage inputImage = ImageIO.read(inputFile);

            BufferedImage outputImage = keepRedPart(inputImage);

            File outputFile = new File("output_keep_red.png");
            ImageIO.write(outputImage, "png", outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
