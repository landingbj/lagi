package ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BdBlock {
    private Circle circle;
    private Rectangle rectangle;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Circle {
        private int x;
        private int y;
        private int radius;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rectangle {
        private int x0;
        private int y0;
        private int x1;
        private int y1;
    }
}