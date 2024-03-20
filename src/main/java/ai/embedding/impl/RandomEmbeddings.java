package ai.embedding.impl;

import ai.embedding.Embeddings;
import ai.migrate.pojo.EmbeddingConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEmbeddings implements Embeddings {
    public RandomEmbeddings(EmbeddingConfig config) {
    }

    public List<List<Float>> createEmbedding(List<String> docs) {
        List<List<Float>> result = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            result.add(generateRandomDoubleList(1024));
        }
        return result;
    }

    @Override
    public List<Float> createEmbedding(String doc) {
        return generateRandomDoubleList(1024);
    }

    private List<Float> generateRandomDoubleList(int size) {
        List<Float> randomDoubles = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            float randomDouble = random.nextFloat() * 10;
            randomDoubles.add(randomDouble);
        }
        return randomDoubles;
    }
}
