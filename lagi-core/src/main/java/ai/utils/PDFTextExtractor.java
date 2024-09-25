package ai.utils;

import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class PDFTextExtractor extends PDFTextStripper {

    private List<TextBlock> textBlocks = new ArrayList<>();
    private Set<TextBlock> textBlockSet = new HashSet<>();
    private List<List<TextBlock>> results = new ArrayList<>();
    private String read = "";
    private boolean done;
    private boolean searchAll;
    private String keyWord;

    public PDFTextExtractor() throws IOException {
        super();
    }

    public PDFTextExtractor(String keyWord, boolean searchAll) throws IOException {
        super();
        this.keyWord = keyWord;
        this.done = false;
        this.searchAll = searchAll;
    }

    public boolean getDone() {
        return this.done;
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        String t =  text.getUnicode();
        if(done) {
            return;
        }
        if(t == null || StrUtil.isBlank(t)) {
            return;
        }
        t = t.replaceAll("\\s+", "");
        if(StrUtil.isBlank(t)) {
            return;
        }
        TextBlock block = TextBlock.builder()
                .pageNo(getCurrentPageNo())
                .x(text.getXDirAdj())
                .y(text.getYDirAdj())
                .width(text.getWidthDirAdj())
                .height(text.getHeightDir())
                .text(text.getUnicode())
                .build();
        boolean add = textBlockSet.add(block);

        if(!add) {
            return;
        }
        read += t;
        textBlocks.add(block);
        String temp = read.replaceAll("版本：V1.01II", "版本：V1.0II");
        if(temp.contains(this.keyWord)) {
            if(!searchAll) {
                done = true;
                textBlockSet.clear();
            }
            textBlocks = textBlocks.subList(textBlocks.size() - keyWord.length(), textBlocks.size());
            if(searchAll) {
                results.add(textBlocks);
                read = "";
                textBlocks = new ArrayList<>();
                textBlockSet.clear();
            }
        }
    }

    @Override
    public List<List<TextPosition>> getCharactersByArticle() {
        return super.getCharactersByArticle();
    }


    @ToString
    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class TextBlock implements Comparable<TextBlock>{
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
}

