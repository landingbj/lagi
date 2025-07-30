package ai.agent.carbus.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteProcessor {

    /**
     * 处理路线规划数据，为每个方案生成包含新增字段的summary信息
     * @param routeData 路线规划数据（包含公交、骑行或步行方案）
     */
    public static void generateSummary(Map<String, Object> routeData) {
        // 1. 提取数据核心部分
        if (routeData == null) {
            return;
        }

        // 2. 确定方案列表（公交方案在transits中，骑行/步行在paths中）
        List<Map<String, Object>> plans = null;
        boolean isTransitPlan = routeData.containsKey("transits");
        if (isTransitPlan) {
            plans = (List<Map<String, Object>>) routeData.get("transits");
        } else if (routeData.containsKey("paths")) {
            plans = (List<Map<String, Object>>) routeData.get("paths");
        }
        if (plans == null || plans.isEmpty()) {
            return;
        }

        // 3. 遍历每个方案生成summary
        for (Map<String, Object> plan : plans) {
            Map<String, Object> summary = new HashMap<>();

            // 3.1 确定主要出行方式（mainType）
            String mainType = determineMainType(plan, isTransitPlan, routeData);
            summary.put("mainType", mainType);

            // 3.2 提取线路列表（lines）
            List<String> lines = extractLines(plan, isTransitPlan, mainType);
            summary.put("lines", lines);

            // 3.3 提取耗时（duration）
            String duration = extractDuration(plan, isTransitPlan, mainType);
            summary.put("duration", duration);

            // 3.4 提取费用（transit_fee）
            Object transitFee = extractTransitFee(plan, isTransitPlan, mainType);
            summary.put("transit_fee", transitFee);

            // 3.5 新增：提取总步行距离（walking_distance）
            String totalWalkingDistance = extractTotalWalkingDistance(plan, isTransitPlan);
            summary.put("walking_distance", totalWalkingDistance);

            // 3.6 新增：提取首次上车站点（first_departure_stop）
            String firstDepartureStop = extractFirstDepartureStop(plan, isTransitPlan, mainType);
            summary.put("first_departure_stop", firstDepartureStop);

            // 4. 将summary放入当前方案
            plan.put("summary", summary);
        }
    }

    /**
     * 确定主要出行方式
     */
    private static String determineMainType(Map<String, Object> plan, boolean isTransitPlan, Map<String, Object> routeData) {
        if (isTransitPlan) {
            // 公交/地铁方案：检查是否包含地铁
            List<Map<String, Object>> segments = (List<Map<String, Object>>) plan.get("segments");
            boolean hasSubway = false;
            boolean hasBus = false;

            if (segments != null) {
                for (Map<String, Object> segment : segments) {
                    Map<String, Object> busInfo = (Map<String, Object>) segment.get("bus");
                    if (busInfo != null) {
                        List<Map<String, Object>> busLines = (List<Map<String, Object>>) busInfo.get("buslines");
                        if (busLines != null && !busLines.isEmpty()) {
                            String lineType = (String) busLines.get(0).get("type");
                            if ("地铁线路".equals(lineType)) {
                                hasSubway = true;
                            } else {
                                hasBus = true; // 普通公交/快速公交
                            }
                        }
                    }
                }
            }

            if (hasSubway) {
                return "地铁";
            } else if (hasBus) {
                return "公交";
            }
        } else {
            // 骑行/步行方案：通过query判断
            String query = (String) routeData.get("query");
            if (query != null) {
                if (query.contains("骑行")) {
                    return "骑行";
                } else if (query.contains("步行")) {
                    return "步行";
                }
            }
        }
        return "未知";
    }

    /**
     * 提取线路列表
     */
    private static List<String> extractLines(Map<String, Object> plan, boolean isTransitPlan, String mainType) {
        List<String> lines = new ArrayList<>();
        // 仅处理公交/地铁方案
        if (isTransitPlan && ("地铁".equals(mainType) || "公交".equals(mainType))) {
            List<Map<String, Object>> segments = (List<Map<String, Object>>) plan.get("segments");
            if (segments != null) {
                for (Map<String, Object> segment : segments) {
                    Map<String, Object> busInfo = (Map<String, Object>) segment.get("bus");
                    if (busInfo != null) {
                        List<Map<String, Object>> busLines = (List<Map<String, Object>>) busInfo.get("buslines");
                        if (busLines != null && !busLines.isEmpty()) {
                            // 提取线路名称（取第一个线路）
                            String lineName = (String) busLines.get(0).get("name");
                            if (lineName != null) {
                                lines.add(lineName);
                            }
                        }
                    }
                }
            }
        }
        return lines;
    }

    /**
     * 提取耗时
     */
    private static String extractDuration(Map<String, Object> plan, boolean isTransitPlan, String mainType) {
        if (isTransitPlan) {
            // 公交/地铁：耗时在plan的cost中
            Map<String, Object> cost = (Map<String, Object>) plan.get("cost");
            if (cost != null) {
                return cost.get("duration").toString();
            }
        } else {
            if ("骑行".equals(mainType)) {
                // 骑行：直接在plan中
                return plan.get("duration").toString();
            } else if ("步行".equals(mainType)) {
                // 步行：耗时在plan的cost中
                Map<String, Object> cost = (Map<String, Object>) plan.get("cost");
                if (cost != null) {
                    return cost.get("duration").toString();
                }
            }
        }
        return "";
    }

    /**
     * 提取费用
     */
    private static Object extractTransitFee(Map<String, Object> plan, boolean isTransitPlan, String mainType) {
        // 仅公交/地铁有费用
        if (isTransitPlan && ("地铁".equals(mainType) || "公交".equals(mainType))) {
            Map<String, Object> cost = (Map<String, Object>) plan.get("cost");
            if (cost != null) {
                return cost.get("transit_fee");
            }
        }
        return null; // 骑行/步行无费用
    }

    /**
     * 新增：提取总步行距离
     * 规则：公交/地铁方案取plan中的walking_distance；骑行/步行方案取总距离（因全程为该方式）
     */
    private static String extractTotalWalkingDistance(Map<String, Object> plan, boolean isTransitPlan) {
        if (isTransitPlan) {
            // 公交/地铁方案：直接读取walking_distance字段
            return plan.get("walking_distance") != null ? plan.get("walking_distance").toString() : "0";
        } else {
            // 骑行/步行方案：总距离即该方式的全程距离
            return plan.get("distance") != null ? plan.get("distance").toString() : "0";
        }
    }

    /**
     * 新增：提取首次上车站点
     * 规则：仅公交/地铁方案有效，取第一段公交/地铁的出发站点名称；其他方式为null
     */
    private static String extractFirstDepartureStop(Map<String, Object> plan, boolean isTransitPlan, String mainType) {
        if (isTransitPlan && ("地铁".equals(mainType) || "公交".equals(mainType))) {
            List<Map<String, Object>> segments = (List<Map<String, Object>>) plan.get("segments");
            if (segments != null && !segments.isEmpty()) {
                // 遍历 segments 找到第一个包含 bus 信息的段
                for (Map<String, Object> segment : segments) {
                    Map<String, Object> busInfo = (Map<String, Object>) segment.get("bus");
                    if (busInfo != null) {
                        List<Map<String, Object>> busLines = (List<Map<String, Object>>) busInfo.get("buslines");
                        if (busLines != null && !busLines.isEmpty()) {
                            // 提取该线路的出发站点名称
                            Map<String, Object> departureStop = (Map<String, Object>) busLines.get(0).get("departure_stop");
                            if (departureStop != null) {
                                return (String) departureStop.get("name");
                            }
                        }
                    }
                }
            }
        }
        return null; // 非公交/地铁方案或无站点信息
    }
}