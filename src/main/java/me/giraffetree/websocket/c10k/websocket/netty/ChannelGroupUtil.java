package me.giraffetree.websocket.c10k.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChannelGroupUtil {

    public static final String KEY_ID = "id";
    public static final AttributeKey<String> ATTRIBUTE_KEY_ID = AttributeKey.valueOf(KEY_ID);

    private static ChannelGroup gChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ConcurrentHashMap<String, Channel> gUserChannelMap = new ConcurrentHashMap<>();

    /**
     * 新增 Channel
     *
     */
    public void addChannel(Channel channel) {
        gChannelGroup.add(channel);
    }

    /**
     * 关闭 Channel
     */
    public void removeChannel(Channel channel) {
        gChannelGroup.remove(channel);
        channel.close();
    }

    /**
     * 用户 注册
     * 1、设置 Channel 中 id 属性
     * 2、存放在 gUserChannelMap 中
     */
    public void idRegister(Channel channel, String id) {
        this.setChannelUserAttribute(channel, id);
        gUserChannelMap.put(id, channel);
        log.info(id + " 已连接");
    }

    /**
     * 获取用户 对应的 Channel
     */
    public Channel getChannelById(String id) {
        return gUserChannelMap.get(id);
    }


    private void setChannelUserAttribute(Channel channel, String id) {
        channel.attr(ATTRIBUTE_KEY_ID).setIfAbsent(id);
    }

    /**
     * 获取 channel 的userId
     */
    public String getUserIdByChannel(Channel channel) {
        return channel.attr(ATTRIBUTE_KEY_ID).get();
    }


    public ChannelGroup getChannelGroup() {
        return gChannelGroup;
    }

    public ConcurrentHashMap<String, Channel> getUserChannelMap() {
        return gUserChannelMap;
    }
}
