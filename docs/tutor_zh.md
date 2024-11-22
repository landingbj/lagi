# 教学演示

Lag[i] (联基) 是一款强大的企业级复合多模态大模型中间件，它可以帮助您轻松地将大模型技术集成到您的业务中。本教程将引导您从零开始，完成 Lag[i] (联基) 的下载、安装、配置和运行，让您快速掌握 Lag[i] (联基) 的使用方法。

## 环境准备

在开始之前，请确保您已经准备好以下环境：

*   **Java 8 或更高版本**
*   **Maven**
*   **Docker (可选，用于运行向量数据库)**

## 1. 下载 Lag[i] (联基)

对于开发者而言，我们提供了简便的方法来编译和运行Lag[i] (联基) 应用。您可以选择使用maven命令行工具进行封包，或者通过IntelliJ IDEA等主流的集成开发环境（IDE）进行运行。

### 方法一：使用maven命令

1. **克隆项目**：首先，您需要克隆Lag[i] (联基) 项目的仓库:

```shell
git clone https://github.com/landingbj/lagi.git
```
2. **进入项目**：切换到项目目录：

```shell
cd lagi
```

3. **编译项目**：在项目根目录下运行 Maven 命令进行编译：

```bash
mvn clean install
```

### 方法二：使用IDE

1. **选择 IDE**： 您可以选择使用 IntelliJ IDEA 或 Eclipse 等主流 IDE。

2. **打开 GitHub 仓库**：在 IDE 中连接 Lag[i] (联基) 的 GitHub 仓库，使用 IDE 的克隆功能，将 Lag[i] (联基) 项目克隆到本地。

|        | GitHub 仓库                             | 
|--------|---------------------------------------| 
| SSH    | git@github.com:landingbj/lagi.git     |
| HTTPS  | https://github.com/landingbj/lagi.git | 

3. **编译项目**： 使用 IDE 的编译功能，编译 Lag[i] (联基) 项目。

## 2. 安装向量数据库 

Lag[i] (联基) 支持多种向量数据库，例如 ChromaDB。如果您想使用检索增强 RAG 功能，需要安装向量数据库。

**以 ChromaDB 为例**:

### 方式一：Python

***确保有安装有Python运行环境***

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

**注意：**

在导包chromadb时报错，原因是sqlite3版本太低

>RuntimeError: Your system has an unsupported version of sqlite3. Chroma requires sqlite3 >= 3.35.0.

请按以下步骤操作

- 1.安装pysqlite3-binary

```bash
pip install pysqlite3-binary
```

- 2.导chromadb包时，覆盖原sqlite3 库 找到你的chromadb源码编辑`__init__.py`文件

```bash
vim xxx/chromadb/__init__.py
```
- 3.在开头添加3行代码

```text
__import__('pysqlite3')
import sys
sys.modules['sqlite3'] = sys.modules.pop('pysqlite3')
```

- 4.启动数据库服务
![img_4.png](images/img_4.png)

安装完成，您可以通过浏览器访问：http://localhost:8000/docs 查看是否启动成功。

chromadb：
![img_1.png](images/img_1.png)

## 3.配置yml文件

修改`lagi.yml`配置文件，选择您喜欢的模型，将其中的模型的`your-api-key`等信息替换为您自己的密钥，并根据需求将启用的模型的`enable`字段设置为`true`。

***以配置kimi为列：***

- 填入模型信息并开启模型,修改enable设置为true。

    ```yaml
      - name: kimi
        type: Moonshot
        enable: true
        model: moonshot-v1-8k,moonshot-v1-32k,moonshot-v1-128k
        driver: ai.llm.adapter.impl.MoonshotAdapter
        api_key: your-api-key  
    ```

- 根据您的需求，设置模型输出的方式stream和优先级priority，值越大优先级越高。

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

选择配置的向量数据库，并填入对应的配置信息。

***以配置本地chromadb为例：***

