package ai.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.annotation.Get;
import ai.annotation.Param;
import ai.dto.Prompt;

public class PageInfoServlet extends RestfulServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static List<Prompt> prompts = new ArrayList<>(10);
	
	static{
		prompts.add(new Prompt("内容撰写","请为一位大学生写一篇产品助理岗位的实习报告"	));
		prompts.add(new Prompt("内容改写","请帮我把这段内容改写：“天青色等烟雨，而我在等你”"));
		prompts.add(new Prompt("旅游规划","请帮我制定一个新疆7天游，预算8888元的旅游攻略"));
		prompts.add(new Prompt("产品推荐","请给我推荐几辆预算15万以内的新能源小汽车，并说明它们的优缺点"));
		prompts.add(new Prompt("智能百科","兔子的耳朵为什么那么长？"));
		prompts.add(new Prompt("家常菜谱","请告诉我红烧猪蹄的做法和注意事项"));
		prompts.add(new Prompt("社恐交际","应酬时想以茶代酒该怎么高情商发言？"));
		prompts.add(new Prompt("“社恐”交际","如何不带脏字的骂人？"));
		
	}
	
	public static List<Prompt> randomPrompts(int size) {
		size = size > prompts.size() ? prompts.size() :size;
		List<Prompt> res = new ArrayList<>(prompts);
		Collections.shuffle(res);
		return res.subList(0, size);
	}
	
	@Get("getPrompts")
	public List<Prompt> getPromptList(@Param("size") Integer size) {
		return randomPrompts(size);
	}
	
}
