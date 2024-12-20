package ai.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ValidateCodeCreator {
    private static final String[] defaultChars = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", "A", "B", "C", "D",
            "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z"
    };


    public static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public static String randomCode(int charNum) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charNum; i++) {
            sb.append(defaultChars[random.nextInt(defaultChars.length)]);
        }
        return sb.toString();
    }

    public static BufferedImage create(String code, int width, int height, int fontSize) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);

        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 55; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }

        AffineTransform fontAT = new AffineTransform();
        fontAT.rotate(Math.toRadians(0));
        Font ft = new Font("TimesRoman", Font.PLAIN, fontSize);

        int unitWidth = fontSize - 5;
        int fontOffset = fontSize / 3;
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            g.setColor(new Color(20 + random.nextInt(110), 20 + random
                    .nextInt(110), 20 + random.nextInt(110)));
            if (random.nextInt(7) > 3)
                fontAT.rotate(Math.toRadians(-random.nextInt(10)));
            else
                fontAT.rotate(Math.toRadians(random.nextInt(10)));

            ft = ft.deriveFont(fontAT);
            g.setFont(ft);
            g.drawString(String.valueOf(chars[i]), unitWidth * i + fontOffset, fontSize);
        }
        return image;
    }

    public static void main(String[] args) throws IOException {
        String code = randomCode(4);
        BufferedImage image = create(code, 60, 20, 18);
        File outputfile = new File("e:/Desktop/image.jpg");
        ImageIO.write(image, "jpg", outputfile);
    }
}
