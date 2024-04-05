package ai.migrate.pojo;

public class VoiceConvertEntity {
	private String category;

	private String audio_url;

	public String getAudio_url() {
		return audio_url;
	}

	public void setAudio_url(String audio_url) {
		this.audio_url = audio_url;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "VoiceConvertEntity [category=" + category + ", audio_url=" + audio_url + "]";
	}

}
