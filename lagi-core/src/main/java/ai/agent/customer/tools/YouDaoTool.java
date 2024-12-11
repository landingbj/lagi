package ai.agent.customer.tools;

import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import ai.common.utils.ObservableList;
import ai.utils.ApiInvokeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class YouDaoTool extends AbstractTool{

    // doc https://ai.youdao.com/DOCSIRMA/html/trans/api/dmxfy/index.html

    private String appId = "";
    private String appKey = "";
    private final String LLM_URL = "https://openapi.youdao.com/llm_trans";
    private final String API_URL = "https://openapi.youdao.com/api";
    private Gson gson = new Gson();

    public YouDaoTool(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
        init();
    }

    private void init() {
        name = "youdao_llm_trans_agent";
        toolInfo = ToolInfo.builder().name("youdao_llm_trans_agent")
                .description("这是一个基于大语言模型的智能翻译工具可以将源语言翻译为目标语言")
                .args(
                        Lists.newArrayList(ToolArg.builder().name("i").type("string").description("待翻译文本").build(),
                        ToolArg.builder().name("from").type("string").description("源语言,默认值auto").build(),
                        ToolArg.builder().name("to").type("string").description("目标语言, 取值列表： 阿拉伯语:ar,德语:de,英语:en,西班牙语:es,法语:fr,印地语:hi,印度尼西亚语:id,意大利语:it,日语:ja,韩语:ko,荷兰语:nl,葡萄牙语:pt,俄语:ru,泰语:th,越南语:vi,简体中文:zh-CHS,繁体中文:zh-CHT,南非荷兰语:af,阿姆哈拉语:am,阿塞拜疆语:az,白俄罗斯语:be,保加利亚语:bg,孟加拉语:bn,波斯尼亚语:bs,加泰隆语:ca,宿务语:ceb,科西嘉语:co,捷克语:cs,威尔士语:cy,丹麦语:da,希腊语:el,世界语:eo,爱沙尼亚语:et,巴斯克语:eu,波斯语:fa,芬兰语:fi,斐济语:fj,弗里西语:fy,爱尔兰语:ga,苏格兰盖尔语:gd,加利西亚语:gl,古吉拉特语:gu,豪萨语:ha,夏威夷语:haw,希伯来语:he,印地语:hi,克罗地亚语:hr,海地克里奥尔语:ht,匈牙利语:hu,亚美尼亚语:hy,伊博语:ig,冰岛语:is,爪哇语:jw,格鲁吉亚语:ka,哈萨克语:kk,高棉语:km,卡纳达语:kn,库尔德语:ku,柯尔克孜语:ky,拉丁语:la,卢森堡语:lb,老挝语:lo,立陶宛语:lt,拉脱维亚语:lv,马尔加什语:mg,毛利语:mi,马其顿语:mk,马拉雅拉姆语:ml,蒙古语:mn,马拉地语:mr,马来语:ms,马耳他语:mt,白苗语:mww,缅甸语:my,尼泊尔语:ne,荷兰语:nl,挪威语:no,齐切瓦语:ny,克雷塔罗奥托米语:otq,旁遮普语:pa,波兰语:pl,普什图语:ps,罗马尼亚语:ro,信德语:sd,僧伽罗语:si,斯洛伐克语:sk,斯洛文尼亚语:sl,萨摩亚语:sm,修纳语:sn,索马里语:so,阿尔巴尼亚语:sq,塞尔维亚语(西里尔文):sr-Cyrl,塞尔维亚语(拉丁文):sr-Latn,塞索托语:st,巽他语:su,瑞典语:sv,斯瓦希里语:sw,泰米尔语:ta,泰卢固语:te,塔吉克语:tg,菲律宾语:tl,克林贡语:tlh,汤加语:to,土耳其语:tr,塔希提语:ty,乌克兰语:uk,乌尔都语:ur,乌兹别克语:uz,南非科萨语:xh,意第绪语:yi,约鲁巴语:yo,尤卡坦玛雅语:yua,粤语:yue,南非祖鲁语:zu").build())
                ).build();
        register(this);
    }




    @Override
    public String apply(Map<String, Object> map) {
        String i = (String) map.get("i");
        String from = (String) map.get("from");
        String to = (String) map.get("to");
        String salt = UUID.randomUUID().toString();
        String curTime = String.valueOf(System.currentTimeMillis() / 1000);
        String input;
        if(i.length() > 20) {
            input = i.substring(0, 10) + i.length() + i.substring(i.length()-20);
        } else {
            input = i;
        }
        String dataToHash = appId + input + salt + curTime + appKey;
        String sign =  DigestUtil.sha256Hex(dataToHash);
        boolean llmSupport = false;
        if((from.equals("zh-CHS") || from.equals("zh-CHT") || to.equals("en"))
         && (to.equals("zh-CHS") || to.equals("zh-CHT") || to.equals("en"))) {
            llmSupport= true;
        }
        Map<String, String> request =  new HashMap<>();
        if(llmSupport) {
            request.put("i", i);
        } else {
            request.put("q", i);
        }
        request.put("from", from);
        request.put("to", to);
        request.put("streamType", "increment");
        request.put("appKey", appId);
        request.put("salt", salt);
        request.put("sign", sign);
        request.put("signType", "v3");
        request.put("curtime", curTime);
        try {
            Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
            if(llmSupport) {
                return llmTrans(request, type);
            } else {
                return apiTrans(request, type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String apiTrans(Map<String, String> request, Type type) {
        String post = ApiInvokeUtil.post(API_URL, null, request, 30, TimeUnit.SECONDS);
        Map<String, Object> res = gson.fromJson(post, type);
        int errorCode = Integer.parseInt((String) res.get("errorCode"));
        if(errorCode != 0) {
            return "翻译失败" + res.get("errorMsg");
        }
        List<String> translation = (List<String>)res.get("translation");
        return String.join("\n", translation);
    }

    private String llmTrans(Map<String, String> request, Type type) {
        ObservableList<String> result = ApiInvokeUtil.sse(LLM_URL, null, request, 30, TimeUnit.SECONDS, response -> {
            if(StrUtil.isBlank(response)) {
                return "";
            }
            Map<String, String> o = gson.fromJson(response, type);
            return o.getOrDefault("transIncre", "");
        });
        StringBuffer stringBuffer = result.getObservable().reduce(new StringBuffer(), StringBuffer::append).blockingGet();
        return stringBuffer.toString();
    }

}
