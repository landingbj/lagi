# 大语言模型功能调用指南

## 文本对话功能
要使用文本对话功能受限需要创建一个 CompletionsService 的实例对象。 这个对象有两个方法 completions,streamCompletions。

__completions__  

`
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest)
`  
一次性获取大模型的对话的回答结果  
参数：  
  chatCompletionRequest - 对话请求参数包含对话使用的模型,对话的上下文及一些模型参数  
返回： 一个包含大模型结果的对象,对象的 choices属性，包含着大模型的返回的回答文本
示例：
```java
CompletionsService completionsService = new CompletionsService(config);
ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
String text = result.getChoices().get(0).getMessage().getContent();
```

__streamCompletions__

`
Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest)
`  
使用流的方式返回大模型对话的结果  
参数：  
chatCompletionRequest - 对话请求参数包含对话使用的模型,对话的上下文及一些模型参数    
返回： 返回一个流的观察者对象。 可以通过这个对象获取流的返回结果, 你可以将写入到 HttpServletResponse 的输出流中
示例：
```java
HttpServletResponse resp;
CompletionsService completionsService = new CompletionsService(config);
Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
PrintWriter out = resp.getWriter();
final ChatCompletionResult[] lastResult = {null};
observable.subscribe(
        data -> {
            lastResult[0] = data;
            String msg = gson.toJson(data);
            out.print("data: " + msg + "\n\n");
            out.flush();
        },
        e -> logger.error("", e),
        () -> extracted(lastResult, indexSearchDataList, req, out)
);
```