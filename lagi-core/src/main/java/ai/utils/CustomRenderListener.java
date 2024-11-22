package ai.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.util.ArrayList;
import java.util.List;

public class CustomRenderListener implements RenderListener {

    private float[] pcoordinate = null;

    private String keyWord;

    private int page;

    private String readed = "";
    private List<List<java.lang.Float>> readedPcoordinate = new ArrayList<>();
    private List<Integer> lengths = new ArrayList<>();

    private boolean findOutKeyWord = false;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public float[] getPcoordinate(){
        return pcoordinate;
    }

    public List<List<java.lang.Float>> getSearchedPcoordinate(){
        if(!findOutKeyWord) {
            return Lists.newArrayList();
        }
        return readedPcoordinate;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    @Override
    public void beginTextBlock() {}

    @Override
    public void endTextBlock() {}

    @Override
    public void renderImage(ImageRenderInfo arg0) {}

    @Override
    public void renderText(TextRenderInfo textRenderInfo) {
        String text = textRenderInfo.getText();
        if(text == null || StrUtil.isBlank(text)) {
            return;
        }
        text = text.replaceAll("\\s+", "");
        if(StrUtil.isBlank(text)) {
            return;
        }
        if(findOutKeyWord) {
            return;
        }
        readed += text;
        Float boundingRectange = textRenderInfo.getBaseline().getBoundingRectange();
        List<java.lang.Float> temp = new  ArrayList<>();
        temp.add(boundingRectange.x);
        temp.add(boundingRectange.y);
        temp.add((float)page);
        readedPcoordinate.add(temp);
        lengths.add(text.length());
        System.out.println("text:" + text + "x:" + boundingRectange.x + "y:" + boundingRectange.y + "page:" + page);
        if (readed.contains(keyWord) ) {
            int count = 0;
            int i = lengths.size() - 1;
            for (; i >= 0; i--) {
                count += lengths.get(i);
                if (count >= keyWord.length()) {
                    break;
                }
            }
            readedPcoordinate = readedPcoordinate.subList(i, readedPcoordinate.size());
            findOutKeyWord = true;
        }

    }

}
