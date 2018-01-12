package com.mjp.mvc.servlet;

import com.mjp.mvc.annotation.Controller;
import com.mjp.mvc.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
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
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("enter get method");
        String url = req.getRequestURI();
        out(resp, url);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("enter post method");
        String url = req.getRequestURI();
        out(resp, url);
    }

    private void out(HttpServletResponse resp, String url) {
        try {
            resp.setContentType("application/json;charset=utf-8");
            resp.getWriter().print("received request from " + url);
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

    private String lowerFirstChar(String className) {
        char[] chars = className.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
