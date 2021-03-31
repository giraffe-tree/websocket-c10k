# websocket 百万连接及心跳管理

## 概述

### 场景与模型

有很多物联网设备通过 websocket 协议连接到服务器, 每个设备有一个唯一的 id (比如mac地址)

### 判断指标

1. 连接数 
2. 连接速率  
3. 过期连接删除速率

### roadmap

1. 单机 100w websocket 长连接
2. 单机 100w websocket 长连接 + 心跳检查, 过期连接删除 (doing... 预计 2021.04.03 前完成) 
    - 思考: 
        1. 通过 tcp 发送心跳包
        2. 通过 udp 发送心跳包
        3. 负载均衡, 多线程处理心跳包 
3. nginx 前置代理/负载均衡 100w websocket 长连接
4. 单机 1000w tcp 连接
5. 单机 1000w websocket 长连接

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
    - `java -cp target/c10k-0.0.1.jar -Dloader.main=me.giraffetree.websocket.c10k.client.ClientStarter org.springframework.boot.loader.PropertiesLauncher` 
    - 查看参数默认参数: `java -cp target/c10k-0.0.1.jar -Dloader.main=me.giraffetree.websocket.c10k.client.ClientStarter org.springframework.boot.loader.PropertiesLauncher --help` 

### 如何优化系统性能

- `vi /etc/sysctl.conf`  修改后使用  `sysctl -p` 生效, 使用 `sysctl -a` 查看当前配置
    - 操作系统可以打开的最大文件数 `fs.file-max=1100000`
    - 单个进程可以打开的最大文件数 `fs.nr_open=1050000`
    - 修改可用端口数 `net.ipv4.ip_local_port_range = 5000 65000`
- `vi /etc/security/limits.conf` 重新打开 bash 即可生效
    - 修改用户进程可以打开的最大文件数 请注意这个最大文件数不要大于 `单个进程可以打开的最大文件数 fs.nr_open`
    - `root soft nofile 1010000`
    - `root hard nofile 1010000`

### 常用工具

- dmesg | tail -n 20
    - dmesg用来显示内核环缓冲区（kernel-ring buffer）内容，内核将各种消息存放在这里。
    - 我们可以用来检查系统 tcp 相关的异常
        - 例如发现 `nf_conntrack: table full, dropping packet` 则需要修改 `/etc/sysctl.conf` 中的 `net.netfilter.nf_conntrack_max = 1200000` 进来的连接数超过这个值时，新连接的包会被丢弃。
        - 例如发现 `too many orphaned sockets` 则表示 系统耗光了socket内存 , 需要调整一下tcp socket参数。在tcp_mem三个值分别代表low，pressure，high三个阈值 `vi /etc/sysctl.conf`
            - `net.ipv4.tcp_mem = 600000 800000 943718`  
                - tcp_mem 中的单位是页，1页=4096字节, 由于我的机子是 2核4GB的, 这里最大我设定为3.6G的内存左右
            - `net.ipv4.tcp_rmem = 4096 4096 6291456` 
                - tcp_rmem，tcp_wmem单位是byte，所以最小socket读写缓存是4k。
            - `net.ipv4.tcp_wmem = 4096 4096 6291456`
- `watch "ss -ant | grep ESTAB | wc -l"`
    - 计算 tcp 连接数
- 查看当前 tcp 连接数
    - `netstat -pntl | head -n 10`
    - `netstat -nlt | grep 8090`
-  查看当前最大文件数
    1. `cat /proc/sys/fs/file-max`
    2. `cat /proc/sys/fs/file-nr`
-  查看端口范围
    1. `cat /proc/sys/net/ipv4/ip_local_port_range`





