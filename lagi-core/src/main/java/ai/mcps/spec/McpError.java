/*
* Copyright 2024 - 2024 the original author or authors.
*/
package ai.mcps.spec;

import ai.mcps.spec.McpSchema.JSONRPCResponse.JSONRPCError;

public class McpError extends RuntimeException {

	private JSONRPCError jsonRpcError;

	public McpError(JSONRPCError jsonRpcError) {
		super(jsonRpcError.getMessage());
		this.jsonRpcError = jsonRpcError;
	}

	public McpError(Object error) {
		super(error.toString());
	}

	public JSONRPCError getJsonRpcError() {
		return jsonRpcError;
	}

}