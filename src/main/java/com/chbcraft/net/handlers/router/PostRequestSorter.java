package com.chbcraft.net.handlers.router;

import com.alibaba.fastjson.JSON;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.MapParam;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.net.util.RequestUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Map;

public class PostRequestSorter extends RequestSorter{
    /**
     * 处理POST方法的请求
     * @param ctx 连接上下文
     * @param request 完整请求消息
     */
    @Override
    public Object handler(ChannelHandlerContext ctx, FullHttpRequest request) {
        String reqPath = request.uri();
        RegisteredRouter router = FloatSphere.getPluginManager().getRouter(RegisteredRouter.RouteMethod.POST.name(),reqPath,0,-1);
        if(router==null) {
            return RequestUtil.HandlerResultState.NO_MATCHES;
        }
        String content = request.content().toString(Charset.forName(FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value())));
        Object ret;
        try {
            if (content.length()==0){
                if(router.getMethodParamsLength()!=0)
                /**
                 * 返回505错误
                 */return RequestUtil.HandlerResultState.FORMAT_ERROR;
                ret = router.execute(null);
            }
            /**
             * 如果POST路由方法的参数是Map
             */
            else if(router.hasTags(MapParam.class)){
                Map map = JSON.parseObject(content,Map.class);
                ret = router.execute(new Object[]{map});
            }
            /**
             * 如果POST路由方法的参数是封装类型对象
             */
            else if(router.getParamClazz()!=null){
                Object paramObj = JSON.parseObject(content,router.getParamClazz());
                ret = router.execute(new Object[]{paramObj});
            }
            /**
             * 如果是基本类型参数
             */
            else{
                ret = router.execute(new Object[]{content});
            }
            if(ret==null){
                ret = RequestUtil.HandlerResultState.NO_RESULT;
            }else{
                ret = RequestUtil.createResponseMessage(ret,router);
            }
            return ret;
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return RequestUtil.HandlerResultState.RUNTIME_ERROR;
            /**
             * 返回错误
             */
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return RequestUtil.HandlerResultState.FORMAT_ERROR;
        }
    }
}
