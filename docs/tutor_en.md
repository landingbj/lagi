# Tutorial

Lag[i] is a powerful enterprise-class composite multimodal big model middleware that helps you easily integrate big model technology into your business. This tutorial will guide you through the download, installation, configuration and operation of Lag[i] from scratch, so that you can quickly grasp the use of Lag[i].

## Environment preparation

Before you start, make sure you have the following environments ready:

*   **Java 8 or later**
*   **Maven**
*   **Docker (Optional, used to run vector database)**

## 1. Download Lag[i]

For developers, we provide an easy way to compile and run Lag[i] applications. You can choose to use the maven command-line tool for wrapping, or run it through a popular integrated development environment (IDE) such as IntelliJ IDEA.

### Method 1: Use maven command

1. **Cloning project**：First, you need to clone the repository for the Lag[i] project:

```shell
git clone https://github.com/landingbj/lagi.git
```
2. **Go to project**：Change to the project directory:

```shell
cd lagi
```

3. **Build the project**：Run the Maven command in the project root to build:

```bash
mvn clean install
```

### Method 2: Use an IDE

1. **Choose an IDE**： You can choose to use a mainstream IDE like IntelliJ IDEA or Eclipse.

2. **Open GitHub repository**：Connect Lag[i] 's GitHub repository in IDE and use the clone function of IDE to clone Lag[i] project locally.

|        | GitHub repository                             | 
|--------|---------------------------------------| 
| SSH    | git@github.com:landingbj/lagi.git     |
| HTTPS  | https://github.com/landingbj/lagi.git | 

3. **Compile project**： Using the compile feature of your IDE, compile Lag[i] project.

## 2. Installing Vector database

Lag[i] supports several vector databases, such as ChromaDB. If you want to enhance RAG functionality with retrieval, you need to install the vector database.  

**Take ChromaDB for example**:

### Option 1: Python

***Make sure you have a Python environment installed***

- Install chromadb

```bash
    pip install chromadb
```

- Create the database storage directory

```bash
    mkdir db_data
```

- Starting the database service

```bash
    # --The path argument allows us to specify the data persistence path
    # Port 8000 is enabled by default
    chroma run --path db_data
```

### Option 2: Docker

***Make sure you have a Docker environment installed***

- Run the following command to pull the installation and start chromadb.

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

The installation is complete, you can access via a browser: http://localhost:8000/docs see if started successfully.

chromadb：
![img_1.png](images/img_1.png)

## 3. Configure yml files

Modify the `lagi.yml` config file, select your preferred model, Replace the model's `your-api-key` and other information with your own key, and set the `enable` field to `true` for enabled models as needed.

***Example configuration kimi:***

- Fill in the model information and enable the model, changing enable to true.

    ```yaml
      - name: kimi
        type: Moonshot
        enable: true
        model: moonshot-v1-8k,moonshot-v1-32k,moonshot-v1-128k
        driver: ai.llm.adapter.impl.MoonshotAdapter
        api_key: your-api-key
    ```

- Depending on your needs, set the mode stream and the priority of the model output, the higher the priority.

    ```yaml
      chat:
        - backend: doubao
          model: doubao-pro-4k
          enable: true
          stream: true
          priority: 160
    
        - backend: kimi
          model: moonshot-v1-8k
          enable: true
          stream: true
          priority: 150
    ```

Select the configured vector database and fill in the corresponding configuration information.

***Example configuring local chromadb:***

- Replace the url with the chromadb url : http://localhost:8000.

    ```yaml
      vectors:
        - name: chroma
          driver: ai.vector.impl.ChromaVectorStore
          default_category: default
          similarity_top_k: 10
          similarity_cutoff: 0.5
          parent_depth: 1
          child_depth: 1
          url: http://localhost:8000
    
      rag:
        - backend: chroma
          enable: true
          priority: 10
    ```

## 4. Import dependencies

To call the lag[i] API, you need to import the dependencies, which you can import via maven or directly by import the jar.

***Take the maven import as an example：***

- Use maven to download the dependency execution command.

    ```shell
    mvn clean install
    ```

## 5. Start the web service.

You can choose to use the maven command-line tool for wrapping, or run it through a popular integrated development environment (IDE) such as IntelliJ IDEA.

***Example maven command encapsulation:***

- 1.Use the maven command to wrap the project, which will generate a war file in the `target` directory.

    ```shell
    mvn package
    ```

- 2.Deploy to Web server: Deploy the packaged files to a Web server.

     Tomcat:
     > Copy the WAR file into Tomcat's webapps directory.

Deploy the generated war package to the Tomcat server. After starting Tomcat, you can view the Lag[i] page by visiting the corresponding port in your browser.

For example: local port 8080：http://localhost:8080/

Local access:
![img.png](images/img.png)

## 6. Testing lags [i]

Using the browser to visit Lag[i] page, you can use the provided sample code or API interface to test, such as text dialogue, speech recognition, text-to-speech, image generation and other functions.

Text Dialogue:
![img_2.png](images/img_2.png)

## 7. Model switching

Lag[i] provides the ability to switch models dynamically. You can set up multiple models in the configuration file and select different models to switch according to your needs.

1.Modify the configuration switching model.

- Change the `lagi.yml` config file to set `enable` to `true` the model you want to use. When the current service is down under non-streaming calls, other models are automatically enabled based on the `priority` value.

```shell
    - backend: chatglm
      model: glm-3-turbo
      enable: true
      stream: true
      priority: 10

    - backend: ernie
      model: ERNIE-Speed-128K
      enable: false
      stream: true
      priority: 10
```

2.Switch models online and choose the one you like.

Online switching:
![img.png](images/img_3.png)

## 8. Extension

If you are not satisfied with the large model or vector database that Lag[i] has adapted，You can refer to [Extension documentation](extend_cn.md)，Extend Lag[i] to fit your favorite large model or vector database.

## Summary

With this tutorial, you have successfully integrated Lag[i] into your project and can start using the various AI features Lag[i] provides. Lag[i] 's power and flexible scalability can help you easily apply big model technology to your business, improving user experience and efficiency.