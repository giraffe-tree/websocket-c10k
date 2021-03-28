//package me.giraffetree.websocket.c10k.websocket;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
///**
// * @author GiraffeTree
// * @date 2020-02-11
// */
//@Configuration
//@EnableWebSocket
//public class WebSocketConf implements WebSocketConfigurer {
//
//    private final WebMessageHandler websocketHandler;
//    private final AuthCheckHandshakeInterceptor authCheckHandshakeInterceptor;
//
//    @Autowired
//    public WebSocketConf(WebMessageHandler websocketHandler, AuthCheckHandshakeInterceptor authCheckHandshakeInterceptor) {
//        this.websocketHandler = websocketHandler;
//        this.authCheckHandshakeInterceptor = authCheckHandshakeInterceptor;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//
//        registry.addHandler(websocketHandler, "/websocket/handshake/**")
//                .addInterceptors(authCheckHandshakeInterceptor)
//                .setAllowedOrigins("*");
//
//    }
//
//
//}
//
