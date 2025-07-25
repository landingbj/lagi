/*
 * Copyright 2024-2024 the original author or authors.
 */

package ai.mcps.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.mcps.spec.*;
import ai.mcps.spec.McpSchema.*;
import ai.mcps.util.Utils;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Model Context Protocol (MCP) server implementation that provides asynchronous
 * communication using Project Reactor's Mono and Flux types.
 *
 * <p>
 * This server implements the MCP specification, enabling AI models to expose tools,
 * resources, and prompts through a standardized interface. Key features include:
 * <ul>
 * <li>Asynchronous communication using reactive programming patterns
 * <li>Dynamic tool registration and management
 * <li>Resource handling with URI-based addressing
 * <li>Prompt template management
 * <li>Real-time client notifications for state changes
 * <li>Structured logging with configurable severity levels
 * <li>Support for client-side AI model sampling
 * </ul>
 *
 * <p>
 * The server follows a lifecycle:
 * <ol>
 * <li>Initialization - Accepts client connections and negotiates capabilities
 * <li>Normal Operation - Handles client requests and sends notifications
 * <li>Graceful Shutdown - Ensures clean connection termination
 * </ol>
 *
 * <p>
 * This implementation uses Project Reactor for non-blocking operations, making it
 * suitable for high-throughput scenarios and reactive applications. All operations return
 * Mono or Flux types that can be composed into reactive pipelines.
 *
 * <p>
 * The server supports runtime modification of its capabilities through methods like
 * {@link #addTool}, {@link #addResource}, and {@link #addPrompt}, automatically notifying
 * connected clients of changes when configured to do so.
 *
 * 
 * 
 * @see McpServer
 * @see McpSchema
 * @see McpClientSession
 */
public class McpAsyncServer {

	private static final Logger logger = LoggerFactory.getLogger(McpAsyncServer.class);

	private final McpAsyncServer delegate;

	McpAsyncServer() {
		this.delegate = null;
	}

	/**
	 * Create a new McpAsyncServer with the given transport and capabilities.
	 * @param mcpTransport The transport layer implementation for MCP communication.
	 * @param features The MCP server supported features.
	 * @deprecated This constructor will beremoved in 0.9.0. Use
	 * {@link #McpAsyncServer(McpServerTransportProvider, ObjectMapper, McpServerFeatures.Async)}
	 * instead.
	 */
	@Deprecated
	McpAsyncServer(ServerMcpTransport mcpTransport, McpServerFeatures.Async features) {
		this.delegate = new LegacyAsyncServer(mcpTransport, features);
	}

	/**
	 * Create a new McpAsyncServer with the given transport provider and capabilities.
	 * @param mcpTransportProvider The transport layer implementation for MCP
	 * communication.
	 * @param features The MCP server supported features.
	 * @param objectMapper The ObjectMapper to use for JSON serialization/deserialization
	 */
	McpAsyncServer(McpServerTransportProvider mcpTransportProvider, ObjectMapper objectMapper,
			McpServerFeatures.Async features) {
		this.delegate = new AsyncServerImpl(mcpTransportProvider, objectMapper, features);
	}

	/**
	 * Get the server capabilities that define the supported features and functionality.
	 * @return The server capabilities
	 */
	public McpSchema.ServerCapabilities getServerCapabilities() {
		return this.delegate.getServerCapabilities();
	}

	/**
	 * Get the server implementation information.
	 * @return The server implementation details
	 */
	public McpSchema.Implementation getServerInfo() {
		return this.delegate.getServerInfo();
	}

	/**
	 * Get the client capabilities that define the supported features and functionality.
	 * @return The client capabilities
	 * @deprecated This will be removed in 0.9.0. Use
	 * {@link McpAsyncServerExchange#getClientCapabilities()}.
	 */
	@Deprecated
	public ClientCapabilities getClientCapabilities() {
		return this.delegate.getClientCapabilities();
	}

	/**
	 * Get the client implementation information.
	 * @return The client implementation details
	 * @deprecated This will be removed in 0.9.0. Use
	 * {@link McpAsyncServerExchange#getClientInfo()}.
	 */
	@Deprecated
	public McpSchema.Implementation getClientInfo() {
		return this.delegate.getClientInfo();
	}

	/**
	 * Gracefully closes the server, allowing any in-progress operations to complete.
	 * @return A Mono that completes when the server has been closed
	 */
	public Mono<Void> closeGracefully() {
		return this.delegate.closeGracefully();
	}

	/**
	 * Close the server immediately.
	 */
	public void close() {
		this.delegate.close();
	}

	/**
	 * Retrieves the list of all roots provided by the client.
	 * @return A Mono that emits the list of roots result.
	 * @deprecated This will be removed in 0.9.0. Use
	 * {@link McpAsyncServerExchange#listRoots()}.
	 */
	@Deprecated
	public Mono<McpSchema.ListRootsResult> listRoots() {
		return this.delegate.listRoots(null);
	}

	/**
	 * Retrieves a paginated list of roots provided by the server.
	 * @param cursor Optional pagination cursor from a previous list request
	 * @return A Mono that emits the list of roots result containing
	 * @deprecated This will be removed in 0.9.0. Use
	 * {@link McpAsyncServerExchange#listRoots(String)}.
	 */
	@Deprecated
	public Mono<McpSchema.ListRootsResult> listRoots(String cursor) {
		return this.delegate.listRoots(cursor);
	}

	// ---------------------------------------
	// Tool Management
	// ---------------------------------------

	/**
	 * Add a new tool registration at runtime.
	 * @param toolRegistration The tool registration to add
	 * @return Mono that completes when clients have been notified of the change
	 * @deprecated This method will be removed in 0.9.0. Use
	 * {@link #addTool(McpServerFeatures.AsyncToolSpecification)}.
	 */
	@Deprecated
	public Mono<Void> addTool(McpServerFeatures.AsyncToolRegistration toolRegistration) {
		return this.delegate.addTool(toolRegistration);
	}

	/**
	 * Add a new tool specification at runtime.
	 * @param toolSpecification The tool specification to add
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> addTool(McpServerFeatures.AsyncToolSpecification toolSpecification) {
		return this.delegate.addTool(toolSpecification);
	}

	/**
	 * Remove a tool handler at runtime.
	 * @param toolName The name of the tool handler to remove
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> removeTool(String toolName) {
		return this.delegate.removeTool(toolName);
	}

	/**
	 * Notifies clients that the list of available tools has changed.
	 * @return A Mono that completes when all clients have been notified
	 */
	public Mono<Void> notifyToolsListChanged() {
		return this.delegate.notifyToolsListChanged();
	}

	// ---------------------------------------
	// Resource Management
	// ---------------------------------------

	/**
	 * Add a new resource handler at runtime.
	 * @param resourceHandler The resource handler to add
	 * @return Mono that completes when clients have been notified of the change
	 * @deprecated This method will be removed in 0.9.0. Use
	 * {@link #addResource(McpServerFeatures.AsyncResourceSpecification)}.
	 */
	@Deprecated
	public Mono<Void> addResource(McpServerFeatures.AsyncResourceRegistration resourceHandler) {
		return this.delegate.addResource(resourceHandler);
	}

	/**
	 * Add a new resource handler at runtime.
	 * @param resourceHandler The resource handler to add
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> addResource(McpServerFeatures.AsyncResourceSpecification resourceHandler) {
		return this.delegate.addResource(resourceHandler);
	}

	/**
	 * Remove a resource handler at runtime.
	 * @param resourceUri The URI of the resource handler to remove
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> removeResource(String resourceUri) {
		return this.delegate.removeResource(resourceUri);
	}

	/**
	 * Notifies clients that the list of available resources has changed.
	 * @return A Mono that completes when all clients have been notified
	 */
	public Mono<Void> notifyResourcesListChanged() {
		return this.delegate.notifyResourcesListChanged();
	}

	// ---------------------------------------
	// Prompt Management
	// ---------------------------------------

	/**
	 * Add a new prompt handler at runtime.
	 * @param promptRegistration The prompt handler to add
	 * @return Mono that completes when clients have been notified of the change
	 * @deprecated This method will be removed in 0.9.0. Use
	 * {@link #addPrompt(McpServerFeatures.AsyncPromptSpecification)}.
	 */
	@Deprecated
	public Mono<Void> addPrompt(McpServerFeatures.AsyncPromptRegistration promptRegistration) {
		return this.delegate.addPrompt(promptRegistration);
	}

	/**
	 * Add a new prompt handler at runtime.
	 * @param promptSpecification The prompt handler to add
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> addPrompt(McpServerFeatures.AsyncPromptSpecification promptSpecification) {
		return this.delegate.addPrompt(promptSpecification);
	}

	/**
	 * Remove a prompt handler at runtime.
	 * @param promptName The name of the prompt handler to remove
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> removePrompt(String promptName) {
		return this.delegate.removePrompt(promptName);
	}

	/**
	 * Notifies clients that the list of available prompts has changed.
	 * @return A Mono that completes when all clients have been notified
	 */
	public Mono<Void> notifyPromptsListChanged() {
		return this.delegate.notifyPromptsListChanged();
	}

	// ---------------------------------------
	// Logging Management
	// ---------------------------------------

	/**
	 * Send a logging message notification to all connected clients. Messages below the
	 * current minimum logging level will be filtered out.
	 * @param loggingMessageNotification The logging message to send
	 * @return A Mono that completes when the notification has been sent
	 */
	public Mono<Void> loggingNotification(LoggingMessageNotification loggingMessageNotification) {
		return this.delegate.loggingNotification(loggingMessageNotification);
	}

	// ---------------------------------------
	// Sampling
	// ---------------------------------------

	/**
	 * Create a new message using the sampling capabilities of the client. The Model
	 * Context Protocol (MCP) provides a standardized way for servers to request LLM
	 * sampling (“completions” or “generations”) from language models via clients. This
	 * flow allows clients to maintain control over model access, selection, and
	 * permissions while enabling servers to leverage AI capabilities—with no server API
	 * keys necessary. Servers can request text or image-based interactions and optionally
	 * include context from MCP servers in their prompts.
	 * @param createMessageRequest The request to create a new message
	 * @return A Mono that completes when the message has been created
	 * @throws McpError if the client has not been initialized or does not support
	 * sampling capabilities
	 * @throws McpError if the client does not support the createMessage method
	 * @see McpSchema.CreateMessageRequest
	 * @see McpSchema.CreateMessageResult
	 * @see <a href=
	 * "https://spec.modelcontextprotocol.io/specification/client/sampling/">Sampling
	 * Specification</a>
	 * @deprecated This will be removed in 0.9.0. Use
	 * {@link McpAsyncServerExchange#createMessage(McpSchema.CreateMessageRequest)}.
	 */
	@Deprecated
	public Mono<McpSchema.CreateMessageResult> createMessage(McpSchema.CreateMessageRequest createMessageRequest) {
		return this.delegate.createMessage(createMessageRequest);
	}

	/**
	 * This method is package-private and used for test only. Should not be called by user
	 * code.
	 * @param protocolVersions the Client supported protocol versions.
	 */
	void setProtocolVersions(List<String> protocolVersions) {
		this.delegate.setProtocolVersions(protocolVersions);
	}

	private static class AsyncServerImpl extends McpAsyncServer {

		private final McpServerTransportProvider mcpTransportProvider;

		private final ObjectMapper objectMapper;

		private final McpSchema.ServerCapabilities serverCapabilities;

		private final McpSchema.Implementation serverInfo;

		private final CopyOnWriteArrayList<McpServerFeatures.AsyncToolSpecification> tools = new CopyOnWriteArrayList<>();

		private final CopyOnWriteArrayList<McpSchema.ResourceTemplate> resourceTemplates = new CopyOnWriteArrayList<>();

		private final ConcurrentHashMap<String, McpServerFeatures.AsyncResourceSpecification> resources = new ConcurrentHashMap<>();

		private final ConcurrentHashMap<String, McpServerFeatures.AsyncPromptSpecification> prompts = new ConcurrentHashMap<>();

		private LoggingLevel minLoggingLevel = LoggingLevel.DEBUG;

		private List<String> protocolVersions = Collections.singletonList(McpSchema.LATEST_PROTOCOL_VERSION);

		AsyncServerImpl(McpServerTransportProvider mcpTransportProvider, ObjectMapper objectMapper,
				McpServerFeatures.Async features) {
			this.mcpTransportProvider = mcpTransportProvider;
			this.objectMapper = objectMapper;
			this.serverInfo = features.getServerInfo();
			this.serverCapabilities = features.getServerCapabilities();
			this.tools.addAll(features.getTools());
			this.resources.putAll(features.getResources());
			this.resourceTemplates.addAll(features.getResourceTemplates());
			this.prompts.putAll(features.getPrompts());

			Map<String, McpServerSession.RequestHandler<?>> requestHandlers = new HashMap<>();

			// Initialize request handlers for standard MCP methods

			// Ping MUST respond with an empty data, but not NULL response.
			requestHandlers.put(McpSchema.METHOD_PING, (exchange, params) -> Mono.just(Collections.emptyMap()));

			// Add tools API handlers if the tool capability is enabled
			if (this.serverCapabilities.getTools() != null) {
				requestHandlers.put(McpSchema.METHOD_TOOLS_LIST, toolsListRequestHandler());
				requestHandlers.put(McpSchema.METHOD_TOOLS_CALL, toolsCallRequestHandler());
			}

			// Add resources API handlers if provided
			if (this.serverCapabilities.getResources() != null) {
				requestHandlers.put(McpSchema.METHOD_RESOURCES_LIST, resourcesListRequestHandler());
				requestHandlers.put(McpSchema.METHOD_RESOURCES_READ, resourcesReadRequestHandler());
				requestHandlers.put(McpSchema.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateListRequestHandler());
			}

			// Add prompts API handlers if provider exists
			if (this.serverCapabilities.getPrompts() != null) {
				requestHandlers.put(McpSchema.METHOD_PROMPT_LIST, promptsListRequestHandler());
				requestHandlers.put(McpSchema.METHOD_PROMPT_GET, promptsGetRequestHandler());
			}

			// Add logging API handlers if the logging capability is enabled
			if (this.serverCapabilities.getLogging() != null) {
				requestHandlers.put(McpSchema.METHOD_LOGGING_SET_LEVEL, setLoggerRequestHandler());
			}

			Map<String, McpServerSession.NotificationHandler> notificationHandlers = new HashMap<>();

			notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_INITIALIZED, (exchange, params) -> Mono.empty());

			List<BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>> rootsChangeConsumers = features
				.getRootsChangeConsumers();

			if (Utils.isEmpty(rootsChangeConsumers)) {
				rootsChangeConsumers = Collections.singletonList((exchange,
                                                                  roots) -> Mono.fromRunnable(() -> logger.warn(
                        "Roots list changed notification, but no consumers provided. Roots list changed: {}",
                        roots)));
			}

			notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED,
					asyncRootsListChangedNotificationHandler(rootsChangeConsumers));

			mcpTransportProvider
				.setSessionFactory(transport -> new McpServerSession(UUID.randomUUID().toString(), transport,
						this::asyncInitializeRequestHandler, Mono::empty, requestHandlers, notificationHandlers));
		}

		// ---------------------------------------
		// Lifecycle Management
		// ---------------------------------------
		private Mono<McpSchema.InitializeResult> asyncInitializeRequestHandler(
				McpSchema.InitializeRequest initializeRequest) {
			return Mono.defer(() -> {
				logger.info("Client initialize request - Protocol: {}, Capabilities: {}, Info: {}",
						initializeRequest.getProtocolVersion(), initializeRequest.getCapabilities(),
						initializeRequest.getClientInfo());

				// The server MUST respond with the highest protocol version it supports
				// if
				// it does not support the requested (e.g. Client) version.
				String serverProtocolVersion = this.protocolVersions.get(this.protocolVersions.size() - 1);

				if (this.protocolVersions.contains(initializeRequest.getProtocolVersion())) {
					// If the server supports the requested protocol version, it MUST
					// respond
					// with the same version.
					serverProtocolVersion = initializeRequest.getProtocolVersion();
				}
				else {
					logger.warn(
							"Client requested unsupported protocol version: {}, so the server will sugggest the {} version instead",
							initializeRequest.getProtocolVersion(), serverProtocolVersion);
				}

				return Mono.just(new McpSchema.InitializeResult(serverProtocolVersion, this.serverCapabilities,
						this.serverInfo, null));
			});
		}

		public McpSchema.ServerCapabilities getServerCapabilities() {
			return this.serverCapabilities;
		}

		public McpSchema.Implementation getServerInfo() {
			return this.serverInfo;
		}

		@Override
		@Deprecated
		public ClientCapabilities getClientCapabilities() {
			throw new IllegalStateException("This method is deprecated and should not be called");
		}

		@Override
		@Deprecated
		public McpSchema.Implementation getClientInfo() {
			throw new IllegalStateException("This method is deprecated and should not be called");
		}

		@Override
		public Mono<Void> closeGracefully() {
			return this.mcpTransportProvider.closeGracefully();
		}

		@Override
		public void close() {
			this.mcpTransportProvider.close();
		}

		@Override
		@Deprecated
		public Mono<McpSchema.ListRootsResult> listRoots() {
			return this.listRoots(null);
		}

		@Override
		@Deprecated
		public Mono<McpSchema.ListRootsResult> listRoots(String cursor) {
			return Mono.error(new RuntimeException("Not implemented"));
		}

		private McpServerSession.NotificationHandler asyncRootsListChangedNotificationHandler(
				List<BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>> rootsChangeConsumers) {
			return (exchange, params) -> exchange.listRoots()
				.flatMap(listRootsResult -> Flux.fromIterable(rootsChangeConsumers)
					.flatMap(consumer -> consumer.apply(exchange, listRootsResult.getRoots()))
					.onErrorResume(error -> {
						logger.error("Error handling roots list change notification", error);
						return Mono.empty();
					})
					.then());
		}

		// ---------------------------------------
		// Tool Management
		// ---------------------------------------

		@Override
		public Mono<Void> addTool(McpServerFeatures.AsyncToolSpecification toolSpecification) {
			if (toolSpecification == null) {
				return Mono.error(new McpError("Tool specification must not be null"));
			}
			if (toolSpecification.getTool() == null) {
				return Mono.error(new McpError("Tool must not be null"));
			}
			if (toolSpecification.getCall() == null) {
				return Mono.error(new McpError("Tool call handler must not be null"));
			}
			if (this.serverCapabilities.getTools() == null) {
				return Mono.error(new McpError("Server must be configured with tool capabilities"));
			}

			return Mono.defer(() -> {
				// Check for duplicate tool names
				if (this.tools.stream().anyMatch(th -> th.getTool().getName().equals(toolSpecification.getTool().getName()))) {
					return Mono
						.error(new McpError("Tool with name '" + toolSpecification.getTool().getName() + "' already exists"));
				}

				this.tools.add(toolSpecification);
				logger.debug("Added tool handler: {}", toolSpecification.getTool().getName());

				if (this.serverCapabilities.getTools().getListChanged()) {
					return notifyToolsListChanged();
				}
				return Mono.empty();
			});
		}

		@Override
		public Mono<Void> addTool(McpServerFeatures.AsyncToolRegistration toolRegistration) {
			return this.addTool(toolRegistration.toSpecification());
		}

		@Override
		public Mono<Void> removeTool(String toolName) {
			if (toolName == null) {
				return Mono.error(new McpError("Tool name must not be null"));
			}
			if (this.serverCapabilities.getTools() == null) {
				return Mono.error(new McpError("Server must be configured with tool capabilities"));
			}

			return Mono.defer(() -> {
				boolean removed = this.tools
					.removeIf(toolSpecification -> toolSpecification.getTool().getName().equals(toolName));
				if (removed) {
					logger.debug("Removed tool handler: {}", toolName);
					if (this.serverCapabilities.getTools().getListChanged()) {
						return notifyToolsListChanged();
					}
					return Mono.empty();
				}
				return Mono.error(new McpError("Tool with name '" + toolName + "' not found"));
			});
		}

		@Override
		public Mono<Void> notifyToolsListChanged() {
			return this.mcpTransportProvider.notifyClients(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, null);
		}

		private McpServerSession.RequestHandler<McpSchema.ListToolsResult> toolsListRequestHandler() {
			return (exchange, params) -> {
				List<Tool> tools = this.tools.stream().map(McpServerFeatures.AsyncToolSpecification::getTool).collect(Collectors.toList());

				return Mono.just(new McpSchema.ListToolsResult(tools, null));
			};
		}

		private McpServerSession.RequestHandler<CallToolResult> toolsCallRequestHandler() {
			return (exchange, params) -> {
				McpSchema.CallToolRequest callToolRequest = objectMapper.convertValue(params,
						new TypeReference<McpSchema.CallToolRequest>() {
						});

				Optional<McpServerFeatures.AsyncToolSpecification> toolSpecification = this.tools.stream()
					.filter(tr -> callToolRequest.getName().equals(tr.getTool().getName()))
					.findAny();

				if (!toolSpecification.isPresent()) {
					return Mono.error(new McpError("Tool not found: " + callToolRequest.getName()));
				}

				return toolSpecification.map(tool -> tool.getCall().apply(exchange, callToolRequest.getArguments()))
					.orElse(Mono.error(new McpError("Tool not found: " + callToolRequest.getName())));
			};
		}

		// ---------------------------------------
		// Resource Management
		// ---------------------------------------

		@Override
		public Mono<Void> addResource(McpServerFeatures.AsyncResourceSpecification resourceSpecification) {
			if (resourceSpecification == null || resourceSpecification.getResource() == null) {
				return Mono.error(new McpError("Resource must not be null"));
			}

			if (this.serverCapabilities.getResources() == null) {
				return Mono.error(new McpError("Server must be configured with resource capabilities"));
			}

			return Mono.defer(() -> {
				if (this.resources.putIfAbsent(resourceSpecification.getResource().getDescription(), resourceSpecification) != null) {
					return Mono.error(new McpError(
							"Resource with URI '" + resourceSpecification.getResource().getDescription() + "' already exists"));
				}
				logger.debug("Added resource handler: {}", resourceSpecification.getResource().getUri());
				if (this.serverCapabilities.getResources().getListChanged()) {
					return notifyResourcesListChanged();
				}
				return Mono.empty();
			});
		}

		@Override
		public Mono<Void> addResource(McpServerFeatures.AsyncResourceRegistration resourceHandler) {
			return this.addResource(resourceHandler.toSpecification());
		}

		@Override
		public Mono<Void> removeResource(String resourceUri) {
			if (resourceUri == null) {
				return Mono.error(new McpError("Resource URI must not be null"));
			}
			if (this.serverCapabilities.getResources() == null) {
				return Mono.error(new McpError("Server must be configured with resource capabilities"));
			}

			return Mono.defer(() -> {
				McpServerFeatures.AsyncResourceSpecification removed = this.resources.remove(resourceUri);
				if (removed != null) {
					logger.debug("Removed resource handler: {}", resourceUri);
					if (this.serverCapabilities.getResources().getListChanged()) {
						return notifyResourcesListChanged();
					}
					return Mono.empty();
				}
				return Mono.error(new McpError("Resource with URI '" + resourceUri + "' not found"));
			});
		}

		@Override
		public Mono<Void> notifyResourcesListChanged() {
			return this.mcpTransportProvider.notifyClients(McpSchema.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED, null);
		}

		private McpServerSession.RequestHandler<McpSchema.ListResourcesResult> resourcesListRequestHandler() {
			return (exchange, params) -> {
				var resourceList = this.resources.values()
					.stream()
					.map(McpServerFeatures.AsyncResourceSpecification::getResource)
					.collect(Collectors.toList());
				return Mono.just(new McpSchema.ListResourcesResult(resourceList, null));
			};
		}

		private McpServerSession.RequestHandler<McpSchema.ListResourceTemplatesResult> resourceTemplateListRequestHandler() {
			return (exchange, params) -> Mono
				.just(new McpSchema.ListResourceTemplatesResult(this.resourceTemplates, null));

		}

		private McpServerSession.RequestHandler<McpSchema.ReadResourceResult> resourcesReadRequestHandler() {
			return (exchange, params) -> {
				McpSchema.ReadResourceRequest resourceRequest = objectMapper.convertValue(params,
						new TypeReference<McpSchema.ReadResourceRequest>() {
						});
				var resourceUri = resourceRequest.getUri();
				McpServerFeatures.AsyncResourceSpecification specification = this.resources.get(resourceUri);
				if (specification != null) {
					return specification.getReadHandler().apply(exchange, resourceRequest);
				}
				return Mono.error(new McpError("Resource not found: " + resourceUri));
			};
		}

		// ---------------------------------------
		// Prompt Management
		// ---------------------------------------

		@Override
		public Mono<Void> addPrompt(McpServerFeatures.AsyncPromptSpecification promptSpecification) {
			if (promptSpecification == null) {
				return Mono.error(new McpError("Prompt specification must not be null"));
			}
			if (this.serverCapabilities.getPrompts() == null) {
				return Mono.error(new McpError("Server must be configured with prompt capabilities"));
			}

			return Mono.defer(() -> {
				McpServerFeatures.AsyncPromptSpecification specification = this.prompts
					.putIfAbsent(promptSpecification.getPrompt().getName(), promptSpecification);
				if (specification != null) {
					return Mono.error(new McpError(
							"Prompt with name '" + promptSpecification.getPrompt().getName() + "' already exists"));
				}

				logger.debug("Added prompt handler: {}", promptSpecification.getPrompt().getName());

				// Servers that declared the listChanged capability SHOULD send a
				// notification,
				// when the list of available prompts changes
				if (this.serverCapabilities.getPrompts().getListChanged()) {
					return notifyPromptsListChanged();
				}
				return Mono.empty();
			});
		}

		@Override
		public Mono<Void> addPrompt(McpServerFeatures.AsyncPromptRegistration promptRegistration) {
			return this.addPrompt(promptRegistration.toSpecification());
		}

		@Override
		public Mono<Void> removePrompt(String promptName) {
			if (promptName == null) {
				return Mono.error(new McpError("Prompt name must not be null"));
			}
			if (this.serverCapabilities.getPrompts() == null) {
				return Mono.error(new McpError("Server must be configured with prompt capabilities"));
			}

			return Mono.defer(() -> {
				McpServerFeatures.AsyncPromptSpecification removed = this.prompts.remove(promptName);

				if (removed != null) {
					logger.debug("Removed prompt handler: {}", promptName);
					// Servers that declared the listChanged capability SHOULD send a
					// notification, when the list of available prompts changes
					if (this.serverCapabilities.getPrompts().getListChanged()) {
						return this.notifyPromptsListChanged();
					}
					return Mono.empty();
				}
				return Mono.error(new McpError("Prompt with name '" + promptName + "' not found"));
			});
		}

		@Override
		public Mono<Void> notifyPromptsListChanged() {
			return this.mcpTransportProvider.notifyClients(McpSchema.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED, null);
		}

		private McpServerSession.RequestHandler<McpSchema.ListPromptsResult> promptsListRequestHandler() {
			return (exchange, params) -> {
				// TODO: Implement pagination
				// McpSchema.PaginatedRequest request = objectMapper.convertValue(params,
				// new TypeReference<McpSchema.PaginatedRequest>() {
				// });

				var promptList = this.prompts.values()
					.stream()
					.map(McpServerFeatures.AsyncPromptSpecification::getPrompt)
					.collect(Collectors.toList());

				return Mono.just(new McpSchema.ListPromptsResult(promptList, null));
			};
		}

		private McpServerSession.RequestHandler<McpSchema.GetPromptResult> promptsGetRequestHandler() {
			return (exchange, params) -> {
				McpSchema.GetPromptRequest promptRequest = objectMapper.convertValue(params,
						new TypeReference<McpSchema.GetPromptRequest>() {
						});

				// Implement prompt retrieval logic here
				McpServerFeatures.AsyncPromptSpecification specification = this.prompts.get(promptRequest.getName());
				if (specification == null) {
					return Mono.error(new McpError("Prompt not found: " + promptRequest.getName()));
				}

				return specification.getPromptHandler().apply(exchange, promptRequest);
			};
		}

		// ---------------------------------------
		// Logging Management
		// ---------------------------------------

		@Override
		public Mono<Void> loggingNotification(LoggingMessageNotification loggingMessageNotification) {

			if (loggingMessageNotification == null) {
				return Mono.error(new McpError("Logging message must not be null"));
			}

			Map<String, Object> params = this.objectMapper.convertValue(loggingMessageNotification,
					new TypeReference<Map<String, Object>>() {
					});

			if (loggingMessageNotification.getLevel().level() < minLoggingLevel.level()) {
				return Mono.empty();
			}

			return this.mcpTransportProvider.notifyClients(McpSchema.METHOD_NOTIFICATION_MESSAGE, params);
		}

		private McpServerSession.RequestHandler<Void> setLoggerRequestHandler() {
			return (exchange, params) -> {
				this.minLoggingLevel = objectMapper.convertValue(params, new TypeReference<LoggingLevel>() {
				});

				return Mono.empty();
			};
		}

		// ---------------------------------------
		// Sampling
		// ---------------------------------------

		@Override
		@Deprecated
		public Mono<McpSchema.CreateMessageResult> createMessage(McpSchema.CreateMessageRequest createMessageRequest) {
			return Mono.error(new RuntimeException("Not implemented"));
		}

		@Override
		void setProtocolVersions(List<String> protocolVersions) {
			this.protocolVersions = protocolVersions;
		}

	}

	private static final class LegacyAsyncServer extends McpAsyncServer {

		/**
		 * The MCP session implementation that manages bidirectional JSON-RPC
		 * communication between clients and servers.
		 */
		private final McpClientSession mcpSession;

		private final ServerMcpTransport transport;

		private final McpSchema.ServerCapabilities serverCapabilities;

		private final McpSchema.Implementation serverInfo;

		private McpSchema.ClientCapabilities clientCapabilities;

		private McpSchema.Implementation clientInfo;

		/**
		 * Thread-safe list of tool handlers that can be modified at runtime.
		 */
		private final CopyOnWriteArrayList<McpServerFeatures.AsyncToolSpecification> tools = new CopyOnWriteArrayList<>();

		private final CopyOnWriteArrayList<McpSchema.ResourceTemplate> resourceTemplates = new CopyOnWriteArrayList<>();

		private final ConcurrentHashMap<String, McpServerFeatures.AsyncResourceSpecification> resources = new ConcurrentHashMap<>();

		private final ConcurrentHashMap<String, McpServerFeatures.AsyncPromptSpecification> prompts = new ConcurrentHashMap<>();

		private LoggingLevel minLoggingLevel = LoggingLevel.DEBUG;

		/**
		 * Supported protocol versions.
		 */
		private List<String> protocolVersions = Collections.singletonList(McpSchema.LATEST_PROTOCOL_VERSION);

		/**
		 * Create a new McpAsyncServer with the given transport and capabilities.
		 * @param mcpTransport The transport layer implementation for MCP communication.
		 * @param features The MCP server supported features.
		 */
		LegacyAsyncServer(ServerMcpTransport mcpTransport, McpServerFeatures.Async features) {

			this.serverInfo = features.getServerInfo();
			this.serverCapabilities = features.getServerCapabilities();
			this.tools.addAll(features.getTools());
			this.resources.putAll(features.getResources());
			this.resourceTemplates.addAll(features.getResourceTemplates());
			this.prompts.putAll(features.getPrompts());

			Map<String, McpClientSession.RequestHandler<?>> requestHandlers = new HashMap<>();

			// Initialize request handlers for standard MCP methods
			requestHandlers.put(McpSchema.METHOD_INITIALIZE, asyncInitializeRequestHandler());

			// Ping MUST respond with an empty data, but not NULL response.
			requestHandlers.put(McpSchema.METHOD_PING, (params) -> Mono.just(Collections.emptyMap()));

			// Add tools API handlers if the tool capability is enabled
			if (this.serverCapabilities.getTools() != null) {
				requestHandlers.put(McpSchema.METHOD_TOOLS_LIST, toolsListRequestHandler());
				requestHandlers.put(McpSchema.METHOD_TOOLS_CALL, toolsCallRequestHandler());
			}

			// Add resources API handlers if provided
			if (this.serverCapabilities.getResources() != null) {
				requestHandlers.put(McpSchema.METHOD_RESOURCES_LIST, resourcesListRequestHandler());
				requestHandlers.put(McpSchema.METHOD_RESOURCES_READ, resourcesReadRequestHandler());
				requestHandlers.put(McpSchema.METHOD_RESOURCES_TEMPLATES_LIST, resourceTemplateListRequestHandler());
			}

			// Add prompts API handlers if provider exists
			if (this.serverCapabilities.getPrompts() != null) {
				requestHandlers.put(McpSchema.METHOD_PROMPT_LIST, promptsListRequestHandler());
				requestHandlers.put(McpSchema.METHOD_PROMPT_GET, promptsGetRequestHandler());
			}

			// Add logging API handlers if the logging capability is enabled
			if (this.serverCapabilities.getLogging() != null) {
				requestHandlers.put(McpSchema.METHOD_LOGGING_SET_LEVEL, setLoggerRequestHandler());
			}

			Map<String, McpClientSession.NotificationHandler> notificationHandlers = new HashMap<>();

			notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_INITIALIZED, (params) -> Mono.empty());

			List<BiFunction<McpAsyncServerExchange, List<McpSchema.Root>, Mono<Void>>> rootsChangeHandlers = features
				.getRootsChangeConsumers();

			List<Function<List<McpSchema.Root>, Mono<Void>>> rootsChangeConsumers = rootsChangeHandlers.stream()
				.map(handler -> (Function<List<McpSchema.Root>, Mono<Void>>) (roots) -> handler.apply(null, roots))
				.collect(Collectors.toList());

			if (Utils.isEmpty(rootsChangeConsumers)) {
				rootsChangeConsumers = Collections.singletonList((roots) -> Mono.fromRunnable(() -> logger.warn(
						"Roots list changed notification, but no consumers provided. Roots list changed: {}", roots)));
			}

			notificationHandlers.put(McpSchema.METHOD_NOTIFICATION_ROOTS_LIST_CHANGED,
					asyncRootsListChangedNotificationHandler(rootsChangeConsumers));

			this.transport = mcpTransport;
			this.mcpSession = new McpClientSession(Duration.ofSeconds(10), mcpTransport, requestHandlers,
					notificationHandlers);
		}

		@Override
		public Mono<Void> addTool(McpServerFeatures.AsyncToolSpecification toolSpecification) {
			throw new IllegalArgumentException(
					"McpAsyncServer configured with legacy " + "transport. Use McpServerTransportProvider instead.");
		}

		@Override
		public Mono<Void> addResource(McpServerFeatures.AsyncResourceSpecification resourceHandler) {
			throw new IllegalArgumentException(
					"McpAsyncServer configured with legacy " + "transport. Use McpServerTransportProvider instead.");
		}

		@Override
		public Mono<Void> addPrompt(McpServerFeatures.AsyncPromptSpecification promptSpecification) {
			throw new IllegalArgumentException(
					"McpAsyncServer configured with legacy " + "transport. Use McpServerTransportProvider instead.");
		}

		// ---------------------------------------
		// Lifecycle Management
		// ---------------------------------------
		private McpClientSession.RequestHandler<McpSchema.InitializeResult> asyncInitializeRequestHandler() {
			return params -> {
				McpSchema.InitializeRequest initializeRequest = transport.unmarshalFrom(params,
						new TypeReference<McpSchema.InitializeRequest>() {
						});
				this.clientCapabilities = initializeRequest.getCapabilities();
				this.clientInfo = initializeRequest.getClientInfo();
				logger.info("Client initialize request - Protocol: {}, Capabilities: {}, Info: {}",
						initializeRequest.getProtocolVersion(), initializeRequest.getCapabilities(),
						initializeRequest.getClientInfo());

				// The server MUST respond with the highest protocol version it supports
				// if
				// it does not support the requested (e.g. Client) version.
				String serverProtocolVersion = this.protocolVersions.get(this.protocolVersions.size() - 1);

				if (this.protocolVersions.contains(initializeRequest.getProtocolVersion())) {
					// If the server supports the requested protocol version, it MUST
					// respond
					// with the same version.
					serverProtocolVersion = initializeRequest.getProtocolVersion();
				}
				else {
					logger.warn(
							"Client requested unsupported protocol version: {}, so the server will sugggest the {} version instead",
							initializeRequest.getProtocolVersion(), serverProtocolVersion);
				}

				return Mono.just(new McpSchema.InitializeResult(serverProtocolVersion, this.serverCapabilities,
						this.serverInfo, null));
			};
		}

		/**
		 * Get the server capabilities that define the supported features and
		 * functionality.
		 * @return The server capabilities
		 */
		public McpSchema.ServerCapabilities getServerCapabilities() {
			return this.serverCapabilities;
		}

		/**
		 * Get the server implementation information.
		 * @return The server implementation details
		 */
		public McpSchema.Implementation getServerInfo() {
			return this.serverInfo;
		}

		/**
		 * Get the client capabilities that define the supported features and
		 * functionality.
		 * @return The client capabilities
		 */
		public ClientCapabilities getClientCapabilities() {
			return this.clientCapabilities;
		}

		/**
		 * Get the client implementation information.
		 * @return The client implementation details
		 */
		public McpSchema.Implementation getClientInfo() {
			return this.clientInfo;
		}

		/**
		 * Gracefully closes the server, allowing any in-progress operations to complete.
		 * @return A Mono that completes when the server has been closed
		 */
		public Mono<Void> closeGracefully() {
			return this.mcpSession.closeGracefully();
		}

		/**
		 * Close the server immediately.
		 */
		public void close() {
			this.mcpSession.close();
		}

		private static final TypeReference<McpSchema.ListRootsResult> LIST_ROOTS_RESULT_TYPE_REF = new TypeReference<McpSchema.ListRootsResult>() {
		};

		/**
		 * Retrieves the list of all roots provided by the client.
		 * @return A Mono that emits the list of roots result.
		 */
		public Mono<McpSchema.ListRootsResult> listRoots() {
			return this.listRoots(null);
		}

		/**
		 * Retrieves a paginated list of roots provided by the server.
		 * @param cursor Optional pagination cursor from a previous list request
		 * @return A Mono that emits the list of roots result containing
		 */
		public Mono<McpSchema.ListRootsResult> listRoots(String cursor) {
			return this.mcpSession.sendRequest(McpSchema.METHOD_ROOTS_LIST, new McpSchema.PaginatedRequest(cursor),
					LIST_ROOTS_RESULT_TYPE_REF);
		}

		private McpClientSession.NotificationHandler asyncRootsListChangedNotificationHandler(
				List<Function<List<McpSchema.Root>, Mono<Void>>> rootsChangeConsumers) {
			return params -> listRoots().flatMap(listRootsResult -> Flux.fromIterable(rootsChangeConsumers)
				.flatMap(consumer -> consumer.apply(listRootsResult.getRoots()))
				.onErrorResume(error -> {
					logger.error("Error handling roots list change notification", error);
					return Mono.empty();
				})
				.then());
		}

		// ---------------------------------------
		// Tool Management
		// ---------------------------------------

		/**
		 * Add a new tool registration at runtime.
		 * @param toolRegistration The tool registration to add
		 * @return Mono that completes when clients have been notified of the change
		 */
		@Override
		public Mono<Void> addTool(McpServerFeatures.AsyncToolRegistration toolRegistration) {
			if (toolRegistration == null) {
				return Mono.error(new McpError("Tool registration must not be null"));
			}
			if (toolRegistration.getTool() == null) {
				return Mono.error(new McpError("Tool must not be null"));
			}
			if (toolRegistration.getCall() == null) {
				return Mono.error(new McpError("Tool call handler must not be null"));
			}
			if (this.serverCapabilities.getTools() == null) {
				return Mono.error(new McpError("Server must be configured with tool capabilities"));
			}

			return Mono.defer(() -> {
				// Check for duplicate tool names
				if (this.tools.stream().anyMatch(th -> th.getTool().getName().equals(toolRegistration.getTool().getName()))) {
					return Mono
						.error(new McpError("Tool with name '" + toolRegistration.getTool().getName() + "' already exists"));
				}

				this.tools.add(toolRegistration.toSpecification());
				logger.debug("Added tool handler: {}", toolRegistration.getTool().getName());

				if (this.serverCapabilities.getTools().getListChanged()) {
					return notifyToolsListChanged();
				}
				return Mono.empty();
			});
		}

		/**
		 * Remove a tool handler at runtime.
		 * @param toolName The name of the tool handler to remove
		 * @return Mono that completes when clients have been notified of the change
		 */
		public Mono<Void> removeTool(String toolName) {
			if (toolName == null) {
				return Mono.error(new McpError("Tool name must not be null"));
			}
			if (this.serverCapabilities.getTools() == null) {
				return Mono.error(new McpError("Server must be configured with tool capabilities"));
			}

			return Mono.defer(() -> {
				boolean removed = this.tools
					.removeIf(toolRegistration -> toolRegistration.getTool().getName().equals(toolName));
				if (removed) {
					logger.debug("Removed tool handler: {}", toolName);
					if (this.serverCapabilities.getTools().getListChanged()) {
						return notifyToolsListChanged();
					}
					return Mono.empty();
				}
				return Mono.error(new McpError("Tool with name '" + toolName + "' not found"));
			});
		}

		/**
		 * Notifies clients that the list of available tools has changed.
		 * @return A Mono that completes when all clients have been notified
		 */
		public Mono<Void> notifyToolsListChanged() {
			return this.mcpSession.sendNotification(McpSchema.METHOD_NOTIFICATION_TOOLS_LIST_CHANGED, null);
		}

		private McpClientSession.RequestHandler<McpSchema.ListToolsResult> toolsListRequestHandler() {
			return params -> {
				List<Tool> tools = this.tools.stream().map(McpServerFeatures.AsyncToolSpecification::getTool).collect(Collectors.toList());

				return Mono.just(new McpSchema.ListToolsResult(tools, null));
			};
		}

		private McpClientSession.RequestHandler<CallToolResult> toolsCallRequestHandler() {
			return params -> {
				McpSchema.CallToolRequest callToolRequest = transport.unmarshalFrom(params,
						new TypeReference<McpSchema.CallToolRequest>() {
						});

				Optional<McpServerFeatures.AsyncToolSpecification> toolRegistration = this.tools.stream()
					.filter(tr -> callToolRequest.getName().equals(tr.getTool().getName()))
					.findAny();

				if (!toolRegistration.isPresent()) {
					return Mono.error(new McpError("Tool not found: " + callToolRequest.getName()));
				}

				return toolRegistration.map(tool -> tool.getCall().apply(null, callToolRequest.getArguments()))
					.orElse(Mono.error(new McpError("Tool not found: " + callToolRequest.getName())));
			};
		}

		// ---------------------------------------
		// Resource Management
		// ---------------------------------------

		/**
		 * Add a new resource handler at runtime.
		 * @param resourceHandler The resource handler to add
		 * @return Mono that completes when clients have been notified of the change
		 */
		@Override
		public Mono<Void> addResource(McpServerFeatures.AsyncResourceRegistration resourceHandler) {
			if (resourceHandler == null || resourceHandler.getResource() == null) {
				return Mono.error(new McpError("Resource must not be null"));
			}

			if (this.serverCapabilities.getResources() == null) {
				return Mono.error(new McpError("Server must be configured with resource capabilities"));
			}

			return Mono.defer(() -> {
				if (this.resources.putIfAbsent(resourceHandler.getResource().getUri(),
						resourceHandler.toSpecification()) != null) {
					return Mono.error(new McpError(
							"Resource with URI '" + resourceHandler.getResource().getUri() + "' already exists"));
				}
				logger.debug("Added resource handler: {}", resourceHandler.getResource().getUri());
				if (this.serverCapabilities.getResources().getListChanged()) {
					return notifyResourcesListChanged();
				}
				return Mono.empty();
			});
		}

		/**
		 * Remove a resource handler at runtime.
		 * @param resourceUri The URI of the resource handler to remove
		 * @return Mono that completes when clients have been notified of the change
		 */
		public Mono<Void> removeResource(String resourceUri) {
			if (resourceUri == null) {
				return Mono.error(new McpError("Resource URI must not be null"));
			}
			if (this.serverCapabilities.getResources() == null) {
				return Mono.error(new McpError("Server must be configured with resource capabilities"));
			}

			return Mono.defer(() -> {
				McpServerFeatures.AsyncResourceSpecification removed = this.resources.remove(resourceUri);
				if (removed != null) {
					logger.debug("Removed resource handler: {}", resourceUri);
					if (this.serverCapabilities.getResources().getListChanged()) {
						return notifyResourcesListChanged();
					}
					return Mono.empty();
				}
				return Mono.error(new McpError("Resource with URI '" + resourceUri + "' not found"));
			});
		}

		/**
		 * Notifies clients that the list of available resources has changed.
		 * @return A Mono that completes when all clients have been notified
		 */
		public Mono<Void> notifyResourcesListChanged() {
			return this.mcpSession.sendNotification(McpSchema.METHOD_NOTIFICATION_RESOURCES_LIST_CHANGED, null);
		}

		private McpClientSession.RequestHandler<McpSchema.ListResourcesResult> resourcesListRequestHandler() {
			return params -> {
				var resourceList = this.resources.values()
						.stream()
						.map(McpServerFeatures.AsyncResourceSpecification::getResource)
						.collect(Collectors.toList());
				return Mono.just(new McpSchema.ListResourcesResult(resourceList, null));
			};
		}

		private McpClientSession.RequestHandler<McpSchema.ListResourceTemplatesResult> resourceTemplateListRequestHandler() {
			return params -> Mono.just(new McpSchema.ListResourceTemplatesResult(this.resourceTemplates, null));

		}

		private McpClientSession.RequestHandler<McpSchema.ReadResourceResult> resourcesReadRequestHandler() {
			return params -> {
				McpSchema.ReadResourceRequest resourceRequest = transport.unmarshalFrom(params,
						new TypeReference<McpSchema.ReadResourceRequest>() {
						});
				var resourceUri = resourceRequest.getUri();
				McpServerFeatures.AsyncResourceSpecification registration = this.resources.get(resourceUri);
				if (registration != null) {
					return registration.getReadHandler().apply(null, resourceRequest);
				}
				return Mono.error(new McpError("Resource not found: " + resourceUri));
			};
		}

		// ---------------------------------------
		// Prompt Management
		// ---------------------------------------

		/**
		 * Add a new prompt handler at runtime.
		 * @param promptRegistration The prompt handler to add
		 * @return Mono that completes when clients have been notified of the change
		 */
		@Override
		public Mono<Void> addPrompt(McpServerFeatures.AsyncPromptRegistration promptRegistration) {
			if (promptRegistration == null) {
				return Mono.error(new McpError("Prompt registration must not be null"));
			}
			if (this.serverCapabilities.getPrompts() == null) {
				return Mono.error(new McpError("Server must be configured with prompt capabilities"));
			}

			return Mono.defer(() -> {
				McpServerFeatures.AsyncPromptSpecification registration = this.prompts
					.putIfAbsent(promptRegistration.getPrompt().getName(), promptRegistration.toSpecification());
				if (registration != null) {
					return Mono.error(new McpError(
							"Prompt with name '" + promptRegistration.getPrompt().getName() + "' already exists"));
				}

				logger.debug("Added prompt handler: {}", promptRegistration.getPrompt().getName());

				// Servers that declared the listChanged capability SHOULD send a
				// notification,
				// when the list of available prompts changes
				if (this.serverCapabilities.getPrompts().getListChanged()) {
					return notifyPromptsListChanged();
				}
				return Mono.empty();
			});
		}

		/**
		 * Remove a prompt handler at runtime.
		 * @param promptName The name of the prompt handler to remove
		 * @return Mono that completes when clients have been notified of the change
		 */
		public Mono<Void> removePrompt(String promptName) {
			if (promptName == null) {
				return Mono.error(new McpError("Prompt name must not be null"));
			}
			if (this.serverCapabilities.getPrompts() == null) {
				return Mono.error(new McpError("Server must be configured with prompt capabilities"));
			}

			return Mono.defer(() -> {
				McpServerFeatures.AsyncPromptSpecification removed = this.prompts.remove(promptName);

				if (removed != null) {
					logger.debug("Removed prompt handler: {}", promptName);
					// Servers that declared the listChanged capability SHOULD send a
					// notification, when the list of available prompts changes
					if (this.serverCapabilities.getPrompts().getListChanged()) {
						return this.notifyPromptsListChanged();
					}
					return Mono.empty();
				}
				return Mono.error(new McpError("Prompt with name '" + promptName + "' not found"));
			});
		}

		/**
		 * Notifies clients that the list of available prompts has changed.
		 * @return A Mono that completes when all clients have been notified
		 */
		public Mono<Void> notifyPromptsListChanged() {
			return this.mcpSession.sendNotification(McpSchema.METHOD_NOTIFICATION_PROMPTS_LIST_CHANGED, null);
		}

		private McpClientSession.RequestHandler<McpSchema.ListPromptsResult> promptsListRequestHandler() {
			return params -> {
				// TODO: Implement pagination
				// McpSchema.PaginatedRequest request = transport.unmarshalFrom(params,
				// new TypeReference<McpSchema.PaginatedRequest>() {
				// });

				var promptList = this.prompts.values()
					.stream()
					.map(McpServerFeatures.AsyncPromptSpecification::getPrompt)
					.collect(Collectors.toList());

				return Mono.just(new McpSchema.ListPromptsResult(promptList, null));
			};
		}

		private McpClientSession.RequestHandler<McpSchema.GetPromptResult> promptsGetRequestHandler() {
			return params -> {
				McpSchema.GetPromptRequest promptRequest = transport.unmarshalFrom(params,
						new TypeReference<McpSchema.GetPromptRequest>() {
						});

				// Implement prompt retrieval logic here
				McpServerFeatures.AsyncPromptSpecification registration = this.prompts.get(promptRequest.getName());
				if (registration == null) {
					return Mono.error(new McpError("Prompt not found: " + promptRequest.getName()));
				}

				return registration.getPromptHandler().apply(null, promptRequest);
			};
		}

		// ---------------------------------------
		// Logging Management
		// ---------------------------------------

		/**
		 * Send a logging message notification to all connected clients. Messages below
		 * the current minimum logging level will be filtered out.
		 * @param loggingMessageNotification The logging message to send
		 * @return A Mono that completes when the notification has been sent
		 */
		public Mono<Void> loggingNotification(LoggingMessageNotification loggingMessageNotification) {

			if (loggingMessageNotification == null) {
				return Mono.error(new McpError("Logging message must not be null"));
			}

			Map<String, Object> params = this.transport.unmarshalFrom(loggingMessageNotification,
					new TypeReference<Map<String, Object>>() {
					});

			if (loggingMessageNotification.getLevel().level() < minLoggingLevel.level()) {
				return Mono.empty();
			}

			return this.mcpSession.sendNotification(McpSchema.METHOD_NOTIFICATION_MESSAGE, params);
		}

		/**
		 * Handles requests to set the minimum logging level. Messages below this level
		 * will not be sent.
		 * @return A handler that processes logging level change requests
		 */
		private McpClientSession.RequestHandler<Void> setLoggerRequestHandler() {
			return params -> {
				this.minLoggingLevel = transport.unmarshalFrom(params, new TypeReference<LoggingLevel>() {
				});

				return Mono.empty();
			};
		}

		// ---------------------------------------
		// Sampling
		// ---------------------------------------
		private static final TypeReference<McpSchema.CreateMessageResult> CREATE_MESSAGE_RESULT_TYPE_REF = new TypeReference<McpSchema.CreateMessageResult>() {
		};

		/**
		 * Create a new message using the sampling capabilities of the client. The Model
		 * Context Protocol (MCP) provides a standardized way for servers to request LLM
		 * sampling (“completions” or “generations”) from language models via clients.
		 * This flow allows clients to maintain control over model access, selection, and
		 * permissions while enabling servers to leverage AI capabilities—with no server
		 * API keys necessary. Servers can request text or image-based interactions and
		 * optionally include context from MCP servers in their prompts.
		 * @param createMessageRequest The request to create a new message
		 * @return A Mono that completes when the message has been created
		 * @throws McpError if the client has not been initialized or does not support
		 * sampling capabilities
		 * @throws McpError if the client does not support the createMessage method
		 * @see McpSchema.CreateMessageRequest
		 * @see McpSchema.CreateMessageResult
		 * @see <a href=
		 * "https://spec.modelcontextprotocol.io/specification/client/sampling/">Sampling
		 * Specification</a>
		 */
		public Mono<McpSchema.CreateMessageResult> createMessage(McpSchema.CreateMessageRequest createMessageRequest) {

			if (this.clientCapabilities == null) {
				return Mono.error(new McpError("Client must be initialized. Call the initialize method first!"));
			}
			if (this.clientCapabilities.getSampling() == null) {
				return Mono.error(new McpError("Client must be configured with sampling capabilities"));
			}
			return this.mcpSession.sendRequest(McpSchema.METHOD_SAMPLING_CREATE_MESSAGE, createMessageRequest,
					CREATE_MESSAGE_RESULT_TYPE_REF);
		}

		/**
		 * This method is package-private and used for test only. Should not be called by
		 * user code.
		 * @param protocolVersions the Client supported protocol versions.
		 */
		void setProtocolVersions(List<String> protocolVersions) {
			this.protocolVersions = protocolVersions;
		}

	}

}
