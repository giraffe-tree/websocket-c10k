# websocket 百万连接及心跳管理

## 概述

### 场景与模型

有很多物联网设备通过 websocket 协议连接到服务器, 每个设备有一个唯一的 id (比如mac地址)

### 判断指标

1. 连接数
2. 连接速率
3. 过期连接删除速率

### 实际测试结果图

- 连接数 
    - 4核16G的server端, 建立 100w websocket 长连接

![](./docs/img/websocket-c1000k.jpg)

## 如何使用

1. 打包
    - `mvn clean package -DskipTests`
2. 启动 websocket 服务端
    - `java -jar target/c10k-0.0.1.jar`
3. 启动 websocket 客户端
    - `java -cp target/c10k-0.0.1.jar -Dloader.main=me.giraffetree.websocket.c10k.client.ClientStarter org.springframework.boot.loader.PropertiesLauncher` 默认
    - 查看参数默认参数: `java -cp target/c10k-0.0.1.jar -Dloader.main=me.giraffetree.websocket.c10k.client.ClientStarter org.springframework.boot.loader.PropertiesLauncher --help` 




