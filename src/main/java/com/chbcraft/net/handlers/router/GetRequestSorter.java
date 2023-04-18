package com.chbcraft.net.handlers.router;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.listen.MapParam;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Map;

public class GetRequestSorter extends RequestSorter{

    @Override
    public Object handler(ChannelHandlerContext ctx, FullHttpRequest request) {
        String reqPath = request.uri();
        String paramStr = null;
        Map<String,Object> paramsMap = null;
        if(reqPath.contains("?")){//检查是否有地址参数,如果有将参数部分和地址部分分割
            int i = reqPath.indexOf("?");
            paramStr = reqPath.substring(i+1);
            reqPath = reqPath.substring(0,i);
            paramsMap = RequestUtil.decodeGetPath(paramStr);
        }
        RegisteredRouter router = FloatSphere.getPluginManager().getRouter(RegisteredRouter.RouteMethod.GET.name(),reqPath,0,paramsMap==null?-1:paramsMap.size());
        LinkedList<String> pathParamsStack = null;
        if(router==null&&paramStr==null)//解析成REST风格去匹配
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
            Object ret = router.execute(params);
            if(ret==null){
                ret = RequestUtil.HandlerResultState.NO_RESULT;
            }else{
                ret = RequestUtil.createResponseMessage(ret,router);
            }
            return ret;
        } catch (InvocationTargetException | IllegalAccessException e ) {
            return RequestUtil.HandlerResultState.RUNTIME_ERROR;
        }catch (ArrayIndexOutOfBoundsException e){
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
        }catch (InvocationTargetException | IllegalAccessException e){
            /**
             * 返回500错误
             */
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
            router = FloatSphere.getPluginManager().getRouter(RegisteredRouter.RouteMethod.GET.name(),reqPath,length,0);
        }
        return router;
    }
}
