package ai.migrate.pojo;

import java.util.List;

import ai.openai.pojo.ChatMessage;

public class QuestionAnswerRequest {
	private String category;
	private Integer channelId;
	private List<ChatMessage> messages;

	private Boolean stream;

	public Boolean getStream() {
		return stream;
	}

	public void setStream(Boolean stream) {
		this.stream = stream;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getChannelId() {
		return channelId;
	}

	public void setChannelId(Integer channelId) {
		this.channelId = channelId;
	}

	public List<ChatMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<ChatMessage> messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		return "QuestionAnswerRequest [category=" + category + ", channelId=" + channelId + ", messages=" + messages + "]";
	}

}
