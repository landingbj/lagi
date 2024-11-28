package ai.common.pojo;

import lombok.*;
import org.jetbrains.annotations.NotNull;

@ToString
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class TextBlock implements Comparable<TextBlock>{
    private int pageNo;
    private float x;
    private float y ;
    private float width;
    private float height;
    private String text;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TextBlock other = (TextBlock) obj;
        int x0 = (int) x;
        int y0 = (int) y;
        int w0 = (int) width;
        int h0 = (int) height;
        int x1 = (int) other.x;
        int y1 = (int) other.y;
        int w1 = (int) other.width;
        int h1 = (int) other.height;
        String t1 = text.replaceAll("\\s+", "");
        String t2 = other.text.replaceAll("\\s+", "");
        int c = 0;
        return pageNo == other.pageNo
                && t1.equals(t2)
                && Math.abs(x0 - x1) <= c
                && Math.abs(y0 - y1) <= c
                && Math.abs(w0 - w1) <= c
                && Math.abs(h0 - h1) <= c
                ;
    }



    @Override
    public int hashCode() {
        return text.replaceAll("\\s+", "").hashCode();
    }

    @Override
    public int compareTo(@NotNull TextBlock o) {
        if(this.y < o.y) {
            return -1;
        }else if(this.y > o.y) {
            return 1;
        }
        if(this.x == o.x) {
            return 0;
        }
        return this.x - o.x > 0 ? 1 : -1;
    }
}