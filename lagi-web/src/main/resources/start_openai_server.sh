#!/bin/bash

conda_path=$1
if [ -z "$conda_path" ]; then
    echo "Error: conda_path is not set."
    exit 1
fi
conda_env=$2
if [ -z "$conda_env" ]; then
    echo "Error: conda_env is not set."
    exit 1
fi

source $conda_path/bin/activate $conda_env


model_path=$3
if [ -z "$model_path" ]; then
    echo "Error: model_path is not set."
    exit 1
fi

template=$4
if [ -z "$template" ]; then
    echo "Error: template is not set."
    exit 1
fi

devices=$5
if [ -z "$devices" ]; then
    echo "Error: devices is not set."
    exit 1
fi
port=$6
if [ -z "$port" ]; then
    echo "Error: port is not set."
    exit 1
fi
adapter_path=$7


finetuning_type=$8



# 设置环境变量
export ASCEND_RT_VISIBLE_DEVICES=$devices
export API_PORT=$port


cmd="llamafactory-cli api --model_name_or_path $model_path --template $template --trust_remote_code True"



# 拼接 --adapter_name_or_path 参数
if [ -n "$adapter_path" ]; then
    cmd="$cmd --adapter_name_or_path $adapter_path"
fi


# 拼接 --finetuning_type 参数
if [ -n "$finetuning_type" ]; then
    cmd="$cmd --finetuning_type $finetuning_type"
fi



echo "Executing command: $cmd"
# 执行命令
eval $cmd