package me.giraffetree.websocket.c10k.websocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.websocket.base.IDeviceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author GiraffeTree
 * @date 2020/12/10 9:51
 */
@Slf4j
@Component
public class AuthCheckHandshakeInterceptor implements HandshakeInterceptor {

    private final static String DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";

    private final IDeviceManager deviceManager;

    @Autowired
    public AuthCheckHandshakeInterceptor(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
        Map<String, String> map = splitQuery(serverHttpRequest.getURI());
        String id = map.get("id");
        if (id == null || id.length() == 0) {
            // 检查失败
            log.debug("check fail - id is null");
            setResponse((HttpServletResponse) serverHttpResponse, 18001, "param id not found");
            return false;
        }
        boolean contains = deviceManager.contains(id);
        if (contains) {
            // 这里有两种策略, 一种是拒绝访问, 一种是结束上一个连接
            // 这里我们先简单点来, 直接拒绝访问
            return false;
        }
        // 实现你自己的鉴权操作

        attributes.put("id", id);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // do nothing

    }

    private void setResponse(HttpServletResponse response, int errorCode, String info) throws IOException {
        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_OK);
        HashMap<String, Object> map = new HashMap<>(8);
        map.put("code", errorCode);
        map.put("info", info);
        response.getWriter().write(JSON.toJSONString(map));
    }

    private static Map<String, String> splitQuery(URI uri) {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String query = uri.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                queryPairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()),
                        URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                // do nothing
            }
        }
        return queryPairs;
    }
}
