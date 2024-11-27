package ai.ocr.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AlibabaOcrDocument {
    private String algo_version;
    private int angle;
    private String content;
    private int height;
    private int orgHeight;
    private int orgWidth;
    private String prism_version;
    private int prism_wnum;
    private List<Figure> figure;
    private List<PrismParagraphInfo> prism_paragraphsInfo;
    private List<PrismWordInfo> prism_wordsInfo;
    private List<PrismTablesInfo> prism_tablesInfo;
    private List<TableHeadTail> tableHeadTail;
    private int width;

    @Data
    public static class TableHeadTail {
        private int tableId;
        private List<String> head;
        private List<String> tail;
    }

    @Data
    public static class PrismTablesInfo {
        private int tableId;
        @JsonProperty("xCellSize")
        private int xCellSize;
        @JsonProperty("yCellSize")
        private int yCellSize;
        private List<CellInfo> cellInfos;
    }

    @Data
    public static class CellInfo {
        private int tableCellId;
        private String word;
        private int xec;
        private int xsc;
        private int yec;
        private int ysc;
        private List<Position> pos;
    }

    @Data
    public static class Figure {
        private String type;
        private int x;
        private int y;
        private int w;
        private int h;
        private Box box;
        private List<Point> points;
    }

    @Data
    public static class Box {
        private int x;
        private int y;
        private int w;
        private int h;
        private int angle;
    }

    @Data
    public static class Point {
        private int x;
        private int y;
    }

    @Data
    public static class PrismParagraphInfo {
        private int paragraphId;
        private String word;
    }

    @Data
    public static class PrismWordInfo {
        private int angle;
        private int direction;
        private int height;
        private int paragraphId;
        private List<Position> pos;
        private int prob;
        private int tableCellId;
        private int tableId;
        private int width;
        private String word;
        private int x;
        private int y;
        private int recClassify;
    }

    @Data
    public static class Position {
        private int x;
        private int y;
    }
}