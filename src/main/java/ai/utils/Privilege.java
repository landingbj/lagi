package ai.utils;

import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Privilege   {
    public static final String NAME = "_NAME";
    public static final String PWDMD5 = "_PWDMD5";
    
    private final static int ENCODE_XORMASK = 0x5A;
    private final static char ENCODE_DELIMETER = '\002';
    private final static char ENCODE_CHAR_OFFSET1 = 'A';
    private final static char ENCODE_CHAR_OFFSET2 = 'h';


    
    public void doLogin(HttpServletRequest request,HttpServletResponse response, String username, String pwdMD5) {
        HttpSession session = request.getSession(true);
        session.setAttribute(NAME, username);
        session.setAttribute(PWDMD5, pwdMD5);
        String c = encodeCookie(username, pwdMD5);       
        try {
            c = URLEncoder.encode(c, "UTF-8");
        } catch (Exception e) {
            //ignore
        }
        Cookie cookie=new Cookie("docs.auth",c);
        cookie.setDomain(".landingbj.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }
    
    public static String encodeCookie(String username, String password)
    {
        StringBuffer buf = new StringBuffer();
        if (username != null && password != null)
        {
            byte[] bytes = (username + ENCODE_DELIMETER + password).getBytes();
            int b;
            
            for (int n = 0; n < bytes.length; n++)
            {
                b = bytes[n] ^ (ENCODE_XORMASK + n);
                buf.append((char)(ENCODE_CHAR_OFFSET1 + (b & 0x0F)));
                buf.append((char)(ENCODE_CHAR_OFFSET2 + ((b >> 4) & 0x0F)));
            }
        }
        return buf.toString();
    }
}
