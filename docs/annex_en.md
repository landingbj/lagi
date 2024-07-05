## Annex 1:
If you want to enhance rag functionality with search, you usually need to build a vector database,Let's build our vector database using chromadb as an example：

### Option 1: Python
***1. Make sure Python is installed***
- Install the chromadb
```bash
    pip install chromadb
```
- Create the database storage directory
```bash
    mkdir db_data
```
- Starting the database service
```bash
    # The --path argument allows us to specify the data persistence path
    # Port 8000 is enabled by default
    chroma run --path db_data
```

***2. Modify the lagi.yml configuration file***
- Change the stores>vectors>url field in the lagui.yml configuration file to:
 ```
    url: http://your-host-ip:8000
 ```

### Option 2: Docker
***1. Make sure you have a Docker environment installed***
- With the following command, pull the installation and start the Chroma database container.
```bash
    # Start the Chroma database container
    #
    # This command is used to start a database service container called Chroma in Docker. It uses a set of parameters to configure the container environment and external services.
    # Parameters explained:
    # -d: runs container in background and returns container ID
    # --name: This specifies a name for the container for easier management and identification.
    # -p: Container internal ports mapped to host ports, here container port 8000 is mapped to host port 8000.
    # -v: Binding the container's internal directory to the host directory for persistent data storage. Here will host/mydata docker/local/chroma/data directory is bound to the container/study/ai/chroma directory.
    # -e: Sets environment variables that are used to configure application behavior inside the container. Two environment variables are set:
    #     IS_PERSISTENT=TRUE Indicates that database data will be stored persistently.
    #     ANONYMIZED_TELEMETRY=TRUE Indicates that allows the collection of anonymous telemetry data.
    # chromadb/chroma:latest: Specify that the image to use is the latest version of chromadb/chroma.
    
    docker run -d \
    --name chromadb \
    -p 8000:8000 \
    -v /mydata/docker/local/chroma/data:/study/ai/chroma \
    -e IS_PERSISTENT=TRUE \
    -e ANONYMIZED_TELEMETRY=TRUE \
    chromadb/chroma:latest
```

***2. Modify the lagi.yml config file***
- Change the stores>vectors>url field in the lagui.yml configuration file to:
 ```
    url: http://your-host-ip:8000
 ```
