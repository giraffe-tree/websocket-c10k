package me.giraffetree.websocket.c10k.websocket.netty;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author GiraffeTree
 * @date 2021/3/25 17:48
 */
@Slf4j
public class ConnectionStatisticsManager {

    private final static LongAdder longAdder = new LongAdder();

    public static void addConnection() {
        longAdder.increment();
        log.info("add connection - size:{}", longAdder.longValue());
    }

    public static void removeConnection() {
        longAdder.decrement();
        log.info("remove connection - size:{}", longAdder.longValue());
    }

}
