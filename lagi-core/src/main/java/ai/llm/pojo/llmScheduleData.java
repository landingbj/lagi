package ai.llm.pojo;

import ai.common.exception.RRException;
import ai.common.pojo.IndexSearchData;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import io.reactivex.Observable;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Data
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class llmScheduleData implements Serializable {
    // latch
    private CountDownLatch latch;
    // request params
    private ChatCompletionRequest request;
    private List<IndexSearchData> indexSearchDataList;
    // results
    private ChatCompletionResult result;
    private Observable<ChatCompletionResult> streamResult;
    private RRException exception;
}