- 替换url地址为chromadb的url地址http://localhost:8000。

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
        vector: chroma
        # fulltext: elasticsearch
        graph: landing
        enable: true
        priority: 10
        default: "Please give prompt more precisely"
    ```

## 4.引入依赖

调用lag[i] (联基) 相关API接口需引入依赖，您可以通过maven引入或直接导入jar的方式。

***以maven引入为例：***

- 使用maven下载依赖执行命令。

    ```shell
    mvn clean install
    ```

## 5.启动web服务。

您可以选择使用maven命令行工具进行封包，或者通过IntelliJ IDEA等主流的集成开发环境（IDE）进行运行。

***以maven命令行工具封包为例：***

- 1.使用maven命令进行项目封包，封包完成后将会在`target`目录下生成一个war文件。

    ```shell
    mvn package
    ```

  - 2.部署到 Web 服务器: 将打包后的文件部署到 Web 服务器中。  
  
  Tomcat:
  > 将 WAR 文件复制到 Tomcat 的 webapps 目录中。

将生成的war包部署到Tomcat服务器中。启动Tomcat后，通过浏览器访问对应的端口，即可查看Lag[i] (联基) 的具体页面。

例如：本地8080端口：http://localhost:8080/

本地访问：
![img.png](images/img.png)

## 6. 测试 Lag[i] (联基)

使用浏览器访问 Lag[i] (联基) 页面，您可以使用提供的示例代码或 API 接口进行测试，例如文本对话、语音识别、文字转语音、图片生成等功能。

文本对话：
![img_2.png](images/img_2.png)

## 7. 模型切换

Lag[i] (联基) 提供了动态切换模型的功能，您可以在配置文件中设置多个模型，并根据需求选择不同的模型进行切换。

1.修改配置切换模型。

- 通过修改`lagi.yml`配置文件，将需要使用的模型设置为`enable`为`true`。在非流式调用下当前服务宕机，会根据`priority`值自动启用其他模型。

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

2.在线切换模型，选择您喜欢的模型。

在线切换：
![img.png](images/img_3.png)

## 8. 自由扩展

如果您对 Lag[i] (联基) 已适配的大模型或向量数据库不满意，您可以参考[扩展文档](extend_cn.md)，对 Lag[i] (联基) 进行扩展，适配您喜欢的大模型或向量数据库。

## 9. 私训问答对

您可以通过上传问答对的形式，将内部数据信息集成至Lag[i] (联基)，从而定制训练一个专属的大模型。模型训练过程中`distance`代表您的问题与上传的问答对之间的距离，该值越小，说明您的问题与上传的问答对越相似度越高。如果模型识别的最匹配问答对与您问题的实际意图不符，您可以采取问答对的新增，删除来进一步优化模型的性能。通过不断地调整和优化您的问答对数据，您可以逐步提高模型对您问题的理解能力，从而提高系统的准确性。

### 1.上传问答对

您可以使用`POST /training/pairing`接口，上传特定的问答对。

| 名称           | 位置    | 类型                 | 必选  | 说明              |
|--------------|-------|--------------------|-----|-----------------|
| category     | body  | string             | 是   | 指定的数据类别         |
| data         | body  | [object] or object | 是   | 问答对数据，支持对象或对象列表 |
| 》instruction | body  | [object] or object | 是   | 问题字符或集合         |
| 》output      | body  | [object] or object | 是   | 答案字符或集合          |
| 》image       | body  | [object] or object | 否   | 相关图片对象集合        |

如上传单个问答对，请求示例如下（一对一）：

```json
{
    "category": "default",
    "data": {
        "instruction": "补办医师执业证书的整个流程包括哪些步骤？",
        "output": "补办医师执业证书的流程包括五个步骤：申报/收件、受理、决定、制证、发证。",
        "image":"[{\"path\": \"https://downloads.saasai.top/vector/szu/8EB8BC9D3E5F4D987BBDB93ECEB_58E46C1C_6DCB0.png\"}]"
    }
}
```

如上传多个问答对，请求示例如下（一对一）：

```json
{
    "category": "default",
    "data": [
        {
            "instruction": "补办医师执业证书的整个流程包括哪些步骤？",
            "output": "补办医师执业证书的流程包括五个步骤：申报/收件、受理、决定、制证、发证。",
            "image":"[{\"path\": \"https://downloads.saasai.top/vector/szu/8EB8BC9D3E5F4D987BBDB93ECEB_58E46C1C_6DCB0.png\"}]"
        },
        {
            "instruction": "医师执业证书补办流程有哪些环节？",
            "output": "补办医师执业证书的流程包括五个步骤：申报/收件、受理、决定、制证、发证。"
        }
    ]
}
```

如上传单个问答对，请求示例如下（多对一）：

```json
{
    "category": "default",
    "data": [
        {
            "instruction": [
                "补办医师执业证书的整个流程包括哪些步骤？",
                "医师执业证书补办流程有哪些环节？"
            ],
            "output": "补办医师执业证书的流程包括五个步骤：申报/收件、受理、决定、制证、发证。",
            "image":"[{\"path\": \"https://downloads.saasai.top/vector/szu/8EB8BC9D3E5F4D987BBDB93ECEB_58E46C1C_6DCB0.png\"}]"
        }
    ]
}
```

返回示列：

```json
{
    "status": "success"
}
```

返回数据结构:

| 名称     | 类型     | 必选    |说明|
|---------|----------|-------|---|
| result  | boolean  | true  |上传私训文件的状态|


### 2.问答校验

如模型响应的答案和您的答案之间存在差异，您可以使用`POST /v1/vector/query`接口，查看您的答案。

请求示例如下：

```json
{
    "text": "哪能寄存行李？", 
    "n": 6,
    "where": {},
    "category": "default"
}
```

| 名称      | 位置  | 类型                 | 必选 | 说明      |
|----------|------|--------------------|----|---------|
| text     | body | string             | true  | 问题      |
| n        | body | integer            | true  | 回答的条数   |
| where    | body | [object] or object | true  | 条件      |
| category | body | string             | true  | 指定的数据类别 |

返回示列：

```json
{
  "data": [
    {
      "document": "哪能寄存行李？\n",
      "id": "a5a74ace0f7d4339b52feb8900c6dc77",
      "metadata": {
        "category": "default",
        "level": "user"
      },
      "distance": 0.041246016
    },
    {
      "document": "行李寄存在哪？\n",
      "id": "16061c3e59344544987806ed457285a2",
      "metadata": {
        "category": "default",
        "level": "user"
      },
      "distance": 0.22894014
    },
    {
      "document": "行李寄存有什么要求\n",
      "id": "80a5d0abcf804e16b0227c95e48c671e",
      "metadata": {
        "category": "default",
        "level": "user"
      },
      "distance": 0.31431544
    },
    {
      "document": "行李寄存可以寄存多久\n",
      "id": "1aceb011d1c947e6acfdf8c7d389c852",
      "metadata": {
        "category": "default",
        "level": "user"
      },
      "distance": 0.3469293
    },
    {
      "document": "行李寄存如何收费\n",
      "id": "ace15a4357a24b5aa2af148847f3e757",
      "metadata": {
        "category": "default",
        "level": "user"
      },
      "distance": 0.36549693
    },
    {
      "document": "您好，行李寄存会按照您行李尺寸规格收费，根据尺寸有10、15、20元每件不等。寄存以24小时为一天，不足24小时的按24小时计，具体收费以现场为准。易燃、易爆、腐蚀、放射性等危险品不得寄存。贵重物品如电脑，单品价值超过2000元、易腐易烂活体、充电宝、锂电池、骨灰盒等不予以寄存。",
      "id": "f6c500e9f8814e6089fe90b640777165",
      "metadata": {
        "category": "default",
        "level": "user",
        "parent_id": "ace15a4357a24b5aa2af148847f3e757"
      },
      "distance": 0.36806005
    }
  ],
  "status": "success"
}
```

返回数据结构:

| 名称           | 类型       | 必选   | 说明                        |
|--------------|----------|------|---------------------------|
| data         | [object] | true | 选择的列表                     |
| status       | string   | true | 服务状态码。                    |
| 》document    | string   | true | 命中问题                      |
| 》id          | string   | true | 该数据id                     |
| 》distance    | flat     | true | 向量距离                      |
| 》metadata    | [object] | true | 上传对象信息                    |
| 》》category   | string   | true | 指定的数据类别                   |
| 》》level      | string   | true | 上传角色                      |
| 》》parent_id  | string   | false | 该条答案对应问题的id(一般只会在答案中出现)   |


### 3.问答删除

如果某些问答对与您的整体问题集不相关或质量较低，您可以通过 `POST /v1/vector/deleteByld`接口将其从数据集中移除，以避免对模型训练产生负面影响。

请求示例如下：

```json
{
    "category":"default",
    "ids":[
        "a4ac6c2511e94a54b454f1daaa270ee5"
    ]
}
```

请求数据结构:

| 名称       | 位置  | 类型             | 必选    | 说明      |
|----------|------|----------------|-------|---------|
| category | body | string         | true  | 指定的数据类别 |
| ids      | body | List< string > | true  | 数据id集合  |

返回示列：

```json
{
    "status": "success"
}
```

返回数据结构

| 名称     | 类型     | 必选    |说明|
|---------|----------|-------|---|
| status  | string   | true | 服务状态码|

### 4.私训问答优化

您可以通过提高数据质量，优化问答对匹配，调整超参数，持续监控和更新等方法来提高模型对您问题的理解能力，从而提高系统的准确性。

1. **提高数据质量**

   数据清洗：确保训练数据集的质量，通过去除无效、错误或重复的数据，提高数据的准确性。

   数据预处理：对文本进行标准化、词干化（stemming）或词形还原（lemmatization），提高问题的多样性和代表性，确保问题覆盖不同的场景和语境，从而提高模型的泛化能力。

2. **问答对匹配**

   增加相关性：确保问题与答案之间的相关性，避免无关或低质量的问答对，可以根据向了距离（）对相关问题数据进行新增或删除。

   调节数据平衡：平衡不同类别的问题和答案，避免某个类别的问题或答案过于集中，对相关问题数据进行新增或删除。

3. **调整超参数**

   调整超参数：通过调整模型的超参数,来提升模型的性能和稳定性。

   例如：  
   超参数temperature，当它接近0时，模型的输出分布变得更加集中，模型倾向于选择概率最高的输出，使生成的文本或决策更加确定和可预测。当temperature接近无穷大时，模型的输出分布变得更加均匀更加多样和随机。

   迭代训练：通过多次迭代训练，逐步优化模型性能。

4. **持续监控和更新**

   持续监控和更新：定期监控模型在实际应用中的表现，及时更新模型或训练数据，以解决数据失效的问题。

## 总结

通过本教程，您已经成功地将 Lag[i] (联基) 集成到您的项目中，并可以开始使用 Lag[i] (联基) 提供的各种 AI 功能。Lag[i] (联基) 的强大功能和灵活的扩展性可以帮助您轻松地将大模型技术应用到您的业务中，提升用户体验和效率。
