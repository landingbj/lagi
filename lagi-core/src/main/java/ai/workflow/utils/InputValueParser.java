package ai.workflow.utils;

import ai.workflow.pojo.WorkflowContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 输入值解析器
 * 处理三种类型的输入值：
 * 1. constant - 常量值
 * 2. template - 模板值（支持变量替换）
 * 3. ref - 引用值（从节点结果中获取）
 */
public class InputValueParser {
    
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    
    /**
     * 解析输入值配置
     * @param inputsValues 输入值配置
     * @param context 工作流上下文
     * @return 解析后的输入值映射
     */
    public static Map<String, Object> parseInputs(JsonNode inputsValues, WorkflowContext context) {
        Map<String, Object> inputs = new HashMap<>();
        
        if (inputsValues == null) {
            return inputs;
        }
        
        inputsValues.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            
            String type = value.get("type").asText();
            JsonNode content = value.get("content");
            
            Object resolvedValue = resolveValue(type, content, context);
            if (resolvedValue != null) {
                inputs.put(key, resolvedValue);
            }
        });
        
        return inputs;
    }
    
    /**
     * 根据类型解析值
     * @param type 值类型 (constant, template, ref)
     * @param content 内容节点
     * @param context 工作流上下文
     * @return 解析后的值
     */
    private static Object resolveValue(String type, JsonNode content, WorkflowContext context) {
        switch (type) {
            case "template":
                return resolveTemplate(content, context);
            case "ref":
                return resolveRef(content, context);
            default:
                return resolveConstant(content);
        }
    }
    
    /**
     * 解析常量值
     * @param content 常量内容节点
     * @return 常量值
     */
    private static Object resolveConstant(JsonNode content) {
        if (content.isTextual()) {
            return content.asText();
        } else if (content.isNumber()) {
            return content.asDouble();
        } else if (content.isBoolean()) {
            return content.asBoolean();
        } else {
            return content.asText();
        }
    }
    
    /**
     * 解析模板值（支持变量替换）
     * @param content 模板内容节点
     * @param context 工作流上下文
     * @return 解析后的模板值
     */
    private static String resolveTemplate(JsonNode content, WorkflowContext context) {
        if (content == null || !content.isTextual()) {
            return null;
        }
        
        String template = content.asText();
        if (template == null) {
            return null;
        }
        
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String reference = matcher.group(1);
            String[] parts = reference.split("\\.");
            
            if (parts.length == 2) {
                String nodeId = parts[0];
                String field = parts[1];
                
                Object nodeResult = context.getNodeResult(nodeId);
                if (nodeResult instanceof Map) {
                    Object value = ((Map<?, ?>) nodeResult).get(field);
                    if (value != null) {
                        matcher.appendReplacement(result, value.toString());
                    }
                }
            } else if (parts.length == 3) {
                String field = parts[1];
                String subField = parts[2];
                Object variable = context.getVariable(field);
                if (variable instanceof Map) {
                    Object subValue = ((Map<?, ?>) variable).get(subField);
                    if (subValue != null) {
                        matcher.appendReplacement(result, subValue.toString());
                    }
                }
            }
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 解析引用值（从节点结果中获取）
     * @param content 引用内容节点（数组格式：[nodeId, fieldName]）
     * @param context 工作流上下文
     * @return 引用的值
     */
    private static Object resolveRef(JsonNode content, WorkflowContext context) {
        if (content == null || !content.isArray() || content.size() != 2) {
            return null;
        }
        
        String nodeId = content.get(0).asText();
        String field = content.get(1).asText();
        
        Object nodeResult = context.getNodeResult(nodeId);
        if (nodeResult instanceof Map) {
            return ((Map<?, ?>) nodeResult).get(field);
        }
        
        return null;
    }
} 