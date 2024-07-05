package ai.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Text2VoiceEntity {
	private String model;
	private String emotion;
	private String text;
	private String category;
    private String source;

    private String appkey;
    private String token;
    private String format;
    private Integer sample_rate;
    private String voice;
    private Integer volume;
    private Integer speech_rate;
    private Integer pitch_rate;



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((emotion == null) ? 0 : emotion.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Text2VoiceEntity other = (Text2VoiceEntity) obj;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        if (emotion == null) {
            if (other.emotion != null)
                return false;
        } else if (!emotion.equals(other.emotion))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }
}
