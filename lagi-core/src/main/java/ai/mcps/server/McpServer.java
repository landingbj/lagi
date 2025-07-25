/*
 * Copyright 2024-2024 the original author or authors.
 */

package ai.mcps.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import ai.mcps.spec.McpSchema;
import ai.mcps.spec.McpSchema.CallToolResult;
import ai.mcps.spec.McpSchema.ResourceTemplate;
import ai.mcps.spec.McpServerTransportProvider;
import ai.mcps.spec.ServerMcpTransport;
import ai.mcps.util.Assert;
import lombok.var;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory class for creating Model Context Protocol (MCP) servers. MCP servers expose
 * tools, resources, and prompts to AI models through a standardized interface.
 *
 * <p>
 * This class serves as the main entry point for implementing the server-side of the MCP
 * specification. The server's responsibilities include:
 * <ul>
 * <li>Exposing tools that models can invoke to perform actions
 * <li>Providing access to resources that give models context
 * <li>Managing prompt templates for structured model interactions
 * <li>Handling client connections and requests
 * <li>Implementing capability negotiation
 * </ul>
 *
 * <p>
 * Thread Safety: Both synchronous and asynchronous server implementations are
 * thread-safe. The synchronous server processes requests sequentially, while the
 * asynchronous server can handle concurrent requests safely through its reactive
 * programming model.
 *
 * <p>
 * Error Handling: The server implementations provide robust error handling through the
 * McpError class. Errors are properly propagated to clients while maintaining the
 * server's stability. Server implementations should use appropriate error codes and
 * provide meaningful error messages to help diagnose issues.
 *
 * <p>
 * The class provides factory methods to create either:
 * <ul>
 * <li>{@link McpAsyncServer} for non-blocking operations with reactive responses
 * <li>{@link McpSyncServer} for blocking operations with direct responses
 * </ul>
 *
 * <p>
 * Example of creating a basic synchronous server: <pre>{@code
 * McpServer.sync(transportProvider)
 *     .serverInfo("my-server", "1.0.0")
 *     .tool(new Tool("calculator", "Performs calculations", schema),
 *           (exchange, args) -> new CallToolResult("Result: " + calculate(args)))
 *     .build();
 * }</pre>
 *
 * Example of creating a basic asynchronous server: <pre>{@code
 * McpServer.async(transportProvider)
 *     .serverInfo("my-server", "1.0.0")
 *     .tool(new Tool("calculator", "Performs calculations", schema),
 *           (exchange, args) -> Mono.fromSupplier(() -> calculate(args))
 *               .map(result -> new CallToolResult("Result: " + result)))
 *     .build();
 * }</pre>
 *
 * <p>
 * Example with comprehensive asynchronous configuration: <pre>{@code
 * McpServer.async(transportProvider)
 *     .serverInfo("advanced-server", "2.0.0")
 *     .capabilities(new ServerCapabilities(...))
 *     // Register tools
 *     .tools(
 *         new McpServerFeatures.AsyncToolSpecification(calculatorTool,
 *             (exchange, args) -> Mono.fromSupplier(() -> calculate(args))
 *                 .map(result -> new CallToolResult("Result: " + result))),
 *         new McpServerFeatures.AsyncToolSpecification(weatherTool,
 *             (exchange, args) -> Mono.fromSupplier(() -> getWeather(args))
 *                 .map(result -> new CallToolResult("Weather: " + result)))
 *     )
 *     // Register resources
 *     .resources(
 *         new McpServerFeatures.AsyncResourceSpecification(fileResource,
 *             (exchange, req) -> Mono.fromSupplier(() -> readFile(req))
 *                 .map(ReadResourceResult::new)),
 *         new McpServerFeatures.AsyncResourceSpecification(dbResource,
 *             (exchange, req) -> Mono.fromSupplier(() -> queryDb(req))
 *                 .map(ReadResourceResult::new))
 *     )
 *     // Add resource templates
 *     .resourceTemplates(
 *         new ResourceTemplate("file://{path}", "Access files"),
 *         new ResourceTemplate("db://{table}", "Access database")
 *     )
 *     // Register prompts
 *     .prompts(
 *         new McpServerFeatures.AsyncPromptSpecification(analysisPrompt,
 *             (exchange, req) -> Mono.fromSupplier(() -> generateAnalysisPrompt(req))
 *                 .map(GetPromptResult::new)),
 *         new McpServerFeatures.AsyncPromptRegistration(summaryPrompt,
 *             (exchange, req) -> Mono.fromSupplier(() -> generateSummaryPrompt(req))
 *                 .map(GetPromptResult::new))
 *     )
 *     .build();
 * }</pre>
 *
 * 
 * 
 * @see McpAsyncServer
 * @see McpSyncServer
 * @see McpServerTransportProvider
 */
public interface McpServer {

	/**
	 * Starts building a synchronous MCP server that provides blocking operations.
	 * Synchronous servers block the current Thread's execution upon each request before
	 * giving the control back to the caller, making them simpler to implement but
	 * potentially less scalable for concurrent operations.
	 * @param transportProvider The transport layer implementation for MCP communication.
	 * @return A new instance of {@link SyncSpecification} for configuring the server.
	 */
	static SyncSpecification sync(McpServerTransportProvider transportProvider) {
		return new SyncSpecification(transportProvider);
	}

	/**
	 * Starts building a synchronous MCP server that provides blocking operations.
	 * Synchronous servers block the current Thread's execution upon each request before
	 * giving the control back to the caller, making them simpler to implement but
	 * potentially less scalable for concurrent operations.
	 * @param transport The transport layer implementation for MCP communication
	 * @return A new instance of {@link SyncSpec} for configuring the server.
	 * @deprecated This method will be removed in 0.9.0. Use
	 * {@link #sync(McpServerTransportProvider)} instead.
	 */
	@Deprecated
	static SyncSpec sync(ServerMcpTransport transport) {
		return new SyncSpec(transport);
	}

	/**
	 * Starts building an asynchronous MCP server that provides non-blocking operations.
	 * Asynchronous servers can handle multiple requests concurrently on a single Thread
	 * using a functional paradigm with non-blocking server transports, making them more
	 * scalable for high-concurrency scenarios but more complex to implement.
	 * @param transportProvider The transport layer implementation for MCP communication.
	 * @return A new instance of {@link AsyncSpecification} for configuring the server.
	 */
	static AsyncSpecification async(McpServerTransportProvider transportProvider) {
		return new AsyncSpecification(transportProvider);
	}

	/**
	 * Starts building an asynchronous MCP server that provides non-blocking operations.
	 * Asynchronous servers can handle multiple requests concurrently on a single Thread
	 * using a functional paradigm with non-blocking server transports, making them more
	 * scalable for high-concurrency scenarios but more complex to implement.
	 * @param transport The transport layer implementation for MCP communication
	 * @return A new instance of {@link AsyncSpec} for configuring the server.
	 * @deprecated This method will be removed in 0.9.0. Use
	 * {@link #async(McpServerTransportProvider)} instead.
	 */
	@Deprecated
	static AsyncSpec async(ServerMcpTransport transport) {
		return new AsyncSpec(transport);
	}

	/**
	 * Asynchronous server specification.
	 */
	class AsyncSpecification {

		private static final McpSchema.Implementation DEFAULT_SERVER_INFO = new McpSchema.Implementation("mcp-server",
				"1.0.0");

		private final McpServerTransportProvider transportProvider;

		private ObjectMapper objectMapper;

