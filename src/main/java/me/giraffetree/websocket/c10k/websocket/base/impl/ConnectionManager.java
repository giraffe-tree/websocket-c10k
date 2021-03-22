package me.giraffetree.websocket.c10k.websocket.base.impl;

import me.giraffetree.websocket.c10k.websocket.base.AbstractConnectionDataManager;
import org.springframework.stereotype.Service;

/**
 * 非线程安全
 *
 * @author GiraffeTree
 * @date 2021/3/22 16:25
 */
@Service
public class ConnectionManager extends AbstractConnectionDataManager<String, ConnectionInfo> {

}
