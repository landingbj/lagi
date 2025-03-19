package ai.openai.pojo;

import ai.learning.pojo.IndexSearchData;

import java.util.List;

public class ChatRequestWithContext {
    private List<ChatMessage> messages;
    private IndexSearchData IndexSearchData;

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public IndexSearchData getIndexSearchData() {
        return IndexSearchData;
    }

    public void setIndexSearchData(IndexSearchData indexSearchData) {
        IndexSearchData = indexSearchData;
    }

    @Override
    public String toString() {
        return "ChatRequestWithContext [messages=" + messages + ", IndexSearchData=" + IndexSearchData + "]";
    }
}
