#!/bin/sh

process_name=llamafactory-cl
port=$1

# 使用 netstat 或 ss 获取指定端口的进程ID
pid=$(netstat -tulnp 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1)

# 如果没有找到进程ID，尝试使用 ss 命令
if [ -z "$pid" ]; then
    pid=$(ss -tulnp 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1)
fi

# 如果找到了进程ID，并且进程名匹配，则结束进程
if [ -n "$pid" ]; then
    process_info=$(ps -p $pid -o comm=)
    if [ "$process_info" = "$process_name" ]; then
        echo "Killing process $process_name (PID: $pid) running on port $port"
        kill -9 $pid
    else
        echo "Process with PID $pid is not $process_name but $process_info"
    fi
else
    echo "No process found running on port $port"
fi