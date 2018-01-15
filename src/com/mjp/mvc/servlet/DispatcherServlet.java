package com.mjp.mvc.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mjp.mvc.annotation.*;
import com.mjp.mvc.model.HandlerModel;
import com.mjp.mvc.model.PatternResult;
import com.mjp.mvc.util.Play;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: majunpeng
 * @Description: 入口Servlet
 * @Date: 2018/1/12 15:02
 */
public class DispatcherServlet extends HttpServlet {

    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> instanceMapping = new HashMap<String, Object>();
    private Map<String, HandlerModel> handlerMapping = new HashMap<String, HandlerModel>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("init servlet");
        System.out.println("scanPackage:" + config.getInitParameter("scanPackage"));
        //扫包
        System.out.println("start scanPackage");
        scanPackage(config.getInitParameter("scanPackage"));
        System.out.println("end scanPackage, class size:" + classNames.size());
        //实例化
        doInstance();
        //注入
        doAutowired();
        //匹配url
        doHandlerMapping();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("enter get method");
        try {
            PatternResult result = pattern(req, resp);
            if (result.getCode() != 200) {
                out(resp, "404 not found", "application/text;charset=utf-8");
            } else {
                out(resp, result.getData(), "application/json;charset=utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out(resp, "500 Exception\n" + e.getMessage(),"application/text;charset=utf-8");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("enter post method");
        try {
            PatternResult result = pattern(req, resp);
            if (result.getCode() != 200) {
                out(resp, "404 not found", "application/text;charset=utf-8");
            } else {
                out(resp, result.getData(), "application/json;charset=utf-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out(resp, "500 Exception\n" + e.getMessage(),"application/text;charset=utf-8");
        }
    }

    private void out(HttpServletResponse resp, String str, String contentType) {
        try {
            resp.setContentType(contentType);
            resp.getWriter().print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描包
     * @param pkName
     */
    private void scanPackage(String pkName) {
        String dirStr = "/" + pkName.replaceAll("\\.", "/");
        URL url = getClass().getClassLoader().getResource(dirStr);
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                scanPackage(pkName + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = pkName + "." + file.getName().replaceAll(".class", "");
                try {
                    Class clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {
                        classNames.add(className);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 实例化Controller和Service
     */
    private void doInstance() {
        System.out.println("start instance");
        if (classNames.size() == 0) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Controller controller = clazz.getAnnotation(Controller.class);
                    String value = controller.value();
                    if (!"".equals(value.trim())) {
                        instanceMapping.put(value, clazz.newInstance());
                    } else {
                        instanceMapping.put(lowerFirstChar(clazz.getSimpleName().replace("Impl", "")), clazz.newInstance());
                    }
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    String value = service.value();
                    if (!"".equals(value.trim())) {
                        instanceMapping.put(value, clazz.newInstance());
                    } else {
                        instanceMapping.put(lowerFirstChar(clazz.getSimpleName().replace("Impl", "")), clazz.newInstance());
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        System.out.println("end instance, instance size:" + instanceMapping.size());
    }

    /**
     * 注入Autowired标注的属性
     */
    private void doAutowired() {
        for (String key: instanceMapping.keySet()) {
            Object instance = instanceMapping.get(key);
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field: fields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String beanName = "";
                    if ("".equals(autowired.value())) {
                        beanName = lowerFirstChar(field.getType().getSimpleName());
                    } else {
                        beanName = autowired.value();
                    }
                    //设置私有属性的访问权限为true
                    field.setAccessible(true);
                    if (instanceMapping.get(beanName) != null) {
                        try {
                            field.set(instance, instanceMapping.get(beanName));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 匹配controller url
     */
    private void doHandlerMapping() {
        if (instanceMapping.isEmpty()) {
            return;
        }

        //遍历托管对象集合
        for (String key: instanceMapping.keySet()) {
            Object instance = instanceMapping.get(key);
            Class<?> clazz = instance.getClass();
            //只取controller注解的类来进行url映射匹配
            if (clazz.isAnnotationPresent(Controller.class)) {
                //拼接url
                String url = "";
                //如果controller上有requestMapping，则拼接
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    url += "/" + clazz.getAnnotation(RequestMapping.class).value();
                }
                //获得所有public方法（包含超类）
                Method[] methods = clazz.getMethods();
                for (Method method: methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        String methodUrl = url + "/" + method.getAnnotation(RequestMapping.class).value();
                        //获得方法的参数名
                        String[] paramNames = Play.getMethodParameterNamesByAsm4(clazz, method);
                        //获取方法的参数注解,二维数组，因为多个参数且每个参数注解可为多个
                        Annotation[][] annotations2w = method.getParameterAnnotations();
                        //获取方法参数类型
                        Class<?>[] paramTypes = method.getParameterTypes();
                        //参数map，key：参数名，value：参数位置，0开始
                        Map<String, Integer> paramMaps = new HashMap<String, Integer>();
                        for (int i = 0; i < annotations2w.length; i++) {
                            Annotation[] annotations = annotations2w[i];
                            if (annotations.length == 0) {
                                //如果方法参数没有注解（RequestParam），则入String abc 则参数名为abc，req和resp则用类名
                                Class<?> type = paramTypes[i];
                                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                                    paramMaps.put(type.getName(), i);
                                } else {
                                    paramMaps.put(paramNames[i], i);
                                }
                            } else {
                                //如果方法参数有注解，则获取RequestParam注解的value值，没有则用属性名
                                for (Annotation an: annotations) {
                                    if (an.annotationType() == RequestParam.class) {
                                        String name = ((RequestParam) an).value();
                                        if (!"".equals(name.trim())) {
                                            paramMaps.put(name, i);
                                        } else {
                                            paramMaps.put(paramNames[i], i);
                                        }
                                    }
                                }
                            }
                        }
                        HandlerModel model = new HandlerModel(instance, method, paramMaps);
                        handlerMapping.put(methodUrl.replaceAll("/+", "/"), model);
                    }
                }
            }
        }
    }

    private PatternResult pattern(HttpServletRequest request, HttpServletResponse response) {
        PatternResult result = new PatternResult();

        if (handlerMapping.isEmpty()) {
            result.setCode(404);
            result.setData("404 not found");
            return result;
        }

        String realUrl = request.getRequestURI().replaceAll(request.getContextPath(), "").replaceAll("/+", "/");
        for (String url: handlerMapping.keySet()) {
            if (realUrl.equals(url)) {
                HandlerModel model = handlerMapping.get(url);
                Object controller = model.getObject();
                Method method = model.getMethod();
                Map<String, Integer> params = model.getParams();

                //方法参数值
                Object[] paramValues = new Object[params.size()];
                //方法参数类型
                Class<?>[] paramTypes = method.getParameterTypes();

                for (String name: params.keySet()) {
                    int index = params.get(name);
                    if (name.equals(request.getClass().getName())) {
                        paramValues[index] = request;
                    } else if (name.equals(response.getClass().getName())) {
                        paramValues[index] = response;
                    } else {
                        String realValue = request.getParameter(name);
                        if (null != realValue) {
                            paramValues[index] = convert(realValue, paramTypes[index]);
                        }
                    }
                }
                //反射调用
                try {
                    Object data = method.invoke(controller, paramValues);
                    String jsonString = JSON.toJSONString(data);
                    result.setCode(200);
                    result.setData(jsonString);
                    return result;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        result.setCode(404);
        result.setData("404 not found");
        return result;
    }

    private Object convert(String originValue, Class<?> type) {
        if (type == String.class) {
            return originValue;
        } else if (type == Integer.class || type == int.class) {
            return Integer.valueOf(originValue);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.valueOf(originValue);
        } else {
            return null;
        }
    }

    private String lowerFirstChar(String className) {
        char[] chars = className.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
