package me.giraffetree.websocket.c10k.websocket.base;

/**
 * 用于检查该对象是否过期
 *
 * @author GiraffeTree
 * @date 2020/12/7 10:38
 */
public interface IExpired {

    /**
     * 检查是否过期
     *
     * @return 是否过期
     */
    boolean checkExpired();

}
