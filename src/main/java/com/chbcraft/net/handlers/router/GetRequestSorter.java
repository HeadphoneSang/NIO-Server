package com.chbcraft.net.handlers.router;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.MapParam;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.listen.URLDecode;
import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class GetRequestSorter extends RequestSorter{

    private static final String charset = FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value());

    @Override
    public Object handler(ChannelHandlerContext ctx, FullHttpRequest request) {
        String reqPath = request.uri();
        String paramStr = null;
        Map<String,Object> paramsMap = null;
        String retain = reqPath;
        if(reqPath.contains("?")){//检查是否有地址参数,如果有将参数部分和地址部分分割
            int i = reqPath.indexOf("?");
            paramStr = reqPath.substring(i+1);
            reqPath = reqPath.substring(0,i);
            paramsMap = RequestUtil.decodeGetPath(paramStr);
        }
        RegisteredRouter router = FloatSphere.getPluginManager().getRouter(RegisteredRouter.RouteMethod.GET.name(),reqPath,0,paramsMap==null?-1:paramsMap.size());
        if(paramsMap!=null&&router==null)
        {
            reqPath = retain;
            paramsMap = null;
            paramStr = null;
        }
        LinkedList<String> pathParamsStack = null;
        if(router == null)//解析成REST风格去匹配
        {
            pathParamsStack = new LinkedList<>();
            router = getRestRouter(pathParamsStack, reqPath);
        }
        if(router==null){//全地址匹配不到,REST匹配不到,返回404
            return RequestUtil.HandlerResultState.NO_MATCHES;
        }
        if(pathParamsStack!=null&&router.isRest()){
            return handlerRestGet(pathParamsStack,router);
        }else{
            return handlerNormalGet(paramsMap,router);
        }
    }

    /**
     * 处理REST风格的GET请求
     * @param pathParamsStack 参数栈
     * @param router 路由器
     * @return 返回运行状态,或者运行结果
     */
    protected Object handlerRestGet(LinkedList<String> pathParamsStack,RegisteredRouter router){
        try {
            Object[] params = new Object[pathParamsStack.size()];
            int[] indexMap = router.getIndexMap();
            for (int j : indexMap) {
                params[j] = pathParamsStack.poll();
            }
            if(router.hasTags(URLDecode.class))
                for(int i = 0;i<params.length;i++){
                    params[i] = URLDecoder.decode((String) params[i],charset);
                }
            Object ret = router.execute(params);
            if(ret==null){
                ret = RequestUtil.HandlerResultState.NO_RESULT;
            }else{
                ret = RequestUtil.createResponseMessage(ret,router);
            }
            return ret;
        } catch (InvocationTargetException | IllegalAccessException e ) {
            e.printStackTrace();
            return RequestUtil.HandlerResultState.RUNTIME_ERROR;
        }catch (ArrayIndexOutOfBoundsException| UnsupportedEncodingException e){
            e.printStackTrace();
            return RequestUtil.HandlerResultState.FORMAT_ERROR;
        }
    }

    /**
     * 处理普通的GET请求
     * @param paramsMap 参数列表
     * @param router 路由器
     * @return 返回运行状态,或者运行结果
     */
    public Object handlerNormalGet(Map<String,Object> paramsMap,RegisteredRouter router){
        try{
            Object ret;
            if(router.hasTags(URLDecode.class)){
                for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                    paramsMap.put(entry.getKey(), URLDecoder.decode((String) entry.getValue(), charset));
                }
            }
            if(paramsMap==null){
                ret = router.execute(null);
            }
            else{
                if(router.hasTags(MapParam.class)){
                    ret = router.execute(new Object[]{paramsMap});

                }else{
                    ret = router.execute(paramsMap.values().toArray());
                }
            }
            if(ret==null){
                ret = RequestUtil.HandlerResultState.NO_RESULT;
            }else{
                ret = RequestUtil.createResponseMessage(ret,router);
            }
            return ret;
        }catch (InvocationTargetException | IllegalAccessException | UnsupportedEncodingException e){
            /**
             * 返回500错误
             */
            e.printStackTrace();
            return RequestUtil.HandlerResultState.RUNTIME_ERROR;
        }
    }

    /**
     * 从未到头匹配Restful的Get请求
     * @param pathParamsStack 参数栈
     * @return 返回匹配到的Router
     */
    protected RegisteredRouter getRestRouter(LinkedList<String> pathParamsStack,String reqPath){
        int e;
        int length = 0;
        RegisteredRouter router = null;
        while(router==null&&(e = reqPath.lastIndexOf("/"))!=-1){
            pathParamsStack.addFirst(reqPath.substring(e+1));
            reqPath = reqPath.substring(0,e);
            if(reqPath.lastIndexOf("/")==-1)
                break;
            length++;
            router = FloatSphere.getPluginManager().getRouter(RegisteredRouter.RouteMethod.GET.name(),reqPath,length,-1);
        }
        return router;
    }
}
