# Large Language model function Invocation Guide

## chat function
To use chat function you need to create an instance object of CompletionsService. This object has two methods completions, streamCompletions.
__completions__

`
ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest)
`  
Get the answer results of the large model conversation at once   
Parameters：  
  chatCompletionRequest - Session request parameters include the model used by the session, the context of the session, and some model parameters 
Returns： An object containing the results of the large model, the object's choices property, contains the answer text returned by the large model
Example：
```java
CompletionsService completionsService = new CompletionsService(config);
ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
String text = result.getChoices().get(0).getMessage().getContent();
```

__streamCompletions__

`
Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest)
`  
Returns the results of a large model conversation using a stream   
Parameters：  
  chatCompletionRequest - Session request parameters include the model used by the session, the context of the session, and a set of model parameters    
Returns： Session request parameters include the model used by the session, the context of the session, and a set of model parameters
Example：
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