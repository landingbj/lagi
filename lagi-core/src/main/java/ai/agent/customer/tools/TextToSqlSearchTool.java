package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.utils.ExcelSqlUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;

import java.util.Map;

public class TextToSqlSearchTool extends AbstractTool {

    public TextToSqlSearchTool() {
        init();
    }

    private void init() {
        name = "text_to_sql_search";
        toolInfo = ToolInfo.builder().name("text_to_sql_search")
                .description("这是一个通过text_to_sql_search查询的工具配置类")
                .args(Lists.newArrayList(ToolArg.builder().name("demand").type("string").description("用户需求").build())).build();
        register(this);
    }

    private String search(String demand) {
        if (ExcelSqlUtil.isConnect()||ExcelSqlUtil.isSqlietConnect()){
            String sql1 = ExcelSqlUtil.WorkflowsToSql(demand);
            if (sql1!=null){
                String sql = ExcelSqlUtil.toSql(sql1,demand);
                String out = ExcelSqlUtil.toText(sql,demand, sql1);
                System.out.println(out);
                return StrUtil.format("{\"返回内容\": \"{}\"}",out);
            }else{
                return StrUtil.format("{\"返回内容\": \"{}\"}","数据库中暂无数据");
            }
        }
      return "mysql连接失败-智能问数模式已关闭";
    }


    @Override
    public String apply(Map<String, Object> args) {
        return search(args.get("demand").toString());
    }

    public static void main(String[] args) {
        TextToSqlSearchTool tool = new TextToSqlSearchTool();
        String result = tool.search("所有学生语文成绩总分是多少");
        System.out.println(result);
    }
}
