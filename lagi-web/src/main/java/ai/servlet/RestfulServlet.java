package ai.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import ai.servlet.annotation.Body;
import ai.servlet.annotation.Get;
import ai.servlet.annotation.Param;
import ai.servlet.annotation.Post;
import ai.servlet.exceptions.RRException;
import ai.response.RestfulResponse;
import ai.utils.StringUtils;
import cn.hutool.core.convert.Convert;

/**
 * @program: RestfulServlet
 * @description: restful response servlet
 * @author: linzhen
 * @create: 2023-06-29 09:00
 **/
public class RestfulServlet extends BaseServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Map<String, Method> registerGetMethod = new ConcurrentHashMap<>();
    private Map<String, Method> registerPostMethod = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        Class<? extends RestfulServlet> clazz = this.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Get.class)) {
                Get get = method.getAnnotation(Get.class);
                registerGetMethod.put(get.value(), method);
                continue;
            }
            if (method.isAnnotationPresent(Post.class)) {
                Post get = method.getAnnotation(Post.class);
                registerPostMethod.put(get.value(), method);
                continue;
            }
        }
    }


    private void doRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, Method> map)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        try {
            Method mth = map.get(method);
            if (mth == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // 动态入参数
            Parameter[] parameters = mth.getParameters();
            Object[] pInst = parseParams(req, resp, parameters);
            Object o = mth.invoke(this, pInst);
            // 返回值是void 不包装结果
            if (!mth.getReturnType().equals(Void.TYPE)) {
                responsePrint(resp, toJson(RestfulResponse.sucecced(o)));
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            responsePrint(resp, toJson(RestfulResponse.error("服务器内部错误请联系管理员-001")));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            responsePrint(resp, toJson(RestfulResponse.error("服务器内部错误请联系管理员-002")));
        } catch (InvocationTargetException e) {

            // do not care ServletException and IOException
            if (e.getCause() instanceof ServletException) {
                throw (ServletException) e.getCause();
            } else if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else if (e.getCause() instanceof RRException) {
                RRException re = (RRException) e.getCause();
                responsePrint(resp, toJson(RestfulResponse.error(re.getMsg())));
                re.printStackTrace();
                return;
            }
            e.printStackTrace();
            responsePrint(resp, toJson(RestfulResponse.error("服务器内部错误请联系管理员-003")));
        }
    }

    private Object[] parseParams(HttpServletRequest req, HttpServletResponse resp, Parameter[] params) {
        List<Object> pIns = new ArrayList<>(params.length);
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            if (param.getType() == HttpServletRequest.class) {
                pIns.add(req);
                continue;
            }
            if (param.getType() == HttpServletResponse.class) {
                pIns.add(resp);
                continue;
            }
            if (param.isAnnotationPresent(Param.class)) {
                Param p = param.getAnnotation(Param.class);
                String pStr = req.getParameter(p.value());
                if (StringUtils.isBlank(pStr)) {
                    pStr = p.def();
                }
                Object o = convertQuery(pStr, param.getType());
                pIns.add(o);
                continue;
            }
            if (param.isAnnotationPresent(Body.class)) {
                pIns.add(convertBody(req, param.getType()));
                continue;
            }

            pIns.add(null);
        }
        return pIns.toArray();
    }

    private Object convertQuery(String pStr, Class<?> type) {
        try {
            if (type == Integer.class) {
                return Integer.parseInt(pStr);
            }
            if (type == Double.class) {
                return Double.parseDouble(pStr);
            }
            if (type == Long.class) {
                return Long.parseLong(pStr);
            }
            if (type == String.class) {
                return pStr;
            }
            return Convert.convert(type, pStr);
        } catch (Exception e) {
        }
        return null;
    }

    private Object convertBody(HttpServletRequest req, Class<?> type) {
        try {
            return reqBodyToObj(req, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doRequest(req, resp, registerGetMethod);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doRequest(req, resp, registerPostMethod);
    }


}
