package ai.utils;


import ai.worker.pojo.WorkPriority;
import ai.worker.pojo.WorkPriorityNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkPriorityWordUtil {

    private static Map<String, AhoCorasick> ahoCorasickMap = null;

    static {
        WorkPriority workPriority = JsonFileLoadUtil.readWordLRulesList("/work_priority_word.json", WorkPriority.class);
        List<WorkPriorityNode> works = new ArrayList<>();
        if (workPriority != null) {
            works = workPriority.getWorks();
        }
        ahoCorasickMap = works.stream().collect(Collectors.toMap(WorkPriorityNode::getName, node -> {
            List<String> words = node.getWords();
            AhoCorasick ahoCorasick = new AhoCorasick();
            ahoCorasick.addWords(words);
            return ahoCorasick;
        }));
    }

    public static boolean isPriorityWord(String workName,  String question, String result) {
        if (ahoCorasickMap.containsKey(workName)) {
            AhoCorasick ahoCorasick = ahoCorasickMap.get(workName);
            return ahoCorasick.containsAny(question) && ahoCorasick.containsAny(result);
        }
        return false;
    }

}
