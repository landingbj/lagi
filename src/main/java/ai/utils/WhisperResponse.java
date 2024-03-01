package ai.utils;

public class WhisperResponse {
	private Integer code;
    private String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public WhisperResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "WhisperResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
