package ai.rerank;

import ai.learn.questionAnswer.KShingleFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RerankTest {

    public static void main(String[] args) throws IOException {
        String wrong = "故障分类:LSI软件故障编号82位号:4002故障名称:外部闪存测试失败解决方法及建议:重新刷写DCB控制板程序，如果失败，更换DCB控制板。";
        String result = "根据提供的背景信息，外部闪存测试失败是指在进行LSI软件故障编号82位号4002的故障诊断时，检测到外部闪存（一种用于存储数据的硬件设备）的测试未能通过。解决此问题的方法是首先尝试重新刷写DCB（直接存储器访问）控制板的程序。如果这一步骤无法解决问题，那么建议更换DCB控制板。";
        int k = result.length();
        double threshold = 0.3;
        double frequencyThreshold = 0.5;
        KShingleFilter kShingleFilter = new KShingleFilter(k, threshold, frequencyThreshold);
        boolean similar = kShingleFilter.isSimilar(result, wrong);
        System.out.println("wrong:" +  similar);
    }
}
