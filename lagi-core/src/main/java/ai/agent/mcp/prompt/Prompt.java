package ai.agent.mcp.prompt;//package ai.agent.mcp.prompt;
//
//import ai.mcps.SyncMcpClient;
//import ai.mcps.spec.McpSchema;
//import ai.openai.pojo.Tool;
//import javafx.util.Pair;
//
//import java.util.List;
//import java.util.Map;
//
//public class Prompt {
//    public static String prompt = "";
//
//    public static void generatePrompt(List<Pair<SyncMcpClient, List<McpSchema.Tool>>> mcpHub) {
//
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("You are Cline, a highly skilled software engineer with extensive knowledge in many programming languages, frameworks, design patterns, and best practices.\n");
//        prompt.append("====\n");
//        prompt.append("TOOL USE\n");
//        prompt.append("You have access to a set of tools that are executed upon the user's approval. You can use one tool per message, and will receive the result of that tool use in the user's response. You use tools step-by-step to accomplish a given task, with each tool use informed by the result of the previous tool use.\n");
//        // 此处省略了部分工具使用的详细描述，可根据需要添加完整
//        prompt.append("====\n");
//        prompt.append("MCP SERVERS\n");
//        prompt.append("The Model Context Protocol (MCP) enables communication between the system and locally running MCP servers that provide additional tools and resources to extend your capabilities.\n");
//        prompt.append("# Connected MCP Servers\n");
//        if (!mcpHub.isEmpty()) {
//            for (Pair<SyncMcpClient, List<McpSchema.Tool>> pair : mcpHub) {
//                SyncMcpClient server = pair.getKey();
//                List<McpSchema.Tool> tools = pair.getValue();
//                prompt.append("## ").append(server.getName()).append(" (").append(getServerCommand(server)).append(")\n");
//                if (tools != null && !tools.isEmpty()) {
//                    prompt.append("\n### Available Tools\n");
//                    for (McpSchema.Tool tool : tools) {
//                        prompt.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
//                        if (tool.getInputSchema() != null) {
//                            prompt.append("    Input Schema:\n");
//                            prompt.append("    ").append(tool.getInputSchema().toString().replace("\n", "\n    ")).append("\n");
//                        }
//                    }
//                }
////                if (server.resourceTemplates != null && !server.resourceTemplates.isEmpty()) {
////                    prompt.append("\n### Resource Templates\n");
////                    for (ResourceTemplate template : server.resourceTemplates) {
////                        prompt.append("- ").append(template.uriTemplate).append(" (").append(template.name).append("): ").append(template.description).append("\n");
////                    }
////                }
////                if (server.resources != null && !server.resources.isEmpty()) {
////                    prompt.append("\n### Direct Resources\n");
////                    for (Resource resource : server.resources) {
////                        prompt.append("- ").append(resource.uri).append(" (").append(resource.name).append("): ").append(resource.description).append("\n");
////                    }
////                }
//            }
//        } else {
//            prompt.append("(No MCP servers currently connected)\n");
//        }
//        prompt.append("====\n");
//        // 此处省略了文件编辑、模式说明等部分，可根据需要添加完整
//        return prompt.toString();
//    }
//}
