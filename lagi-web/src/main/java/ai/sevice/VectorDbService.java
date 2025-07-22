//package ai.sevice;
//
//import ai.common.pojo.IndexSearchData;
//import ai.medusa.utils.LCS;
//import ai.openai.pojo.ChatCompletionRequest;
//import ai.servlet.dto.UserRagConfig;
//import ai.utils.PriorityWordUtil;
//import ai.utils.qa.ChatCompletionUtil;
//
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//public class VectorDbService {
//    private final VectorStoreService vectorStoreService;
//    private final UserRagConfigService userRagConfigService;
//
//    public VectorDbService(VectorStoreService vectorStoreService, UserRagConfigService userRagConfigService) {
//        this.vectorStoreService = vectorStoreService;
//        this.userRagConfigService = userRagConfigService;
//    }
//
//    public List<IndexSearchData> searchByContext(ChatCompletionRequest request, UserRagConfig config) {
//        List<IndexSearchData> searchResults = vectorStoreService.searchByContext(request, config);
//        String lastMessage = ChatCompletionUtil.getLastMessage(request);
//
//        List<IndexSearchData> filteredResults = searchResults.stream()
//                .filter(new java.util.function.Predicate<IndexSearchData>() {
//                    @Override
//                    public boolean test(IndexSearchData indexSearchData) {
//                        String text = indexSearchData.getText();
//                        Set<String> lcsSet = LCS.findLongestCommonSubstrings(lastMessage, text, 2);
//                        double ratio = LCS.getLcsRatio(lastMessage, lcsSet);
//                        return ratio > 0.1;
//                    }
//                })
//                .collect(Collectors.toList());
//
//        return PriorityWordUtil.sortByPriorityWord(filteredResults);
//    }
//
//}
