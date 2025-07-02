package ai.pnps.pojo;

public class ChannelUser {
    private Integer channelId;
    private String username;
    private String nickname;

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "ChannelUser [channelId=" + channelId + ", username=" + username + ", nickname=" + nickname + "]";
    }

}
