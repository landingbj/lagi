package ai.servlet;

import java.util.*;

import ai.common.pojo.Configuration;
import ai.servlet.annotation.Get;
import ai.servlet.annotation.Param;
import ai.servlet.dto.Prompt;
import ai.utils.MigrateGlobal;

public class PageInfoServlet extends RestfulServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static List<Prompt> prompts = new ArrayList<>(10);
	private static final Configuration config = MigrateGlobal.config;
	private static final Map<String, Integer> navMap = new HashMap<>(20);

	static{
		prompts.add(new Prompt("内容撰写","请为一位大学生写一篇产品助理岗位的实习报告"	));
		prompts.add(new Prompt("内容改写","请帮我把这段内容改写：“天青色等烟雨，而我在等你”"));
		prompts.add(new Prompt("旅游规划","请帮我制定一个新疆7天游，预算8888元的旅游攻略"));
		prompts.add(new Prompt("产品推荐","请给我推荐几辆预算15万以内的新能源小汽车，并说明它们的优缺点"));
		prompts.add(new Prompt("智能百科","兔子的耳朵为什么那么长？"));
		prompts.add(new Prompt("家常菜谱","请告诉我红烧猪蹄的做法和注意事项"));
		prompts.add(new Prompt("社恐交际","应酬时想以茶代酒该怎么高情商发言？"));
		prompts.add(new Prompt("“社恐”交际","如何不带脏字的骂人？"));

		// 智能问答 配置检测

		long llmCount = config.getLLM().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("znwd", llmCount > 0 ? 1 :0);
		navMap.put("wbsc", llmCount > 0 ? 1 :0);
		long count = config.getASR().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("yysb", count > 0 ? 1 :0);
		count = config.getTTS().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("qrqs", count > 0 ? 1 :0);
		count = config.getImageCaptioning().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("ktsh", count > 0 ? 1 :0);
		count = config.getImageEnhance().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("hzzq", count > 0 ? 1 :0);
		count = config.getImageGeneration().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("tpsc", count > 0 ? 1 :0);
		count = config.getVideoTrack().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("spzz", count > 0 ? 1 :0);
		count = config.getVideoEnhance().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("spzq", count > 0 ? 1 :0);
		count = config.getVideoGeneration().getBackends().stream().filter(backend -> {
			return backend.getEnable();
		}).count();
		navMap.put("spsc", count > 0 ? 1 :0);
		navMap.put("kjsx", config.getVectorStore() != null  ? 1 :0);
		// aiservice
		navMap.put("zlsc", 1);

		navMap.put("twhp", llmCount > 0 ? 1 :0);
	}
	
	public static List<Prompt> randomPrompts(int size) {
		size = Math.min(size, prompts.size());
		List<Prompt> res = new ArrayList<>(prompts);
		Collections.shuffle(res);
		return res.subList(0, size);
	}
	
	@Get("getPrompts")
	public List<Prompt> getPromptList(@Param("size") Integer size) {
		return randomPrompts(size);
	}

	@Get("getNavStatus")
	public Map<String, Integer> getNavStatus() {
		return navMap;
	}
	
}
