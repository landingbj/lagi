package ai.mcps;


import ai.mcps.spec.McpSchema;

public interface SyncMcpClient extends AutoCloseable{

   String getName();

     McpSchema.InitializeResult initialize();
    /**
     * Send a roots/list_changed notification.
     */
    void rootsListChangedNotification();

   void addRoot(McpSchema.Root root);

    void removeRoot(String rootUri);

    Object ping();

    McpSchema.CallToolResult callTool(McpSchema.CallToolRequest callToolRequest);

    McpSchema.ListToolsResult listTools();


    McpSchema.ListToolsResult listTools(String cursor);

    McpSchema.ListResourcesResult listResources(String cursor) ;

    McpSchema.ListResourcesResult listResources();


    McpSchema.ReadResourceResult readResource(McpSchema.Resource resource) ;


    McpSchema.ReadResourceResult readResource(McpSchema.ReadResourceRequest readResourceRequest);


    McpSchema.ListResourceTemplatesResult listResourceTemplates(String cursor);

    McpSchema.ListResourceTemplatesResult listResourceTemplates() ;

    void subscribeResource(McpSchema.SubscribeRequest subscribeRequest);


    void unsubscribeResource(McpSchema.UnsubscribeRequest unsubscribeRequest) ;

    McpSchema.ListPromptsResult listPrompts(String cursor) ;

    McpSchema.ListPromptsResult listPrompts();


    McpSchema.GetPromptResult getPrompt(McpSchema.GetPromptRequest getPromptRequest);

    void setLoggingLevel(McpSchema.LoggingLevel loggingLevel);
}
