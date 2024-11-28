package ai.utils;

import ai.common.pojo.TextBlock;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PDFTextBlockExtractor extends PDFTextStripper {

    private List<TextBlock> textBlocks;
    private String currentPageText = "";
    private int cursorPoint;

    public PDFTextBlockExtractor(String sourceText) throws IOException {
        super();
        this.currentPageText = sourceText.replaceAll("\\s+", "");
        cursorPoint = 0;
        textBlocks = new ArrayList<>(this.currentPageText.length());
    }



    @Override
    protected void processTextPosition(TextPosition text) {
        String t =  text.getUnicode();
        t = t.replaceAll("\\s+", "");
        if(StrUtil.isBlank(t)) {
            return;
        }
        if(cursorPoint == currentPageText.length()) {
            return;
        }
        String compareText = currentPageText.substring(cursorPoint, cursorPoint + t.length());
        if(!compareText.equals(t)) {
            return;
        }
        cursorPoint = cursorPoint + t.length();
        TextBlock block = TextBlock.builder()
                .pageNo(getCurrentPageNo())
                .x(text.getXDirAdj())
                .y(text.getYDirAdj())
                .width(text.getWidthDirAdj())
                .height(text.getHeightDir())
                .text(text.getUnicode())
                .build();
        textBlocks.add(block);

    }



}

