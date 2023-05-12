package com.chbcraft.internals.components.listen;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.Routers;
import com.chbcraft.net.handlers.inbound.websocket.event.RouterAfterExecuteEvent;
import com.chbcraft.net.handlers.inbound.websocket.event.RouterBeforeExecuteEvent;
import com.chbcraft.plugin.Plugin;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegisteredRouter {
    /**
     * 注册的插件
     */
    private Plugin plugin;
    /**
     * 占位符
     */
    private static final Object placeHolder = new Object();
    /**
     * 路由方法
     */
    private final Method handlerMethod;
    /**
     * 注册路由的类
     */
    private final Routers routers;
    /**
     * 路由方法的标签
     */
    private Map<Class<? extends Annotation>,Object> tags;
    /**
     * 是否是RESTFUL
     */
    private boolean isRest = false;
    /**
     * 下标代表路径中参数的位置,对应下标的值代表路径中下标位置的值在方法中为第几个参数
     */
    private int[] indexMap;
    /**
     * 路由地址
     */
    private String route;
    /**
     * 是什么类型的方法
     */
    private RouteMethod method;
    /**
     * REST地址栏参数个数
     */
    private int paramLength = 0;
    /**
     * 方法参数个数
     */
    private int methodParamsLength = 0;
    /**
     * 方法封装类型参数
     */
    private Class<?> paramClazz = null;

    public Class<?> getParamClazz() {
        return paramClazz;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public void setMethod(RouteMethod method) {
        this.method = method;
    }
    public RegisteredRouter(Plugin plugin,Method handlerMethod,Routers routers){
        this(plugin,handlerMethod,routers,"invalid",RouteMethod.GET);
    }

    public RegisteredRouter(Plugin plugin, Method handlerMethod, Routers routers, String routePath, RouteMethod method){
        this.plugin = plugin;
        this.handlerMethod = handlerMethod;
        this.routers = routers;
        this.route = routePath;
        this.method = method;
    }

    /**
     * 执行路由方法
     * @param params 路由方法的参数
     * @return 返回方法执行后的返回值
     */
    public Object execute(Object[] params) throws InvocationTargetException, IllegalAccessException {
        Object ret = null;
        RouterExecutorInfo info = new RouterExecutorInfo(params);
        FloatSphere.getPluginManager().callEvent(new RouterBeforeExecuteEvent(info));
        if(params==null){
            ret = handlerMethod.invoke(routers);
        }
        ret = handlerMethod.invoke(routers,params);
        info.clearParams();
        info.setResult(ret);
        FloatSphere.getPluginManager().callEvent(new RouterAfterExecuteEvent(info));
        return ret;
    }
    public Plugin getPlugin() {
        return plugin;
    }

    public Routers getRouters() {
        return routers;
    }

    public String getRoute() {
        return route;
    }

    public RouteMethod getMethod() {
        return method;
    }

    public boolean hasTags(Class<? extends Annotation> tag) {

        return tags != null && (tags.get(tag) != null);
    }

    public Collection<Class<? extends Annotation>> getTags(){
        if(tags==null)
            return null;
        return Collections.unmodifiableCollection(tags.keySet());
    }

    public void addTags(Class<? extends Annotation> tag) {
        if(this.tags==null){
            this.tags = new HashMap<>();
        }
        this.tags.put(tag,placeHolder);
    }

    /**
     * REST地址栏参数个数
     * @return 返回REST地址栏参数个数
     */
    public int getParamLength() {
        return paramLength;
    }

    /**
     * 方法参数个数
     * @return 返回方法参数格式
     */
    public int getMethodParamsLength() {
        return methodParamsLength;
    }

    /**
     * 设置方法参数个数
     * @param methodParamsLength 方法参数个数
     */
    public void setMethodParamsLength(int methodParamsLength) {
        this.methodParamsLength = methodParamsLength;
    }

    public void setParamLength(int paramLength) {
        this.paramLength = paramLength;
    }

    public enum RouteMethod {
        GET("GET"),
        OPTIONS("OPTIONS"),
        POST("POST");
        private final String typeValue;
        private RouteMethod(String value){
            this.typeValue = value;
        }
        public String getTypeValue() {
            return typeValue;
        }
    }

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public int[] getIndexMap() {
        return indexMap;
    }

    public void setIndexMap(int[] indexMap) {
        this.indexMap = indexMap;
    }

    public class RouterExecutorInfo {

        private Object[] params;

        private Object result;

        private RouterExecutorInfo(Object[] params){
            this.params = params;
        }

        /**
         * 查看当前执行路由的路由器是否含有某个标签
         * @param clazz 标签的Class类
         * @return 返回是否含有
         */
        public boolean hasTags(Class<? extends Annotation> clazz){
            return RegisteredRouter.this.hasTags(clazz);
        }

        public Object[] getParams(){
            return params;
        }

        protected void clearParams(){
            this.params = null;
        }

        public Object getRouterResult(){
            return this.result;
        }

        public Object updateRouterResult(Object o){
            Object old = this.result;
            this.result = o;
            return old;
        }

        private void setResult(Object result){
            this.result = result;
        }
    }
}
