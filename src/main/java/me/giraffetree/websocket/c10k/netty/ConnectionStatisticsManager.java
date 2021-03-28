package me.giraffetree.websocket.c10k.netty;

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
        long sum = longAdder.longValue();
        if (sum % 1000L == 0L) {
            log.info("add connection - size:{}", sum);
        }
    }

    public static void removeConnection() {
        longAdder.decrement();
        long sum = longAdder.longValue();
        if (sum % 1000L == 0L) {
            log.info("remove connection - size:{}", sum);
        }
    }

}
