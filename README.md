# websocket 百万连接及心跳管理

## 概述

### 场景与模型

有很多物联网设备通过 websocket 协议连接到服务器, 每个设备有一个唯一的 id (比如mac地址)

### 判断指标

1. 连接数
2. 连接速率
3. 过期连接删除速率

## 实践

### 第一次测试

在浏览器中

```
ws = new WebSocket("ws://localhost:8010/websocket/handshake?id=01")
```

### 0.0.1

- 2核8G 服务器 
- OpenJDK 64-Bit Server VM (build 25.275-b01, mixed mode)
- Linux version 4.18.0-193.28.1.el8_2.x86_64 (mockbuild@kbuilder.bsys.centos.org) (gcc version 8.3.1 20191121 (Red Hat 8.3.1-5) (GCC)) #1 SMP Thu Oct 22 00:20:22 UTC 2020
- 2核4G 客户端

- `java -jar target/c10k-0.0.1.jar`
    - 10000 连接
        - 服务器完全启动后, 约10秒可以接受 10000 新连接
        - 服务端 cpu 连接时, cpu 90%以上, 维护心跳时 45% 左右
    - 15000 连接
        - gc 大约10秒钟会进行一次 full gc
        - 维护心跳时 cpu: 60% ~ 80%
    - 20000 连接
        - gc 大约3秒钟会进行一次 full gc
        - 维护心跳时 cpu: 140%
- `java -Xmx6g -Xms6g -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:MetaspaceSize=96M -jar target/c10k-0.0.1.jar`
    - 这里主要提高堆大小, 以增加连接数, 使用 arthas 可以监控堆大小以及gc情况
    - 为了减少应用启动时 `Metadata GC Threshold` 导致的 full gc, 我这里还提高了 `MetaspaceSize` 的大小
    - 并且我们打印了 gc 情况, 方便排查问题
    - 20000 连接
        - 这里我们直接从 20000 连接开始测试
            - 这里我使用 arthas 进行监控
            - 从连接数0 到 连接数20000 的过程中一共发生 3 次年轻代GC , 总计 1203ms
            - 虽然没有发生 full gc 但这个时间我仍然无法忍受
- `java -Xmx6g -Xms6g -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseG1GC -jar target/c10k-0.0.1.jar`
    - 这里我使用了 g1gc 想要减少 gc 中 stop the world 等待时间
    - 20000 连接
        - 从连接数0 到 连接数20000 的过程中一共发生 9 次 young gc , 共计 1563ms
            - 虽然单次 gc 的平均时长比之前短, 但是总时长还增加了 




