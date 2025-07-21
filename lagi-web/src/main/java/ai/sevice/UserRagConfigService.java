package ai.sevice;

import ai.dao.KnowledgeBaseRepository;
import ai.dao.UserRagConfigRepository;
import ai.servlet.dto.KnowledgeBase;
import ai.servlet.dto.UserRagConfig;

public class UserRagConfigService {
    private final UserRagConfigRepository userRagConfigRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;

    public UserRagConfigService(UserRagConfigRepository userRagConfigRepository, KnowledgeBaseRepository knowledgeBaseRepository) {
        this.userRagConfigRepository = userRagConfigRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
    }

    public UserRagConfig getConfig(String userId, String category) {
        KnowledgeBase kb;
        if (category == null) {
            // 如果未指定 category，使用活跃知识库
            kb = knowledgeBaseRepository.findByUserIdAndIsActive(userId, true);
            if (kb == null) {
                kb = knowledgeBaseRepository.findDefaultByUserId(userId); // 假设有默认知识库
            }
        } else {
            kb = knowledgeBaseRepository.findByUserIdAndCategory(userId, category);
        }
        
        if (kb == null) {
            return getDefaultConfig();
        }
        
        UserRagConfig config = userRagConfigRepository.findByUserIdAndKnowledgeBaseId(userId, kb.getId());
        return config != null ? config : getDefaultConfig();
    }

    private UserRagConfig getDefaultConfig() {
        UserRagConfig config = new UserRagConfig();
        config.setEnableFulltext(false);
        config.setEnableGraph(false);
        config.setEnableText2qa(false);
        config.setWenbenChunkSize(512);
        config.setBiaogeChunkSize(512);
        config.setTuwenChunkSize(512);
        config.setSimilarityTopK(5);
        config.setSimilarityCutoff(0.7);
        return config;
    }
}
