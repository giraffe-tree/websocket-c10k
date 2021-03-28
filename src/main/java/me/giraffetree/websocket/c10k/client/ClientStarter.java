package me.giraffetree.websocket.c10k.client;

import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author GiraffeTree
 * @date 2021-03-28
 */
public class ClientStarter {

    private static final String PING = "{\"seq\":\"0\",\"cmd\":\"ping\",\"response\":{\"code\":200}}";


    @Parameter(names = {"--count", "-c"}, description = "")
    private int count;

    @Parameter(names = {"--heartBeatLoop"},description = "")
    private int heartBeatLoop;

    @Parameter(names = {"--heartBeatDurationMills"},description = "heartBeat duration")
    private long heartBeatDurationMills;

    @Parameter(names = {"-h", "--host"}, description = "server hosts/ips")
    private List<String> hosts = new ArrayList<>();

    @Parameter(names = {"-p","--port"},description = "server port")
    private int port;
    @Parameter(names = {"--path","-P"},description = "websocket path")
    private String path;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("args error");
            System.err.println("format: {count} {heartBeatCount} {heartBeatDurationMills} {serverIps Separated by comma ,} {port} {path(please start with /)} ");
            System.err.println("example: 1000 20 10000 ws://localhost:8011/websocket/handshake/");
            return;
        }

        String urlPrefix = args[3];
        ThreadLocalRandom cur = ThreadLocalRandom.current();
        ArrayList<WebSocketClient> list = new ArrayList<>(count);
        int randomPrefix = cur.nextInt(10000);
        System.out.println(String.format("[connect] start connect - count:%d", count));
        long startConnectMills = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            String uri = urlPrefix + "?id=" + randomPrefix + "_" + i;
            WebSocketClient webSocketClient = new WebSocketClient(uri);
            try {
                webSocketClient.open();
            } catch (Exception e) {
                System.err.println(String.format("[connect] error connect - cur:%d msg:%s", i, e.getLocalizedMessage()));
                continue;
            }
            list.add(webSocketClient);
        }
        long endConnectMills = System.currentTimeMillis();
        System.out.println(String.format("[connect] success connect - %d/%d cost:%dms", list.size(), count, endConnectMills - startConnectMills));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ close ] try to close all connection... size:" + list.size());
            int c = 0;
            for (WebSocketClient webSocketClient : list) {
                try {
                    webSocketClient.close();
                    c++;
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            System.out.println("[ close ] close size:" + c);
        }));

        for (int i = 1; i <= heartBeatLoop; i++) {
            System.out.println(String.format("[heartBeat] start heartBeat loop:%d size: %d", i, list.size()));
            long l1 = System.currentTimeMillis();
            list.forEach(x -> {
                try {
                    x.eval(PING);
                } catch (IOException e) {
                    try {
                        x.close();
                    } catch (InterruptedException interruptedException) {
                        // do nothing
                        System.exit(0);
                    }
                }
            });
            long l2 = System.currentTimeMillis();
            long cost = l2 - l1;
            System.out.println(String.format("[heartBeat] end heartBeat cost:%dms", cost));
            if (cost > heartBeatDurationMills) {
                continue;
            }
            try {
                Thread.sleep(heartBeatDurationMills - cost);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }

    }


}
