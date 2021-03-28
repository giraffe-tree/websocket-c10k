package me.giraffetree.websocket.c10k.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * -c 1000 --heartBeatLoop 20 --heartBeatDurationMills 10000 --hosts localhost -p 8011 --path /websocket/handshake/
 *
 * @author GiraffeTree
 * @date 2021-03-28
 */
public class ClientStarter {

    private static final String PING = "{\"seq\":\"0\",\"cmd\":\"ping\",\"response\":{\"code\":200}}";

    @Parameter(names = {"--count", "-c"}, description = "connection count per host ")
    private int count = 1000;

    @Parameter(names = {"--heartBeatLoop"}, description = "heartBeat loop count")
    private int heartBeatLoop = 20;

    @Parameter(names = {"--heartBeatDurationMills"}, description = "heartBeat duration")
    private long heartBeatDurationMills = 10000L;

    @Parameter(names = {"-h", "--hosts"}, splitter = CommaParameterSplitter.class, description = "server hosts/ips - split by comma , ")
    private List<String> hosts = new ArrayList<>();

    @Parameter(names = {"-p", "--port"}, description = "server port")
    private int port = 8011;
    @Parameter(names = {"--path", "-P"}, description = "websocket path - please start with /", help = true)
    private String path = "/websocket/handshake/";

    @Parameter(names = "--help", help = true)
    private boolean help;

    public static void main(String[] args) {
        ClientStarter clientStarter = new ClientStarter();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(clientStarter)
                .build();
        jCommander.parse(args);
        if (clientStarter.help) {
            jCommander.usage();
            return;
        }
        clientStarter.run();
    }

    private void run() {

        ThreadLocalRandom cur = ThreadLocalRandom.current();
        ArrayList<WebSocketClient> list = new ArrayList<>(count);
        if (hosts.size() == 0) {
            System.out.println("[ warn ] try to use localhost as host");
            hosts.add("localhost");
        }
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

        for (String host : hosts) {
            int randomPrefix = cur.nextInt(100000);
            String formatPath = String.format("ws://%s:%d%s", host, port, path);
            System.out.printf("[connect] start connect - %s count:%d%n", formatPath, count);
            long startConnectMills = System.currentTimeMillis();
            for (int i = 1; i <= count; i++) {
                String uri = formatPath + "?id=" + randomPrefix + "_" + i;
                WebSocketClient webSocketClient = new WebSocketClient(uri);
                try {
                    webSocketClient.open();
                } catch (Exception e) {
                    System.err.printf("[connect] error connect - cur:%d msg:%s%n", i, e.getLocalizedMessage());
                    continue;
                }
                list.add(webSocketClient);
            }
            long endConnectMills = System.currentTimeMillis();
            System.out.printf("[connect] success connect - %s %d/%d cost:%dms%n", formatPath, list.size(), count, endConnectMills - startConnectMills);
        }

        for (int i = 1; i <= heartBeatLoop; i++) {
            System.out.printf("[heartBeat] start heartBeat loop:%d size: %d%n", i, list.size());
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
            System.out.printf("[heartBeat] end heartBeat cost:%dms%n", cost);
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
