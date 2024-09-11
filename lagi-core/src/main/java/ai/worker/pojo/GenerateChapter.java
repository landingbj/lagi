package ai.worker.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class GenerateChapter {
    private List<String> chapterList;
    private String outline;
}
