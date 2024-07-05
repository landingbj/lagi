## 附件一：
如果您想使用检索增强rag功能，一般需要搭建向量数据库，下面就以chroma来示范介绍具体步骤：

### 方式一：Python
***1.首先要确保有安装有Python运行环境***
- 安装chromadb
```bash
    pip install chromadb
```
- 创建数据库存储目录
```bash
    mkdir db_data
```
- 启动数据库服务
```bash
    # --path参数可以指定数据持久化路径
    # 默认开启8000端口
    chroma run --path db_data
```

***2.修改lagi.yml配置文件***
- 修改lagi.yml配置文件中stores>vectors>url字段的值为：
 ```
    url: http://your-host-ip:8000
 ```

### 方式二：Docker
***1.首先要确保有安装有Docker运行环境***
- 运行以下命令,拉取安装并启动chromadb。
```bash
    # 启动Chroma数据库容器
    #
    # 该命令用于在Docker中启动一个名为Chroma的数据库服务容器。它通过一系列参数配置容器的运行环境和对外服务。
    # 参数解释：
    # -d: 后台运行容器并返回容器ID。
    # --name: 为容器指定一个名称，便于后续管理和识别。
    # -p: 容器内部端口与主机端口的映射，这里将容器的8000端口映射到主机的8000端口。
    # -v: 容器内部目录与主机目录的绑定，实现数据的持久化存储。这里将主机的/mydata/docker/local/chroma/data目录绑定到容器的/study/ai/chroma目录。
    # -e: 设置环境变量，用于配置容器内部的应用程序行为。这里设置了两个环境变量：
    #     IS_PERSISTENT=TRUE 表示数据库数据将被持久化存储。
    #     ANONYMIZED_TELEMETRY=TRUE 表示允许匿名遥测数据的收集。
    # chromadb/chroma:latest: 指定使用的镜像为chromadb/chroma的最新版本。
    
    docker run -d \
    --name chromadb \
    -p 8000:8000 \
    -v /mydata/docker/local/chroma/data:/study/ai/chroma \
    -e IS_PERSISTENT=TRUE \
    -e ANONYMIZED_TELEMETRY=TRUE \
    chromadb/chroma:latest
```

***2.修改lagi.yml配置文件***
- 修改lagi.yml配置文件中stores>vectors>url字段的值为：
 ```
    url: http://your-host-ip:8000
 ```
