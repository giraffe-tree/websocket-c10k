package me.giraffetree.websocket.c10k.benchmark;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author GiraffeTree
 * @date 2021/3/22
 */
public class BenchmarkWebsocketClient {

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        if (args == null || args.length != 2) {
            System.out.println("first: uri second: loopCount");
            return;
        }
        Integer loop = Integer.valueOf(args[1]);
        WebSocketClient[] list = new WebSocketClient[loop];

        for (int i = 1; i <= loop; i++) {
            URI uri = new URI(args[0] + "?id=" + i);
            WebSocketClient client;
            try {
                client = new DefaultWebsocketClient(uri);
                // 每次连接会创建一个线程...不能这么做
                client.connect();
            } catch (Exception e) {
                System.out.println("error connect - " + e.getLocalizedMessage());
                Thread.sleep(1000L);
                continue;
            }
            list[i - 1] = client;
            if (i % 100 == 0) {
                System.out.println("connect client - " + i);
            }
        }

        Thread.sleep(5000L);
        for (WebSocketClient webSocketClient : list) {
            webSocketClient.close();
        }
    }

}