		private McpSchema.Implementation serverInfo = DEFAULT_SERVER_INFO;

		private McpSchema.ServerCapabilities serverCapabilities;

		/**
		 * The Model Context Protocol (MCP) allows servers to expose tools that can be
		 * invoked by language models. Tools enable models to interact with external
		 * systems, such as querying databases, calling APIs, or performing computations.
		 * Each tool is uniquely identified by a name and includes metadata describing its
		 * schema.
		 */
		private final List<McpServerFeatures.AsyncToolSpecification> tools = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose resources to clients. Resources allow servers to share data that
		 * provides context to language models, such as files, database schemas, or
		 * application-specific information. Each resource is uniquely identified by a
		 * URI.
		 */
		private final Map<String, McpServerFeatures.AsyncResourceSpecification> resources = new HashMap<>();

		private final List<ResourceTemplate> resourceTemplates = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose prompt templates to clients. Prompts allow servers to provide structured
		 * messages and instructions for interacting with language models. Clients can
		 * discover available prompts, retrieve their contents, and provide arguments to
		 * customize them.
		 */
		private final Map<String, McpServerFeatures.AsyncPromptSpecification> prompts = new HashMap<>();

		private final List<BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>> rootsChangeHandlers = new ArrayList<>();

		private AsyncSpecification(McpServerTransportProvider transportProvider) {
			Assert.notNull(transportProvider, "Transport provider must not be null");
			this.transportProvider = transportProvider;
		}

		/**
		 * Sets the server implementation information that will be shared with clients
		 * during connection initialization. This helps with version compatibility,
		 * debugging, and server identification.
		 * @param serverInfo The server implementation details including name and version.
		 * Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverInfo is null
		 */
		public AsyncSpecification serverInfo(McpSchema.Implementation serverInfo) {
			Assert.notNull(serverInfo, "Server info must not be null");
			this.serverInfo = serverInfo;
			return this;
		}

		/**
		 * Sets the server implementation information using name and version strings. This
		 * is a convenience method alternative to
		 * {@link #serverInfo(McpSchema.Implementation)}.
		 * @param name The server name. Must not be null or empty.
		 * @param version The server version. Must not be null or empty.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if name or version is null or empty
		 * @see #serverInfo(McpSchema.Implementation)
		 */
		public AsyncSpecification serverInfo(String name, String version) {
			Assert.hasText(name, "Name must not be null or empty");
			Assert.hasText(version, "Version must not be null or empty");
			this.serverInfo = new McpSchema.Implementation(name, version);
			return this;
		}

		/**
		 * Sets the server capabilities that will be advertised to clients during
		 * connection initialization. Capabilities define what features the server
		 * supports, such as:
		 * <ul>
		 * <li>Tool execution
		 * <li>Resource access
		 * <li>Prompt handling
		 * </ul>
		 * @param serverCapabilities The server capabilities configuration. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverCapabilities is null
		 */
		public AsyncSpecification capabilities(McpSchema.ServerCapabilities serverCapabilities) {
			Assert.notNull(serverCapabilities, "Server capabilities must not be null");
			this.serverCapabilities = serverCapabilities;
			return this;
		}

