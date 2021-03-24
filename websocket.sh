#!/usr/bin/env bash

# 这些 ip 是服务端的ip
IPS=(
"172.16.67.210"
"172.16.67.202"
"172.16.67.203"
"172.16.67.206"
"172.16.67.207"
"172.16.67.204"
"172.16.67.205"
"172.16.67.208"
"172.16.67.209"
"172.16.68.133"
"172.16.68.134"
"172.16.68.135"
"172.16.68.131"
"172.16.68.132"
)

TYPE=$1
COUNT=$2
NUM=$3

exec_websocket(){
    mkdir -p logs
    for i in "${!IPS[@]}"; do
        #./main -c 30000 -n 200 -u ws://172.16.67.200:8010/websocket/handshake/
        echo ./main -c $COUNT -n $NUM -u ws://${IPS[$i]}:8010/websocket/handshake/
        nohup ./main -c $COUNT -n $NUM -u ws://${IPS[$i]}:8010/websocket/handshake/ > logs/${IPS[$i]}.log 2>&1 &
    done
}
exec_end(){
  echo try to end:
  ps -ef | grep main | grep websocket | awk '{print $2 " " $8 " " $14}'
  ps -ef | grep main | grep websocket | awk '{print $2}' | xargs kill -9
}

case $TYPE in
    "start")  exec_websocket;;
    "end") exec_end;;
    *)  echo "get unkown type $TYPE"; exit ;;
esac
