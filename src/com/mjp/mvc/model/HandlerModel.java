package com.mjp.mvc.model;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * url映射匹配方法（对应controller，方法，方法参数）
 */
public class HandlerModel {

    //controller
    private Object object;

    //对应映射方法
    private Method method;

    //方法参数：key：参数名字，value：参数顺序
    private Map<String, Integer> params;

    public HandlerModel(Object object, Method method, Map<String, Integer> params) {
        this.object = object;
        this.method = method;
        this.params = params;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Map<String, Integer> getParams() {
        return params;
    }

    public void setParams(Map<String, Integer> params) {
        this.params = params;
    }
}