		/**
		 * Adds a single tool with its implementation handler to the server. This is a
		 * convenience method for registering individual tools without creating a
		 * {@link McpServerFeatures.AsyncToolSpecification} explicitly.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tool(
		 *     new Tool("calculator", "Performs calculations", schema),
		 *     (exchange, args) -> Mono.fromSupplier(() -> calculate(args))
		 *         .map(result -> new CallToolResult("Result: " + result))
		 * )
		 * }</pre>
		 * @param tool The tool definition including name, description, and schema. Must
		 * not be null.
		 * @param handler The function that implements the tool's logic. Must not be null.
		 * The function's first argument is an {@link McpAsyncServerExchange} upon which
		 * the server can interact with the connected client. The second argument is the
		 * map of arguments passed to the tool.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if tool or handler is null
		 */
		public AsyncSpecification tool(McpSchema.Tool tool,
				BiFunction<McpAsyncServerExchange, Map<String, Object>, Mono<CallToolResult>> handler) {
			Assert.notNull(tool, "Tool must not be null");
			Assert.notNull(handler, "Handler must not be null");

			this.tools.add(new McpServerFeatures.AsyncToolSpecification(tool, handler));

			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using a List. This method
		 * is useful when tools are dynamically generated or loaded from a configuration
		 * source.
		 * @param toolSpecifications The list of tool specifications to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolSpecifications is null
		 * @see #tools(McpServerFeatures.AsyncToolSpecification...)
		 */
		public AsyncSpecification tools(List<McpServerFeatures.AsyncToolSpecification> toolSpecifications) {
			Assert.notNull(toolSpecifications, "Tool handlers list must not be null");
			this.tools.addAll(toolSpecifications);
			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using varargs. This
		 * method provides a convenient way to register multiple tools inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tools(
		 *     new McpServerFeatures.AsyncToolSpecification(calculatorTool, calculatorHandler),
		 *     new McpServerFeatures.AsyncToolSpecification(weatherTool, weatherHandler),
		 *     new McpServerFeatures.AsyncToolSpecification(fileManagerTool, fileManagerHandler)
		 * )
		 * }</pre>
		 * @param toolSpecifications The tool specifications to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolSpecifications is null
		 * @see #tools(List)
		 */
		public AsyncSpecification tools(McpServerFeatures.AsyncToolSpecification... toolSpecifications) {
			Assert.notNull(toolSpecifications, "Tool handlers list must not be null");
			for (McpServerFeatures.AsyncToolSpecification tool : toolSpecifications) {
				this.tools.add(tool);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a Map. This method is
		 * useful when resources are dynamically generated or loaded from a configuration
		 * source.
		 * @param resourceSpecifications Map of resource name to specification. Must not
		 * be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceSpecifications is null
		 * @see #resources(McpServerFeatures.AsyncResourceSpecification...)
		 */
		public AsyncSpecification resources(
				Map<String, McpServerFeatures.AsyncResourceSpecification> resourceSpecifications) {
			Assert.notNull(resourceSpecifications, "Resource handlers map must not be null");
			this.resources.putAll(resourceSpecifications);
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a List. This method is
		 * useful when resources need to be added in bulk from a collection.
		 * @param resourceSpecifications List of resource specifications. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceSpecifications is null
		 * @see #resources(McpServerFeatures.AsyncResourceSpecification...)
		 */
		public AsyncSpecification resources(List<McpServerFeatures.AsyncResourceSpecification> resourceSpecifications) {
			Assert.notNull(resourceSpecifications, "Resource handlers list must not be null");
			for (McpServerFeatures.AsyncResourceSpecification resource : resourceSpecifications) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using varargs. This method
		 * provides a convenient way to register multiple resources inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resources(
		 *     new McpServerFeatures.AsyncResourceSpecification(fileResource, fileHandler),
		 *     new McpServerFeatures.AsyncResourceSpecification(dbResource, dbHandler),
		 *     new McpServerFeatures.AsyncResourceSpecification(apiResource, apiHandler)
		 * )
		 * }</pre>
		 * @param resourceSpecifications The resource specifications to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceSpecifications is null
		 */
		public AsyncSpecification resources(McpServerFeatures.AsyncResourceSpecification... resourceSpecifications) {
			Assert.notNull(resourceSpecifications, "Resource handlers list must not be null");
			for (McpServerFeatures.AsyncResourceSpecification resource : resourceSpecifications) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Sets the resource templates that define patterns for dynamic resource access.
		 * Templates use URI patterns with placeholders that can be filled at runtime.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resourceTemplates(
		 *     new ResourceTemplate("file://{path}", "Access files by path"),
		 *     new ResourceTemplate("db://{table}/{id}", "Access database records")
		 * )
		 * }</pre>
		 * @param resourceTemplates List of resource templates. If null, clears existing
		 * templates.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceTemplates is null.
		 * @see #resourceTemplates(ResourceTemplate...)
		 */
		public AsyncSpecification resourceTemplates(List<ResourceTemplate> resourceTemplates) {
			Assert.notNull(resourceTemplates, "Resource templates must not be null");
			this.resourceTemplates.addAll(resourceTemplates);
			return this;
		}

		/**
		 * Sets the resource templates using varargs for convenience. This is an
		 * alternative to {@link #resourceTemplates(List)}.
		 * @param resourceTemplates The resource templates to set.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceTemplates is null.
		 * @see #resourceTemplates(List)
		 */
		public AsyncSpecification resourceTemplates(ResourceTemplate... resourceTemplates) {
			Assert.notNull(resourceTemplates, "Resource templates must not be null");
			for (ResourceTemplate resourceTemplate : resourceTemplates) {
				this.resourceTemplates.add(resourceTemplate);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a Map. This method is
		 * useful when prompts are dynamically generated or loaded from a configuration
		 * source.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .prompts(Map.of("analysis", new McpServerFeatures.AsyncPromptSpecification(
		 *     new Prompt("analysis", "Code analysis template"),
		 *     request -> Mono.fromSupplier(() -> generateAnalysisPrompt(request))
		 *         .map(GetPromptResult::new)
		 * )));
		 * }</pre>
		 * @param prompts Map of prompt name to specification. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public AsyncSpecification prompts(Map<String, McpServerFeatures.AsyncPromptSpecification> prompts) {
			Assert.notNull(prompts, "Prompts map must not be null");
			this.prompts.putAll(prompts);
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a List. This method is
		 * useful when prompts need to be added in bulk from a collection.
		 * @param prompts List of prompt specifications. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 * @see #prompts(McpServerFeatures.AsyncPromptSpecification...)
		 */
		public AsyncSpecification prompts(List<McpServerFeatures.AsyncPromptSpecification> prompts) {
			Assert.notNull(prompts, "Prompts list must not be null");
			for (McpServerFeatures.AsyncPromptSpecification prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using varargs. This method
		 * provides a convenient way to register multiple prompts inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .prompts(
		 *     new McpServerFeatures.AsyncPromptSpecification(analysisPrompt, analysisHandler),
		 *     new McpServerFeatures.AsyncPromptSpecification(summaryPrompt, summaryHandler),
		 *     new McpServerFeatures.AsyncPromptSpecification(reviewPrompt, reviewHandler)
		 * )
		 * }</pre>
		 * @param prompts The prompt specifications to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public AsyncSpecification prompts(McpServerFeatures.AsyncPromptSpecification... prompts) {
			Assert.notNull(prompts, "Prompts list must not be null");
			for (McpServerFeatures.AsyncPromptSpecification prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers a consumer that will be notified when the list of roots changes. This
		 * is useful for updating resource availability dynamically, such as when new
		 * files are added or removed.
		 * @param handler The handler to register. Must not be null. The function's first
		 * argument is an {@link McpAsyncServerExchange} upon which the server can
		 * interact with the connected client. The second argument is the list of roots.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumer is null
		 */
		public AsyncSpecification rootsChangeHandler(
				BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>> handler) {
			Assert.notNull(handler, "Consumer must not be null");
			this.rootsChangeHandlers.add(handler);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes. This method is useful when multiple consumers need to be registered at
		 * once.
		 * @param handlers The list of handlers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 * @see #rootsChangeHandler(BiFunction)
		 */
		public AsyncSpecification rootsChangeHandlers(
				List<BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>> handlers) {
			Assert.notNull(handlers, "Handlers list must not be null");
			this.rootsChangeHandlers.addAll(handlers);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes using varargs. This method provides a convenient way to register
		 * multiple consumers inline.
		 * @param handlers The handlers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 * @see #rootsChangeHandlers(List)
		 */
		public AsyncSpecification rootsChangeHandlers(
				@SuppressWarnings("unchecked") BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>... handlers) {
			Assert.notNull(handlers, "Handlers list must not be null");
			return this.rootsChangeHandlers(Arrays.asList(handlers));
		}

		/**
		 * Sets the object mapper to use for serializing and deserializing JSON messages.
		 * @param objectMapper the instance to use. Must not be null.
		 * @return This builder instance for method chaining.
		 * @throws IllegalArgumentException if objectMapper is null
		 */
		public AsyncSpecification objectMapper(ObjectMapper objectMapper) {
			Assert.notNull(objectMapper, "ObjectMapper must not be null");
			this.objectMapper = objectMapper;
			return this;
		}

		/**
		 * Builds an asynchronous MCP server that provides non-blocking operations.
		 * @return A new instance of {@link McpAsyncServer} configured with this builder's
		 * settings.
		 */
		public McpAsyncServer build() {
			var features = new McpServerFeatures.Async(this.serverInfo, this.serverCapabilities, this.tools,
					this.resources, this.resourceTemplates, this.prompts, this.rootsChangeHandlers);
			var mapper = this.objectMapper != null ? this.objectMapper : new ObjectMapper();
			return new McpAsyncServer(this.transportProvider, mapper, features);
		}

	}

	/**
	 * Synchronous server specification.
	 */
	class SyncSpecification {

		private static final McpSchema.Implementation DEFAULT_SERVER_INFO = new McpSchema.Implementation("mcp-server",
				"1.0.0");

		private final McpServerTransportProvider transportProvider;

		private ObjectMapper objectMapper;

		private McpSchema.Implementation serverInfo = DEFAULT_SERVER_INFO;

		private McpSchema.ServerCapabilities serverCapabilities;

		/**
		 * The Model Context Protocol (MCP) allows servers to expose tools that can be
		 * invoked by language models. Tools enable models to interact with external
		 * systems, such as querying databases, calling APIs, or performing computations.
		 * Each tool is uniquely identified by a name and includes metadata describing its
		 * schema.
		 */
		private final List<McpServerFeatures.SyncToolSpecification> tools = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose resources to clients. Resources allow servers to share data that
		 * provides context to language models, such as files, database schemas, or
		 * application-specific information. Each resource is uniquely identified by a
		 * URI.
		 */
		private final Map<String, McpServerFeatures.SyncResourceSpecification> resources = new HashMap<>();

		private final List<ResourceTemplate> resourceTemplates = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose prompt templates to clients. Prompts allow servers to provide structured
		 * messages and instructions for interacting with language models. Clients can
		 * discover available prompts, retrieve their contents, and provide arguments to
		 * customize them.
		 */
		private final Map<String, McpServerFeatures.SyncPromptSpecification> prompts = new HashMap<>();

		private final List<BiConsumer<McpSyncServerExchange, List<McpSchema.Root>>> rootsChangeHandlers = new ArrayList<>();

		private SyncSpecification(McpServerTransportProvider transportProvider) {
			Assert.notNull(transportProvider, "Transport provider must not be null");
			this.transportProvider = transportProvider;
		}

		/**
		 * Sets the server implementation information that will be shared with clients
		 * during connection initialization. This helps with version compatibility,
		 * debugging, and server identification.
		 * @param serverInfo The server implementation details including name and version.
		 * Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverInfo is null
		 */
		public SyncSpecification serverInfo(McpSchema.Implementation serverInfo) {
			Assert.notNull(serverInfo, "Server info must not be null");
			this.serverInfo = serverInfo;
			return this;
		}

		/**
		 * Sets the server implementation information using name and version strings. This
		 * is a convenience method alternative to
		 * {@link #serverInfo(McpSchema.Implementation)}.
		 * @param name The server name. Must not be null or empty.
		 * @param version The server version. Must not be null or empty.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if name or version is null or empty
		 * @see #serverInfo(McpSchema.Implementation)
		 */
		public SyncSpecification serverInfo(String name, String version) {
			Assert.hasText(name, "Name must not be null or empty");
			Assert.hasText(version, "Version must not be null or empty");
			this.serverInfo = new McpSchema.Implementation(name, version);
			return this;
		}

		/**
		 * Sets the server capabilities that will be advertised to clients during
		 * connection initialization. Capabilities define what features the server
		 * supports, such as:
		 * <ul>
		 * <li>Tool execution
		 * <li>Resource access
		 * <li>Prompt handling
		 * </ul>
		 * @param serverCapabilities The server capabilities configuration. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverCapabilities is null
		 */
		public SyncSpecification capabilities(McpSchema.ServerCapabilities serverCapabilities) {
			Assert.notNull(serverCapabilities, "Server capabilities must not be null");
			this.serverCapabilities = serverCapabilities;
			return this;
		}

		/**
		 * Adds a single tool with its implementation handler to the server. This is a
		 * convenience method for registering individual tools without creating a
		 * {@link McpServerFeatures.SyncToolSpecification} explicitly.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tool(
		 *     new Tool("calculator", "Performs calculations", schema),
		 *     (exchange, args) -> new CallToolResult("Result: " + calculate(args))
		 * )
		 * }</pre>
		 * @param tool The tool definition including name, description, and schema. Must
		 * not be null.
		 * @param handler The function that implements the tool's logic. Must not be null.
		 * The function's first argument is an {@link McpSyncServerExchange} upon which
		 * the server can interact with the connected client. The second argument is the
		 * list of arguments passed to the tool.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if tool or handler is null
		 */
		public SyncSpecification tool(McpSchema.Tool tool,
				BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> handler) {
			Assert.notNull(tool, "Tool must not be null");
			Assert.notNull(handler, "Handler must not be null");

			this.tools.add(new McpServerFeatures.SyncToolSpecification(tool, handler));

			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using a List. This method
		 * is useful when tools are dynamically generated or loaded from a configuration
		 * source.
		 * @param toolSpecifications The list of tool specifications to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolSpecifications is null
		 * @see #tools(McpServerFeatures.SyncToolSpecification...)
		 */
		public SyncSpecification tools(List<McpServerFeatures.SyncToolSpecification> toolSpecifications) {
			Assert.notNull(toolSpecifications, "Tool handlers list must not be null");
			this.tools.addAll(toolSpecifications);
			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using varargs. This
		 * method provides a convenient way to register multiple tools inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tools(
		 *     new ToolSpecification(calculatorTool, calculatorHandler),
		 *     new ToolSpecification(weatherTool, weatherHandler),
		 *     new ToolSpecification(fileManagerTool, fileManagerHandler)
		 * )
		 * }</pre>
		 * @param toolSpecifications The tool specifications to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolSpecifications is null
		 * @see #tools(List)
		 */
		public SyncSpecification tools(McpServerFeatures.SyncToolSpecification... toolSpecifications) {
			Assert.notNull(toolSpecifications, "Tool handlers list must not be null");
			for (McpServerFeatures.SyncToolSpecification tool : toolSpecifications) {
				this.tools.add(tool);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a Map. This method is
		 * useful when resources are dynamically generated or loaded from a configuration
		 * source.
		 * @param resourceSpecifications Map of resource name to specification. Must not
		 * be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceSpecifications is null
		 * @see #resources(McpServerFeatures.SyncResourceSpecification...)
		 */
		public SyncSpecification resources(
				Map<String, McpServerFeatures.SyncResourceSpecification> resourceSpecifications) {
			Assert.notNull(resourceSpecifications, "Resource handlers map must not be null");
			this.resources.putAll(resourceSpecifications);
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a List. This method is
		 * useful when resources need to be added in bulk from a collection.
		 * @param resourceSpecifications List of resource specifications. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceSpecifications is null
		 * @see #resources(McpServerFeatures.SyncResourceSpecification...)
		 */
		public SyncSpecification resources(List<McpServerFeatures.SyncResourceSpecification> resourceSpecifications) {
			Assert.notNull(resourceSpecifications, "Resource handlers list must not be null");
			for (McpServerFeatures.SyncResourceSpecification resource : resourceSpecifications) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using varargs. This method
		 * provides a convenient way to register multiple resources inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resources(
		 *     new ResourceSpecification(fileResource, fileHandler),
		 *     new ResourceSpecification(dbResource, dbHandler),
		 *     new ResourceSpecification(apiResource, apiHandler)
		 * )
		 * }</pre>
		 * @param resourceSpecifications The resource specifications to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceSpecifications is null
		 */
		public SyncSpecification resources(McpServerFeatures.SyncResourceSpecification... resourceSpecifications) {
			Assert.notNull(resourceSpecifications, "Resource handlers list must not be null");
			for (McpServerFeatures.SyncResourceSpecification resource : resourceSpecifications) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Sets the resource templates that define patterns for dynamic resource access.
		 * Templates use URI patterns with placeholders that can be filled at runtime.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resourceTemplates(
		 *     new ResourceTemplate("file://{path}", "Access files by path"),
		 *     new ResourceTemplate("db://{table}/{id}", "Access database records")
		 * )
		 * }</pre>
		 * @param resourceTemplates List of resource templates. If null, clears existing
		 * templates.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceTemplates is null.
		 * @see #resourceTemplates(ResourceTemplate...)
		 */
		public SyncSpecification resourceTemplates(List<ResourceTemplate> resourceTemplates) {
			Assert.notNull(resourceTemplates, "Resource templates must not be null");
			this.resourceTemplates.addAll(resourceTemplates);
			return this;
		}

		/**
		 * Sets the resource templates using varargs for convenience. This is an
		 * alternative to {@link #resourceTemplates(List)}.
		 * @param resourceTemplates The resource templates to set.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceTemplates is null
		 * @see #resourceTemplates(List)
		 */
		public SyncSpecification resourceTemplates(ResourceTemplate... resourceTemplates) {
			Assert.notNull(resourceTemplates, "Resource templates must not be null");
			for (ResourceTemplate resourceTemplate : resourceTemplates) {
				this.resourceTemplates.add(resourceTemplate);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a Map. This method is
		 * useful when prompts are dynamically generated or loaded from a configuration
		 * source.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * Map<String, PromptSpecification> prompts = new HashMap<>();
		 * prompts.put("analysis", new PromptSpecification(
		 *     new Prompt("analysis", "Code analysis template"),
		 *     (exchange, request) -> new GetPromptResult(generateAnalysisPrompt(request))
		 * ));
		 * .prompts(prompts)
		 * }</pre>
		 * @param prompts Map of prompt name to specification. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public SyncSpecification prompts(Map<String, McpServerFeatures.SyncPromptSpecification> prompts) {
			Assert.notNull(prompts, "Prompts map must not be null");
			this.prompts.putAll(prompts);
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a List. This method is
		 * useful when prompts need to be added in bulk from a collection.
		 * @param prompts List of prompt specifications. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 * @see #prompts(McpServerFeatures.SyncPromptSpecification...)
		 */
		public SyncSpecification prompts(List<McpServerFeatures.SyncPromptSpecification> prompts) {
			Assert.notNull(prompts, "Prompts list must not be null");
			for (McpServerFeatures.SyncPromptSpecification prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using varargs. This method
		 * provides a convenient way to register multiple prompts inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .prompts(
		 *     new PromptSpecification(analysisPrompt, analysisHandler),
		 *     new PromptSpecification(summaryPrompt, summaryHandler),
		 *     new PromptSpecification(reviewPrompt, reviewHandler)
		 * )
		 * }</pre>
		 * @param prompts The prompt specifications to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public SyncSpecification prompts(McpServerFeatures.SyncPromptSpecification... prompts) {
			Assert.notNull(prompts, "Prompts list must not be null");
			for (McpServerFeatures.SyncPromptSpecification prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers a consumer that will be notified when the list of roots changes. This
		 * is useful for updating resource availability dynamically, such as when new
		 * files are added or removed.
		 * @param handler The handler to register. Must not be null. The function's first
		 * argument is an {@link McpSyncServerExchange} upon which the server can interact
		 * with the connected client. The second argument is the list of roots.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumer is null
		 */
		public SyncSpecification rootsChangeHandler(BiConsumer<McpSyncServerExchange, List<McpSchema.Root>> handler) {
			Assert.notNull(handler, "Consumer must not be null");
			this.rootsChangeHandlers.add(handler);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes. This method is useful when multiple consumers need to be registered at
		 * once.
		 * @param handlers The list of handlers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 * @see #rootsChangeHandler(BiConsumer)
		 */
		public SyncSpecification rootsChangeHandlers(
				List<BiConsumer<McpSyncServerExchange, List<McpSchema.Root>>> handlers) {
			Assert.notNull(handlers, "Handlers list must not be null");
			this.rootsChangeHandlers.addAll(handlers);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes using varargs. This method provides a convenient way to register
		 * multiple consumers inline.
		 * @param handlers The handlers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 * @see #rootsChangeHandlers(List)
		 */
		public SyncSpecification rootsChangeHandlers(
				BiConsumer<McpSyncServerExchange, List<McpSchema.Root>>... handlers) {
			Assert.notNull(handlers, "Handlers list must not be null");
			return this.rootsChangeHandlers(Arrays.asList(handlers));
		}

		/**
		 * Sets the object mapper to use for serializing and deserializing JSON messages.
		 * @param objectMapper the instance to use. Must not be null.
		 * @return This builder instance for method chaining.
		 * @throws IllegalArgumentException if objectMapper is null
		 */
		public SyncSpecification objectMapper(ObjectMapper objectMapper) {
			Assert.notNull(objectMapper, "ObjectMapper must not be null");
			this.objectMapper = objectMapper;
			return this;
		}

		/**
		 * Builds a synchronous MCP server that provides blocking operations.
		 * @return A new instance of {@link McpSyncServer} configured with this builder's
		 * settings.
		 */
		public McpSyncServer build() {
			McpServerFeatures.Sync syncFeatures = new McpServerFeatures.Sync(this.serverInfo, this.serverCapabilities,
					this.tools, this.resources, this.resourceTemplates, this.prompts, this.rootsChangeHandlers);
			McpServerFeatures.Async asyncFeatures = McpServerFeatures.Async.fromSync(syncFeatures);
			var mapper = this.objectMapper != null ? this.objectMapper : new ObjectMapper();
			var asyncServer = new McpAsyncServer(this.transportProvider, mapper, asyncFeatures);

			return new McpSyncServer(asyncServer);
		}

	}

	/**
	 * Asynchronous server specification.
	 *
	 * @deprecated
	 */
	@Deprecated
	class AsyncSpec {

		private static final McpSchema.Implementation DEFAULT_SERVER_INFO = new McpSchema.Implementation("mcp-server",
				"1.0.0");

		private final ServerMcpTransport transport;

		private ObjectMapper objectMapper;

		private McpSchema.Implementation serverInfo = DEFAULT_SERVER_INFO;

		private McpSchema.ServerCapabilities serverCapabilities;

		/**
		 * The Model Context Protocol (MCP) allows servers to expose tools that can be
		 * invoked by language models. Tools enable models to interact with external
		 * systems, such as querying databases, calling APIs, or performing computations.
		 * Each tool is uniquely identified by a name and includes metadata describing its
		 * schema.
		 */
		private final List<McpServerFeatures.AsyncToolRegistration> tools = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose resources to clients. Resources allow servers to share data that
		 * provides context to language models, such as files, database schemas, or
		 * application-specific information. Each resource is uniquely identified by a
		 * URI.
		 */
		private final Map<String, McpServerFeatures.AsyncResourceRegistration> resources = new HashMap<>();

		private final List<ResourceTemplate> resourceTemplates = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose prompt templates to clients. Prompts allow servers to provide structured
		 * messages and instructions for interacting with language models. Clients can
		 * discover available prompts, retrieve their contents, and provide arguments to
		 * customize them.
		 */
		private final Map<String, McpServerFeatures.AsyncPromptRegistration> prompts = new HashMap<>();

		private final List<Function<List<McpSchema.Root>, Mono<Void>>> rootsChangeConsumers = new ArrayList<>();

		private AsyncSpec(ServerMcpTransport transport) {
			Assert.notNull(transport, "Transport must not be null");
			this.transport = transport;
		}

		/**
		 * Sets the server implementation information that will be shared with clients
		 * during connection initialization. This helps with version compatibility,
		 * debugging, and server identification.
		 * @param serverInfo The server implementation details including name and version.
		 * Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverInfo is null
		 */
		public AsyncSpec serverInfo(McpSchema.Implementation serverInfo) {
			Assert.notNull(serverInfo, "Server info must not be null");
			this.serverInfo = serverInfo;
			return this;
		}

		/**
		 * Sets the server implementation information using name and version strings. This
		 * is a convenience method alternative to
		 * {@link #serverInfo(McpSchema.Implementation)}.
		 * @param name The server name. Must not be null or empty.
		 * @param version The server version. Must not be null or empty.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if name or version is null or empty
		 * @see #serverInfo(McpSchema.Implementation)
		 */
		public AsyncSpec serverInfo(String name, String version) {
			Assert.hasText(name, "Name must not be null or empty");
			Assert.hasText(version, "Version must not be null or empty");
			this.serverInfo = new McpSchema.Implementation(name, version);
			return this;
		}

		/**
		 * Sets the server capabilities that will be advertised to clients during
		 * connection initialization. Capabilities define what features the server
		 * supports, such as:
		 * <ul>
		 * <li>Tool execution
		 * <li>Resource access
		 * <li>Prompt handling
		 * <li>Streaming responses
		 * <li>Batch operations
		 * </ul>
		 * @param serverCapabilities The server capabilities configuration. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverCapabilities is null
		 */
		public AsyncSpec capabilities(McpSchema.ServerCapabilities serverCapabilities) {
			this.serverCapabilities = serverCapabilities;
			return this;
		}

		/**
		 * Adds a single tool with its implementation handler to the server. This is a
		 * convenience method for registering individual tools without creating a
		 * {@link McpServerFeatures.AsyncToolRegistration} explicitly.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tool(
		 *     new Tool("calculator", "Performs calculations", schema),
		 *     args -> Mono.just(new CallToolResult("Result: " + calculate(args)))
		 * )
		 * }</pre>
		 * @param tool The tool definition including name, description, and schema. Must
		 * not be null.
		 * @param handler The function that implements the tool's logic. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if tool or handler is null
		 */
		public AsyncSpec tool(McpSchema.Tool tool, Function<Map<String, Object>, Mono<CallToolResult>> handler) {
			Assert.notNull(tool, "Tool must not be null");
			Assert.notNull(handler, "Handler must not be null");

			this.tools.add(new McpServerFeatures.AsyncToolRegistration(tool, handler));

			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using a List. This method
		 * is useful when tools are dynamically generated or loaded from a configuration
		 * source.
		 * @param toolRegistrations The list of tool registrations to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolRegistrations is null
		 * @see #tools(McpServerFeatures.AsyncToolRegistration...)
		 */
		public AsyncSpec tools(List<McpServerFeatures.AsyncToolRegistration> toolRegistrations) {
			Assert.notNull(toolRegistrations, "Tool handlers list must not be null");
			this.tools.addAll(toolRegistrations);
			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using varargs. This
		 * method provides a convenient way to register multiple tools inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tools(
		 *     new McpServerFeatures.AsyncToolRegistration(calculatorTool, calculatorHandler),
		 *     new McpServerFeatures.AsyncToolRegistration(weatherTool, weatherHandler),
		 *     new McpServerFeatures.AsyncToolRegistration(fileManagerTool, fileManagerHandler)
		 * )
		 * }</pre>
		 * @param toolRegistrations The tool registrations to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolRegistrations is null
		 * @see #tools(List)
		 */
		public AsyncSpec tools(McpServerFeatures.AsyncToolRegistration... toolRegistrations) {
			for (McpServerFeatures.AsyncToolRegistration tool : toolRegistrations) {
				this.tools.add(tool);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a Map. This method is
		 * useful when resources are dynamically generated or loaded from a configuration
		 * source.
		 * @param resourceRegsitrations Map of resource name to registration. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceRegsitrations is null
		 * @see #resources(McpServerFeatures.AsyncResourceRegistration...)
		 */
		public AsyncSpec resources(Map<String, McpServerFeatures.AsyncResourceRegistration> resourceRegsitrations) {
			Assert.notNull(resourceRegsitrations, "Resource handlers map must not be null");
			this.resources.putAll(resourceRegsitrations);
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a List. This method is
		 * useful when resources need to be added in bulk from a collection.
		 * @param resourceRegsitrations List of resource registrations. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceRegsitrations is null
		 * @see #resources(McpServerFeatures.AsyncResourceRegistration...)
		 */
		public AsyncSpec resources(List<McpServerFeatures.AsyncResourceRegistration> resourceRegsitrations) {
			Assert.notNull(resourceRegsitrations, "Resource handlers list must not be null");
			for (McpServerFeatures.AsyncResourceRegistration resource : resourceRegsitrations) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using varargs. This method
		 * provides a convenient way to register multiple resources inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resources(
		 *     new McpServerFeatures.AsyncResourceRegistration(fileResource, fileHandler),
		 *     new McpServerFeatures.AsyncResourceRegistration(dbResource, dbHandler),
		 *     new McpServerFeatures.AsyncResourceRegistration(apiResource, apiHandler)
		 * )
		 * }</pre>
		 * @param resourceRegistrations The resource registrations to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceRegistrations is null
		 */
		public AsyncSpec resources(McpServerFeatures.AsyncResourceRegistration... resourceRegistrations) {
			Assert.notNull(resourceRegistrations, "Resource handlers list must not be null");
			for (McpServerFeatures.AsyncResourceRegistration resource : resourceRegistrations) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Sets the resource templates that define patterns for dynamic resource access.
		 * Templates use URI patterns with placeholders that can be filled at runtime.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resourceTemplates(
		 *     new ResourceTemplate("file://{path}", "Access files by path"),
		 *     new ResourceTemplate("db://{table}/{id}", "Access database records")
		 * )
		 * }</pre>
		 * @param resourceTemplates List of resource templates. If null, clears existing
		 * templates.
		 * @return This builder instance for method chaining
		 * @see #resourceTemplates(ResourceTemplate...)
		 */
		public AsyncSpec resourceTemplates(List<ResourceTemplate> resourceTemplates) {
			this.resourceTemplates.addAll(resourceTemplates);
			return this;
		}

		/**
		 * Sets the resource templates using varargs for convenience. This is an
		 * alternative to {@link #resourceTemplates(List)}.
		 * @param resourceTemplates The resource templates to set.
		 * @return This builder instance for method chaining
		 * @see #resourceTemplates(List)
		 */
		public AsyncSpec resourceTemplates(ResourceTemplate... resourceTemplates) {
			for (ResourceTemplate resourceTemplate : resourceTemplates) {
				this.resourceTemplates.add(resourceTemplate);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a Map. This method is
		 * useful when prompts are dynamically generated or loaded from a configuration
		 * source.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .prompts(Map.of("analysis", new McpServerFeatures.AsyncPromptRegistration(
		 *     new Prompt("analysis", "Code analysis template"),
		 *     request -> Mono.just(new GetPromptResult(generateAnalysisPrompt(request)))
		 * )));
		 * }</pre>
		 * @param prompts Map of prompt name to registration. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public AsyncSpec prompts(Map<String, McpServerFeatures.AsyncPromptRegistration> prompts) {
			this.prompts.putAll(prompts);
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a List. This method is
		 * useful when prompts need to be added in bulk from a collection.
		 * @param prompts List of prompt registrations. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 * @see #prompts(McpServerFeatures.AsyncPromptRegistration...)
		 */
		public AsyncSpec prompts(List<McpServerFeatures.AsyncPromptRegistration> prompts) {
			for (McpServerFeatures.AsyncPromptRegistration prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using varargs. This method
		 * provides a convenient way to register multiple prompts inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .prompts(
		 *     new McpServerFeatures.AsyncPromptRegistration(analysisPrompt, analysisHandler),
		 *     new McpServerFeatures.AsyncPromptRegistration(summaryPrompt, summaryHandler),
		 *     new McpServerFeatures.AsyncPromptRegistration(reviewPrompt, reviewHandler)
		 * )
		 * }</pre>
		 * @param prompts The prompt registrations to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public AsyncSpec prompts(McpServerFeatures.AsyncPromptRegistration... prompts) {
			for (McpServerFeatures.AsyncPromptRegistration prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers a consumer that will be notified when the list of roots changes. This
		 * is useful for updating resource availability dynamically, such as when new
		 * files are added or removed.
		 * @param consumer The consumer to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumer is null
		 */
		public AsyncSpec rootsChangeConsumer(Function<List<McpSchema.Root>, Mono<Void>> consumer) {
			Assert.notNull(consumer, "Consumer must not be null");
			this.rootsChangeConsumers.add(consumer);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes. This method is useful when multiple consumers need to be registered at
		 * once.
		 * @param consumers The list of consumers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 */
		public AsyncSpec rootsChangeConsumers(List<Function<List<McpSchema.Root>, Mono<Void>>> consumers) {
			Assert.notNull(consumers, "Consumers list must not be null");
			this.rootsChangeConsumers.addAll(consumers);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes using varargs. This method provides a convenient way to register
		 * multiple consumers inline.
		 * @param consumers The consumers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 */
		public AsyncSpec rootsChangeConsumers(
				@SuppressWarnings("unchecked") Function<List<McpSchema.Root>, Mono<Void>>... consumers) {
			for (Function<List<McpSchema.Root>, Mono<Void>> consumer : consumers) {
				this.rootsChangeConsumers.add(consumer);
			}
			return this;
		}

		/**
		 * Builds an asynchronous MCP server that provides non-blocking operations.
		 * @return A new instance of {@link McpAsyncServer} configured with this builder's
		 * settings
		 */
		public McpAsyncServer build() {
			var tools = this.tools.stream().map(McpServerFeatures.AsyncToolRegistration::toSpecification).collect(Collectors.toList());

			var resources = this.resources.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().toSpecification()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			var prompts = this.prompts.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().toSpecification()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			var rootsChangeHandlers = this.rootsChangeConsumers.stream()
				.map(consumer -> (BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>) (exchange,
						roots) -> consumer.apply(roots))
					.collect(Collectors.toList());

			var features = new McpServerFeatures.Async(this.serverInfo, this.serverCapabilities, tools, resources,
					this.resourceTemplates, prompts, rootsChangeHandlers);

			return new McpAsyncServer(this.transport, features);
		}

	}

	/**
	 * Synchronous server specification.
	 *
	 * @deprecated
	 */
	@Deprecated
	class SyncSpec {

		private static final McpSchema.Implementation DEFAULT_SERVER_INFO = new McpSchema.Implementation("mcp-server",
				"1.0.0");

		private final ServerMcpTransport transport;

		private final McpServerTransportProvider transportProvider;

		private ObjectMapper objectMapper;

		private McpSchema.Implementation serverInfo = DEFAULT_SERVER_INFO;

		private McpSchema.ServerCapabilities serverCapabilities;

		/**
		 * The Model Context Protocol (MCP) allows servers to expose tools that can be
		 * invoked by language models. Tools enable models to interact with external
		 * systems, such as querying databases, calling APIs, or performing computations.
		 * Each tool is uniquely identified by a name and includes metadata describing its
		 * schema.
		 */
		private final List<McpServerFeatures.SyncToolRegistration> tools = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose resources to clients. Resources allow servers to share data that
		 * provides context to language models, such as files, database schemas, or
		 * application-specific information. Each resource is uniquely identified by a
		 * URI.
		 */
		private final Map<String, McpServerFeatures.SyncResourceRegistration> resources = new HashMap<>();

		private final List<ResourceTemplate> resourceTemplates = new ArrayList<>();

		/**
		 * The Model Context Protocol (MCP) provides a standardized way for servers to
		 * expose prompt templates to clients. Prompts allow servers to provide structured
		 * messages and instructions for interacting with language models. Clients can
		 * discover available prompts, retrieve their contents, and provide arguments to
		 * customize them.
		 */
		private final Map<String, McpServerFeatures.SyncPromptRegistration> prompts = new HashMap<>();

		private final List<Consumer<List<McpSchema.Root>>> rootsChangeConsumers = new ArrayList<>();

		private SyncSpec(McpServerTransportProvider transportProvider) {
			Assert.notNull(transportProvider, "Transport provider must not be null");
			this.transportProvider = transportProvider;
			this.transport = null;
		}

		private SyncSpec(ServerMcpTransport transport) {
			Assert.notNull(transport, "Transport must not be null");
			this.transport = transport;
			this.transportProvider = null;
		}

		/**
		 * Sets the server implementation information that will be shared with clients
		 * during connection initialization. This helps with version compatibility,
		 * debugging, and server identification.
		 * @param serverInfo The server implementation details including name and version.
		 * Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverInfo is null
		 */
		public SyncSpec serverInfo(McpSchema.Implementation serverInfo) {
			Assert.notNull(serverInfo, "Server info must not be null");
			this.serverInfo = serverInfo;
			return this;
		}

		/**
		 * Sets the server implementation information using name and version strings. This
		 * is a convenience method alternative to
		 * {@link #serverInfo(McpSchema.Implementation)}.
		 * @param name The server name. Must not be null or empty.
		 * @param version The server version. Must not be null or empty.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if name or version is null or empty
		 * @see #serverInfo(McpSchema.Implementation)
		 */
		public SyncSpec serverInfo(String name, String version) {
			Assert.hasText(name, "Name must not be null or empty");
			Assert.hasText(version, "Version must not be null or empty");
			this.serverInfo = new McpSchema.Implementation(name, version);
			return this;
		}

		/**
		 * Sets the server capabilities that will be advertised to clients during
		 * connection initialization. Capabilities define what features the server
		 * supports, such as:
		 * <ul>
		 * <li>Tool execution
		 * <li>Resource access
		 * <li>Prompt handling
		 * <li>Streaming responses
		 * <li>Batch operations
		 * </ul>
		 * @param serverCapabilities The server capabilities configuration. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if serverCapabilities is null
		 */
		public SyncSpec capabilities(McpSchema.ServerCapabilities serverCapabilities) {
			this.serverCapabilities = serverCapabilities;
			return this;
		}

		/**
		 * Adds a single tool with its implementation handler to the server. This is a
		 * convenience method for registering individual tools without creating a
		 * {@link McpServerFeatures.SyncToolRegistration} explicitly.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tool(
		 *     new Tool("calculator", "Performs calculations", schema),
		 *     args -> new CallToolResult("Result: " + calculate(args))
		 * )
		 * }</pre>
		 * @param tool The tool definition including name, description, and schema. Must
		 * not be null.
		 * @param handler The function that implements the tool's logic. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if tool or handler is null
		 */
		public SyncSpec tool(McpSchema.Tool tool, Function<Map<String, Object>, McpSchema.CallToolResult> handler) {
			Assert.notNull(tool, "Tool must not be null");
			Assert.notNull(handler, "Handler must not be null");

			this.tools.add(new McpServerFeatures.SyncToolRegistration(tool, handler));

			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using a List. This method
		 * is useful when tools are dynamically generated or loaded from a configuration
		 * source.
		 * @param toolRegistrations The list of tool registrations to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolRegistrations is null
		 * @see #tools(McpServerFeatures.SyncToolRegistration...)
		 */
		public SyncSpec tools(List<McpServerFeatures.SyncToolRegistration> toolRegistrations) {
			Assert.notNull(toolRegistrations, "Tool handlers list must not be null");
			this.tools.addAll(toolRegistrations);
			return this;
		}

		/**
		 * Adds multiple tools with their handlers to the server using varargs. This
		 * method provides a convenient way to register multiple tools inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .tools(
		 *     new ToolRegistration(calculatorTool, calculatorHandler),
		 *     new ToolRegistration(weatherTool, weatherHandler),
		 *     new ToolRegistration(fileManagerTool, fileManagerHandler)
		 * )
		 * }</pre>
		 * @param toolRegistrations The tool registrations to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if toolRegistrations is null
		 * @see #tools(List)
		 */
		public SyncSpec tools(McpServerFeatures.SyncToolRegistration... toolRegistrations) {
			for (McpServerFeatures.SyncToolRegistration tool : toolRegistrations) {
				this.tools.add(tool);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a Map. This method is
		 * useful when resources are dynamically generated or loaded from a configuration
		 * source.
		 * @param resourceRegsitrations Map of resource name to registration. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceRegsitrations is null
		 * @see #resources(McpServerFeatures.SyncResourceRegistration...)
		 */
		public SyncSpec resources(Map<String, McpServerFeatures.SyncResourceRegistration> resourceRegsitrations) {
			Assert.notNull(resourceRegsitrations, "Resource handlers map must not be null");
			this.resources.putAll(resourceRegsitrations);
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using a List. This method is
		 * useful when resources need to be added in bulk from a collection.
		 * @param resourceRegsitrations List of resource registrations. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceRegsitrations is null
		 * @see #resources(McpServerFeatures.SyncResourceRegistration...)
		 */
		public SyncSpec resources(List<McpServerFeatures.SyncResourceRegistration> resourceRegsitrations) {
			Assert.notNull(resourceRegsitrations, "Resource handlers list must not be null");
			for (McpServerFeatures.SyncResourceRegistration resource : resourceRegsitrations) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Registers multiple resources with their handlers using varargs. This method
		 * provides a convenient way to register multiple resources inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resources(
		 *     new ResourceRegistration(fileResource, fileHandler),
		 *     new ResourceRegistration(dbResource, dbHandler),
		 *     new ResourceRegistration(apiResource, apiHandler)
		 * )
		 * }</pre>
		 * @param resourceRegistrations The resource registrations to add. Must not be
		 * null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if resourceRegistrations is null
		 */
		public SyncSpec resources(McpServerFeatures.SyncResourceRegistration... resourceRegistrations) {
			Assert.notNull(resourceRegistrations, "Resource handlers list must not be null");
			for (McpServerFeatures.SyncResourceRegistration resource : resourceRegistrations) {
				this.resources.put(resource.getResource().getUri(), resource);
			}
			return this;
		}

		/**
		 * Sets the resource templates that define patterns for dynamic resource access.
		 * Templates use URI patterns with placeholders that can be filled at runtime.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .resourceTemplates(
		 *     new ResourceTemplate("file://{path}", "Access files by path"),
		 *     new ResourceTemplate("db://{table}/{id}", "Access database records")
		 * )
		 * }</pre>
		 * @param resourceTemplates List of resource templates. If null, clears existing
		 * templates.
		 * @return This builder instance for method chaining
		 * @see #resourceTemplates(ResourceTemplate...)
		 */
		public SyncSpec resourceTemplates(List<ResourceTemplate> resourceTemplates) {
			this.resourceTemplates.addAll(resourceTemplates);
			return this;
		}

		/**
		 * Sets the resource templates using varargs for convenience. This is an
		 * alternative to {@link #resourceTemplates(List)}.
		 * @param resourceTemplates The resource templates to set.
		 * @return This builder instance for method chaining
		 * @see #resourceTemplates(List)
		 */
		public SyncSpec resourceTemplates(ResourceTemplate... resourceTemplates) {
			for (ResourceTemplate resourceTemplate : resourceTemplates) {
				this.resourceTemplates.add(resourceTemplate);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a Map. This method is
		 * useful when prompts are dynamically generated or loaded from a configuration
		 * source.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * Map<String, PromptRegistration> prompts = new HashMap<>();
		 * prompts.put("analysis", new PromptRegistration(
		 *     new Prompt("analysis", "Code analysis template"),
		 *     request -> new GetPromptResult(generateAnalysisPrompt(request))
		 * ));
		 * .prompts(prompts)
		 * }</pre>
		 * @param prompts Map of prompt name to registration. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public SyncSpec prompts(Map<String, McpServerFeatures.SyncPromptRegistration> prompts) {
			this.prompts.putAll(prompts);
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using a List. This method is
		 * useful when prompts need to be added in bulk from a collection.
		 * @param prompts List of prompt registrations. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 * @see #prompts(McpServerFeatures.SyncPromptRegistration...)
		 */
		public SyncSpec prompts(List<McpServerFeatures.SyncPromptRegistration> prompts) {
			for (McpServerFeatures.SyncPromptRegistration prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers multiple prompts with their handlers using varargs. This method
		 * provides a convenient way to register multiple prompts inline.
		 *
		 * <p>
		 * Example usage: <pre>{@code
		 * .prompts(
		 *     new PromptRegistration(analysisPrompt, analysisHandler),
		 *     new PromptRegistration(summaryPrompt, summaryHandler),
		 *     new PromptRegistration(reviewPrompt, reviewHandler)
		 * )
		 * }</pre>
		 * @param prompts The prompt registrations to add. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if prompts is null
		 */
		public SyncSpec prompts(McpServerFeatures.SyncPromptRegistration... prompts) {
			for (McpServerFeatures.SyncPromptRegistration prompt : prompts) {
				this.prompts.put(prompt.getPrompt().getName(), prompt);
			}
			return this;
		}

		/**
		 * Registers a consumer that will be notified when the list of roots changes. This
		 * is useful for updating resource availability dynamically, such as when new
		 * files are added or removed.
		 * @param consumer The consumer to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumer is null
		 */
		public SyncSpec rootsChangeConsumer(Consumer<List<McpSchema.Root>> consumer) {
			Assert.notNull(consumer, "Consumer must not be null");
			this.rootsChangeConsumers.add(consumer);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes. This method is useful when multiple consumers need to be registered at
		 * once.
		 * @param consumers The list of consumers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 */
		public SyncSpec rootsChangeConsumers(List<Consumer<List<McpSchema.Root>>> consumers) {
			Assert.notNull(consumers, "Consumers list must not be null");
			this.rootsChangeConsumers.addAll(consumers);
			return this;
		}

		/**
		 * Registers multiple consumers that will be notified when the list of roots
		 * changes using varargs. This method provides a convenient way to register
		 * multiple consumers inline.
		 * @param consumers The consumers to register. Must not be null.
		 * @return This builder instance for method chaining
		 * @throws IllegalArgumentException if consumers is null
		 */
		public SyncSpec rootsChangeConsumers(Consumer<List<McpSchema.Root>>... consumers) {
			for (Consumer<List<McpSchema.Root>> consumer : consumers) {
				this.rootsChangeConsumers.add(consumer);
			}
			return this;
		}

		/**
		 * Builds a synchronous MCP server that provides blocking operations.
		 * @return A new instance of {@link McpSyncServer} configured with this builder's
		 * settings
		 */
		public McpSyncServer build() {
			var tools = this.tools.stream().map(McpServerFeatures.SyncToolRegistration::toSpecification).collect(Collectors.toList());

			var resources = this.resources.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<String, McpServerFeatures.SyncResourceSpecification>(entry.getKey(), entry.getValue().toSpecification()) {})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			var prompts = this.prompts.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().toSpecification()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			var rootsChangeHandlers = this.rootsChangeConsumers.stream()
				.map(consumer -> (BiConsumer<McpSyncServerExchange, List<McpSchema.Root>>) (exchange, roots) -> consumer
					.accept(roots))
					.collect(Collectors.toList());

			McpServerFeatures.Sync syncFeatures = new McpServerFeatures.Sync(this.serverInfo, this.serverCapabilities,
					tools, resources, this.resourceTemplates, prompts, rootsChangeHandlers);

			McpServerFeatures.Async asyncFeatures = McpServerFeatures.Async.fromSync(syncFeatures);
			var asyncServer = new McpAsyncServer(this.transport, asyncFeatures);

			return new McpSyncServer(asyncServer);
		}

	}

}
