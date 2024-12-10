package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolInfo;
import com.google.common.collect.Lists;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CalendarTool extends AbstractTool{

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public CalendarTool() {
        init();
    }

    private void init() {
        name = "calendar_tool";
        toolInfo = ToolInfo.builder().name("calendar_tool")
                .description("这是日历工具它可以帮助你获取通过它来获取今天的日期")
                .args(Lists.newArrayList(
                ))
                .build();
        register(this);
    }
    @Override
    public String apply(Map<String, Object> map) {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
