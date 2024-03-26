package ai.servlet.exceptions;

/**
* @program: RRException
*
* @description: restful response exception
*
* @author: linzhen
*
* @create: 2023-06-29 09:00
**/
public class RRException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	private int code = 500;
	private String msg;
	
	public RRException() {
		super();
	}
	
	
	public RRException(String msg) {
		this.msg = msg;
	}
	
	public RRException(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
	

}
