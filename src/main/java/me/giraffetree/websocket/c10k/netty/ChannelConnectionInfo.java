package me.giraffetree.websocket.c10k.netty;

import io.netty.channel.Channel;
import lombok.Data;
import me.giraffetree.websocket.c10k.base.IExpired;


/**
 * @author GiraffeTree
 * @date 2021-04-06
 */
@Data
public class ChannelConnectionInfo implements IExpired {

    private static final long DEFAULT_DELAY_MILLS = 10L * 1000;

    private final String id;

    private final Channel channel;

    private long created;

    private long expired;

    public ChannelConnectionInfo(String id, Channel channel) {
        this.id = id;
        this.channel = channel;
        this.created = System.currentTimeMillis();
        this.expired = this.created + DEFAULT_DELAY_MILLS;
    }

    public void resetExpired() {
        this.expired = System.currentTimeMillis() + DEFAULT_DELAY_MILLS;
    }

    @Override
    public boolean checkExpired() {
        return System.currentTimeMillis() > this.expired;
    }

}
