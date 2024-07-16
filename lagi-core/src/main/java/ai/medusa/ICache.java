package ai.medusa;


import ai.medusa.utils.PromptPool;

public interface ICache<K, V> {
    V get(K key);

    void put(K key, V value);

    void put(K key);

    int size();

    V locate(K key);

    PromptPool getPromptPool();

    void startProcessingPrompt();
}
