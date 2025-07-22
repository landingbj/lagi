package ai.sevice;

import ai.common.pojo.KnowledgeBase;
import ai.common.pojo.VectorStoreConfig;
import ai.config.ContextLoader;
import ai.dao.KnowledgeBaseDao;
import ai.migrate.db.Conn;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KnowledgeBaseService {

    private final KnowledgeBaseDao knowledgeBaseDao;

    public KnowledgeBaseService() {
        this.knowledgeBaseDao = new KnowledgeBaseDao(new Conn());
    }

    public boolean insert(KnowledgeBase kb) throws Exception {
        long time = new Date().getTime();
        kb.setCreateTime(time);
        kb.setUpdateTime(time);
        boolean aPublic = kb.isPublic();
        if(aPublic) {
            this.knowledgeBaseDao.unPublicOtherKnowledgeBase(kb.getUserId(), kb.getRegion());
        }
        return this.knowledgeBaseDao.insert(kb);
    }

    public List<KnowledgeBase> getByUserRegion(String userId, String region) throws Exception {
        return this.knowledgeBaseDao.getByUserRegion(userId, region);
    }

    public KnowledgeBase getById(Long id) {
        if(id == null) {
            return null;
        }
        try {
            return this.knowledgeBaseDao.getById(id);
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean deleteById(Long id) throws Exception {
        return this.knowledgeBaseDao.deleteById(id);
    }

    public boolean update(KnowledgeBase kb) throws Exception {
        kb.setUpdateTime(new Date().getTime());
        kb.setCreateTime(null);
        boolean aPublic = kb.isPublic();
        if(aPublic) {
            this.knowledgeBaseDao.unPublicOtherKnowledgeBase(kb.getUserId(), kb.getRegion());
        }
        return this.knowledgeBaseDao.update(kb);
    }

    public KnowledgeBase getLatestPublicKnowledgeBase(String useId, String region) throws Exception {
        KnowledgeBase latestPublicKnowledgeBase = this.knowledgeBaseDao.getLatestPublicKnowledgeBase(useId, region);
        if (latestPublicKnowledgeBase != null) {
            return this.knowledgeBaseDao.getLatestPublicKnowledgeBase(useId, region);
        }
        return this.knowledgeBaseDao.getFirstKnowledgeBase(useId, region);
    }

    public String getRegion()  {
        Map<String, String> map = ContextLoader.configuration.getStores().getVectors().stream()
                .collect(Collectors.toMap(VectorStoreConfig::getName, VectorStoreConfig::getDefaultCategory));
        String vector = ContextLoader.configuration.getStores().getRag().getVector();
        return map.get(vector);
    }

}
