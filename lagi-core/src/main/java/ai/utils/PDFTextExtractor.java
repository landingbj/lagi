package ai.utils;

import ai.common.pojo.TextBlock;
import cn.hutool.core.util.StrUtil;
import lombok.*;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PDFTextExtractor extends PDFTextStripper {

    private List<TextBlock> textBlocks = new ArrayList<>();
    private List<List<TextBlock>> results = new ArrayList<>();
    @Setter
    private String currentPageText = "";
    @Setter
    private int cursorPoint = -1;
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
        if(cursorPoint == currentPageText.length()) {
            return;
        }
        String compareText = currentPageText.substring(cursorPoint, cursorPoint + 1);
        if(!compareText.equals(t)) {
            return;
        }
        cursorPoint++;
        TextBlock block = TextBlock.builder()
                .pageNo(getCurrentPageNo())
                .x(text.getXDirAdj())
                .y(text.getYDirAdj())
                .width(text.getWidthDirAdj())
                .height(text.getHeightDir())
                .text(text.getUnicode())
                .build();

        read += t;
        textBlocks.add(block);
        String temp = read.replaceAll("版本：V1.01II", "版本：V1.0II");
        if(temp.contains(this.keyWord)) {
            if(!searchAll) {
                done = true;
            }
            textBlocks = textBlocks.subList(textBlocks.size() - keyWord.length(), textBlocks.size());
            if(searchAll) {
                results.add(textBlocks);
                read = "";
                textBlocks = new ArrayList<>();
            }
        }
    }

    @Override
    public List<List<TextPosition>> getCharactersByArticle() {
        return super.getCharactersByArticle();
    }



}

