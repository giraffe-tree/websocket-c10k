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

### 连接数优化










