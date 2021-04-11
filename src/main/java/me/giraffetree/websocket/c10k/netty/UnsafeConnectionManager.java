package me.giraffetree.websocket.c10k.netty;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import me.giraffetree.websocket.c10k.base.AbstractConnectionDataManager;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

/**
 * @author GiraffeTree
 * @date 2021-04-06
 */
@Slf4j
@NotThreadSafe
public class UnsafeConnectionManager extends AbstractConnectionDataManager<String, ChannelConnectionInfo> {

    public UnsafeConnectionManager() {
    }

    public UnsafeConnectionManager(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void updateConnection(String key, ChannelConnectionInfo value) {
        // 每次更新连接信息时, 回去查找最近过期的连接
        List<ChannelConnectionInfo> expiredConnection = getExpiredConnection(10);
        for (ChannelConnectionInfo channelConnectionInfo : expiredConnection) {
            String id = channelConnectionInfo.getId();
            deleteConnection(id);
            Channel channel = channelConnectionInfo.getChannel();
            channel.close();
            log.debug("update connection id:{} and delete expired connection id:{}", key, id);
        }
        super.updateConnection(key, value);
    }
}
