package ai.vector.pojo;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class ExcelPage {
    private List<Integer> headerIndex;
    private List<List<JSONObject>> data;
}
