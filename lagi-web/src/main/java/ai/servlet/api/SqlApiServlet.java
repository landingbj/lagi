package ai.servlet.api;

import ai.database.impl.MysqlAdapter;
import ai.database.pojo.TableColumnInfo;
import ai.dto.SqlToTextRequest;
import ai.dto.TextToSqlRequest;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.servlet.annotation.Post;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SqlApiServlet extends BaseServlet {
     private static final long serialVersionUID = 1L;
    protected Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        if (method.equals("text2sql")) {
            this.textToSql(req, resp);
        }else if (method.equals("sql2text")) {
            this.sqlToText(req, resp);
        }

    }
        @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

     @Post("textToSql")
    public void textToSql(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");

        String jsonString = IOUtils.toString(req.getInputStream(),
                StandardCharsets.UTF_8);
        TextToSqlRequest qaRequest = gson.fromJson(jsonString, TextToSqlRequest.class);

        String demand = qaRequest.getDemand();
        String out = toSql(demand,qaRequest.getTableName());
           System.out.println("out1的回答是："+out);
        String outcome =  extractContentWithinBraces(out);
        qaRequest.setText(out);
        qaRequest.setSql(outcome);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "success");
        result.put("data", qaRequest);
        responsePrint(resp, toJson(result));
    }

    @Post("sqlToText")
    public void sqlToText(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String jsonString = IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8);
        Map<String, Object> result = new HashMap<String, Object>();
        SqlToTextRequest qaRequest = gson.fromJson(jsonString, SqlToTextRequest.class);

        String out = qaRequest.getSql();

        String outcome =  "";
       List<Map<String, Object>> list = new ArrayList<>();
         Gson gson = new Gson();
            if (out != null){
                outcome =  extractContentWithinBraces(out);
                 //System.out.println("outcome:" + outcome);
                String sql = extractContentWithinBraces(outcome);
                System.out.println("sql:" + sql);
                try {
                    list = new MysqlAdapter().sqlToValue(sql);
                }catch (Exception e){
                    list = new ArrayList<>();
                }

                String msg = toText(qaRequest.getDemand(),gson.toJson(list),qaRequest.getTableName());

                result.put("status", "success");
                result.put("data", msg);
                result.put("list",list);
            }else {
             result.put("status", "failed");
             result.put("data", "The SQL is null");
            }
        responsePrint(resp, toJson(result));

    }



      public String toSql(String demand,String tableNeam) {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("现在你是一个数据分析师,MYSQL大神,请根据用户提供的表的信息，以及用户的需求，写出效率最高的SQL," +
                "当涉及到中文字段的时候你比较喜欢使用模糊查询." +
                "表信息如下："+
                tableParsing(tableNeam)+
                "输并且要求输出的S0L以#开头,以#结尾，样例如下:" +
                "{SELECT * FROM "+tableNeam+" ;}  " +
                 "{SELECT * FROM "+tableNeam+" WHERE city LIKE '%北京%' tier = '%档位一%';}" +
                "{SELECT COUNT(*)FROM "+tableNeam+";}" +
                "注意不需要分析过程，" +
                "用户需求:" + demand
        );
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        chatCompletionRequest.setStream(false);
        CompletionsService completionsService = new CompletionsService();
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String out = null;
          if (result != null) {
              out = result.getChoices().get(0).getMessage().getContent();
          }
        return out;
    }

    public String tableParsing(String tableNeam) {
      List<TableColumnInfo> list = new MysqlAdapter().getTableColumnInfo(tableNeam);
               StringBuilder tableresult = new StringBuilder();
                Map<String, List<TableColumnInfo>> groupedByTable = list.stream()
                   .collect(Collectors.groupingBy(TableColumnInfo::getTableName));

            for (Map.Entry<String, List<TableColumnInfo>> entry : groupedByTable.entrySet()) {
                String tableName = entry.getKey();
                List<TableColumnInfo> columns = entry.getValue();
                tableresult.append("表名为: ").append(tableName).append(";\n");
               for (TableColumnInfo column : columns) {
                    tableresult.append(String.format("字段: %s %s",
                                                column.getColumnName(),
                                                column.getColumnType()));
                    if (!column.getColumnRemark().isEmpty()) {
                        tableresult.append(" 备注为: ").append(column.getColumnRemark());
                    }
                    tableresult.append("；\n");
                }
                tableresult.append("\n");
            }
           return tableresult.toString();
    }

     public String toText(String demand,String outMsg,String tableNeam) {

        //mock request
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        chatCompletionRequest.setCategory("default");
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent("现在你是一个数据分析师,请根据用户提供的表的信息，用户的需求，以及你查询数据库返回的信息，给客户一个满意的回答." +
                "表信息如下："+
                tableParsing(tableNeam)+
                "用户需求:“" + demand+"”。" +
                "你结合需求查数据库返回的信息的信息是：“"+outMsg+
                "”注意不需要分析过程。");
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        chatCompletionRequest.setStream(false);
        CompletionsService completionsService = new CompletionsService();
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        String out = null;
        if(result != null){
            out = result.getChoices().get(0).getMessage().getContent();
        }
        return out;
    }



    public static String extractContentWithinBraces(String input) {
        if (input == null || !input.contains("#") || !input.contains("#")) {
            return input;
        }
        input = input.replace("\n", " ");
        if (input.matches(".*#.*#.*")) {
            int startIndex = input.indexOf("#");
            int endIndex = input.indexOf("#", startIndex + 1);

             if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return input.substring(startIndex + 1, endIndex);
            } else {
                System.out.println("只找到了一个 # 字符");
            }
        }
        return input;
    }

    @Test
    public void mysql() {
        List<Map<String, Object>> list = new MysqlAdapter().sqlToValue("SELECT * FROM hotel_agreement;");
        Gson gson = new Gson();

        for (Object o : list) {
            System.out.println(gson.toJson(o));
        }
    }
}
