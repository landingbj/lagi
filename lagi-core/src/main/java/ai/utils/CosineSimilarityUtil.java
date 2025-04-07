package ai.utils;

import java.util.List;

public class CosineSimilarityUtil {

    // 新增方法：计算两个向量之间的余弦相似度
    public static double calculateCosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
