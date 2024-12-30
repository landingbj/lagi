# API参考指南

## 问答接口

POST `/chat/completions`

输入prompt，获取大模型的回答。

### 请求示例

```json
{
  "model": "gpt-3.5-turbo-1106",
  "temperature": 0.8,
  "max_tokens": 500,
  "category": "xxxx",
  "messages": [
    {
      "role": "user",
      "content": "查看大数据中心上云的单位数量。"
    }
  ]
}
```

### 请求参数

| 名称          | 位置 | 类型     | 必选 | 说明                                                         |
| ------------- | ---- | -------- | ---- | ------------------------------------------------------------ |
| body          | body | object   | 否   | none                                                         |
| » model       | body | string   | 否   | 模型类型                                                     |
| » temperature | body | number   | 是   | 用来控制语言模型生成文本的创造性和多样性的参数，范围再0-1之间，调低温度会让模型生成更加确定和高概率的文本，而调高温度则会增加文本的随机性和多样性。 |
| » max_tokens  | body | integer  | 是   | 可以生成的最大token数。                                      |
| » category    | body | string   | 否   | 数据类别                                                     |
| » messages    | body | [object] | 是   | 提交的消息列表                                               |
| »» role       | body | string   | 否   | user或者assistant, user表示用户提交，assistant表示大模型输出 |
| »» content    | body | string   | 否   | 请求内容                                                     |

### 返回示例

> 成功

```json
{
  "id": "chatcmpl-W9Hp9zbUjE572UKvPZvybz",
  "object": "chat.completion",
  "created": 1709287530,
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": " 知识图谱是一种表示和组织人类知识的结构化方式。它通过将知识点（即事实、概念、关系等）以及其相互联系的方式用图形化的方式呈现，以便人类或机器可以更好地理解和使用这些知识。知识图谱可以帮助人们更好地探索和理解一个特定领域的知识，并在需要时快速找到所需的信息。在本文的背景信息中，知识图谱被用于沉淀和存储专家知识，以支持业务决策和转化成盈利中心。"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 0,
    "completion_tokens": 0,
    "total_tokens": 0
  }
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称                 | 类型     | 必选  | 说明                                                       |
| -------------------- | -------- | ----- | ---------------------------------------------------------- |
| » id                 | string   | true  | 唯一标识符                                                 |
| » object             | string   | true  | 对象类型                                                   |
| » created            | integer  | true  | 聊天完成创建时的Unix时间戳（秒）                           |
| » choices            | [object] | true  | 选择的列表                                                 |
| »» index             | integer  | false | 对象的索引                                                 |
| »» message           | object   | false | 返回的消息                                                 |
| »»» role             | string   | true  | user或assistant, user表示用户提交，assistant表示大模型输出 |
| »»» content          | string   | true  | 大模型的输出内容                                           |
| »»» context          | string   | false | 向量数据库查出来的上下文信息                               |
| »» finish_reason     | string   | false | 模型停止生成的原因                                         |
| » usage              | object   | true  | 完成请求的使用统计                                         |
| »» prompt_tokens     | integer  | true  | 提示中的词元数量。                                         |
| »» completion_tokens | integer  | true  | 生成的词元数量                                             |
| »» total_tokens      | integer  | true  | 请求中使用的总词元数量                                     |


## 智能体调用接口

POST `/chat/go`

输入prompt，获取智能体的回答。

### 请求示例

```json
{
  "router": "pass",
  "agentId": "weather_agent",
  "temperature": 0.8,
  "max_tokens": 500,
  "messages": [
    {
      "role": "user",
      "content": "今天武汉的天气如何。"
    }
  ]
}
```

### 请求参数

| 名称              | 位置   | 类型       | 必选 | 说明                                                                                         |
|-----------------|------|----------|----|--------------------------------------------------------------------------------------------|
| body            | body | object   | 否  | none                                                                                       |
| » router         | body | string   | 是  | 路由规则,该值取于router 配置项, 具体请参考[路由配置](./config.md#路由配置) 接口会按规则调用智能体                             |
| » agentId      | body | string   | 否  | 当指定路由规则为%通配时，能指定调用所有智能体, 值为智能体配置里配置的智能体名字。                                                 |
| » temperature   | body | number   | 是  | 用来控制语言模型生成文本的创造性和多样性的参数，范围再0-1之间，调低温度会让模型生成更加确定和高概率的文本，而调高温度则会增加文本的随机性和多样性。 |
| » max_tokens    | body | integer  | 是  | 可以生成的最大token数。                                                                             |
| » messages      | body | [object] | 是  | 提交的消息列表                                                                                    |
| »» role         | body | string   | 否  | user或者assistant, user表示用户提交，assistant表示智能体输出                                          |
| »» content      | body | string   | 否  | 请求内容                                                                                       |

### 返回示例

> 成功

```json
{
  "source": "weather_agent",
  "created": 0,
  "choices": [
    {
      "index": 0,
      "message": {
        "content": "今天武汉的天气是晴天，温度为7℃，湿度为59%，风力小于等于3级，风向为北。更新时间为2024-12-16 18:01:56。"
      }
    }
  ]
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称                   | 类型       | 必选    | 说明                                            |
|----------------------|----------|-------|-----------------------------------------------|
| » id                 | string   | true  | 唯一标识符                                         |
| » object             | string   | true  | 对象类型                                          |
| » created            | integer  | true  | 聊天完成创建时的Unix时间戳（秒）                            |
| » source             | integer  | true  | 源智能体名称                                        |
| » imageList          | array   | true  | 图片理解列表                                        |
| » filepath          | array   | true  | 文件列表                                          |
| » created            | integer  | true  | 聊天完成创建时的Unix时间戳（秒）                            |
| » choices            | [object] | true  | 选择的列表                                         |
| »» index             | integer  | false | 对象的索引                                         |
| »» message           | object   | false | 返回的消息                                         |
| »»» role             | string   | true  | user或assistant, user表示用户提交，assistant表示大模型输出 |
| »»» content          | string   | true  | 大模型的输出内容                                      |
| »»» context          | string   | false | 向量数据库查出来的上下文信息                                |
| »» finish_reason     | string   | false | 模型停止生成的原因                                     |
| » usage              | object   | true  | 完成请求的使用统计                                     |
| »» prompt_tokens     | integer  | true  | 提示中的词元数量。                                     |
| »» completion_tokens | integer  | true  | 生成的词元数量                                       |
| »» total_tokens      | integer  | true  | 请求中使用的总词元数量                                   |
| »» total_tokens      | integer  | true  | 请求中使用的总词元数量                                   |

## 语音识别

POST `/audio/speech2text`

语音识别接口，返回识别后的文字。

### 请求参数

请求体传入的是二进制音频数据，因此在HTTPS请求头部中的`Content-Type`必须设置为`application/octet-stream`。

| 名称           | 位置     | 类型             | 必选 | 说明   |
|--------------|--------|----------------|----|------|
| Content-Type | header | string         | 否  | none |
| body         | body   | string(binary) | 否  | none |

### 返回示例

> 成功

```json
{
  "result": "万一出现了这种情况你准备怎么办",
  "status": 20000000
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称       | 类型      | 必选   | 说明      |
|----------|---------|------|---------|
| » result | string  | true | 语音识别结果。 |
| » status | integer | true | 服务状态码。  |

## 文字转语音

GET `/audio/text2speech`

输入文字，返回播报的语音文件。

### 请求参数

| 名称   | 位置    | 类型     | 必选 | 说明     |
|------|-------|--------|----|--------|
| text | query | string | 是  | 待合成的文本 |

### 响应结果

- 成功响应
    - HTTPS Headers的`Content-Type`字段内容为`audio/mpeg`，表示合成成功，合成的语音数据在响应体中。
    - 响应内容为合成音频的二进制数据。
- 失败响应
    - HTTP Headers没有`Content-Type`字段，或者`Content-Type`字段内容为`application/json`，表示合成失败，错误信息在响应体中。

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|------|-------|----|
| 200  | OK    | 成功 |

## 图片生成

POST `/image/text2image`

输入生成图片的指令返回图片。

### 请求示例

```json
{
  "prompt": "a.pig"
}
```

### 请求参数

| 名称        | 位置   | 类型     | 必选 | 说明      |
|-----------|------|--------|----|---------|
| body      | body | object | 否  | none    |
| » prompt  | body | string | 是  | 生成图片的指令 |

### 返回示例

> 成功

```json
{
  "created": 1709288374,
  "data": [
    {
      "url": "http://116.255.226.214:9000/stable-diffusion/tlnniooq240301181913.png?response-content-type=image%2F%2A&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=9OMV1OGIpDH29iDq1HWC%2F20240301%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240301T101913Z&X-Amz-Expires=7200&X-Amz-SignedHeaders=host&X-Amz-Signature=6670b8278a61aa8953e424da4c77386c120821ab5bf49ee31970b027a88430ae"
    }
  ]
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称         | 类型       | 必选    | 说明                 |
|------------|----------|-------|--------------------|
| » created  | integer  | true  | 聊天完成创建时的Unix时间戳（秒） |
| » data     | [object] | true  | 生成的图片数据            |
| »» url     | string   | false | 生成的图片地址            |

## 上传私训学习文件

POST `/training/upload`

上传私训学习文件，支持txt、word、pdf格式

### 请求示例

```yaml
fileToUpload: file://D:/知识图谱.pdf
```

### 请求参数

| 名称              | 位置    | 类型             | 必选 | 说明       |
|-----------------|-------|----------------|----|----------|
| category        | query | string         | 是  | 指定的数据类别  |
| body            | body  | object         | 否  | none     |
| » fileToUpload  | body  | string(binary) | 是  | 所上传的私训文件 |

### 返回示例

> 成功

```json
{
  "result": true
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称        | 类型      | 必选   | 说明        |
|-----------|---------|------|-----------|
| » result  | boolean | true | 上传私训文件的状态 |

## 私训问答对数据

POST `/training/pairing`

私训问答对数据，要求为json格式

### 请求示例

data和instruction字段支持对象或对象列表，参考如下请求示例。

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

```json
{
  "category": "default",
  "data": [
    {
      "instruction": "补办医师执业证书的整个流程包括哪些步骤？",
      "output": "补办医师执业证书的流程包括五个步骤：申报/收件、受理、决定、制证、发证。"
    },
    {
      "instruction": "医师执业证书补办流程有哪些环节？",
      "output": "补办医师执业证书的流程包括五个步骤：申报/收件、受理、决定、制证、发证。",
      "image":"[{\"path\": \"https://downloads.saasai.top/vector/szu/8EB8BC9D3E5F4D987BBDB93ECEB_58E46C1C_6DCB0.png\"}]"
    }
  ]
}
```

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

### 请求参数

| 名称             | 位置   | 类型                 | 必选 | 说明       |
|----------------|------|--------------------|---|----------|
| body           | body | object             | 否 | none     |
| » category     | body | string             | 是 | 指定的数据类别  |
| » data         | body | [object] or object | 是 | 问答对数据    |
| »» instruction | body  | [object] or object | 是 | 问题字符串或集合 |
| »» output      | body  | [object] or object | 是 | 答案字符串或集合 |
| »» image       | body  | [object] or object | 否 | 相关图片对象集合 |

### 返回示例

> 成功

```json
{
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称       | 类型     | 必选   | 说明       |
|-----------|--------|------|----------|
| » result  | string | true | 问答对数据的状态 |

## 向量数据库查询

如模型响应的答案和您的答案之间存在差异，您可以使用`POST /v1/vector/query`接口，查看您的答案。

### 请求示例

```json
{
    "text": "哪能寄存行李？", 
    "n": 6,
    "where": {},
    "category": "default"
}
```

| 名称       | 位置   | 类型                 | 必选   | 说明      |
| -------- | ---- | ------------------ | ---- | ------- |
| text     | body | string             | true | 问题      |
| n        | body | integer            | true | 回答的条数   |
| where    | body | [object] or object | true | 条件      |
| category | body | string             | true | 指定的数据类别 |

### 返回示列

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
    }
  ],
  "status": "success"
}
```

### 返回数据结构

| 名称         | 类型     | 必选  | 说明                                       |
| ------------ | -------- | ----- | ------------------------------------------ |
| data         | [object] | true  | 选择的列表                                 |
| status       | string   | true  | 服务状态码。                               |
| » document   | string   | true  | 命中问题                                   |
| » id         | string   | true  | 该数据id                                   |
| » distance   | flat     | true  | 向量距离                                   |
| » metadata   | [object] | true  | 上传对象信息                               |
| »» category  | string   | true  | 指定的数据类别                             |
| »» level     | string   | true  | 上传角色                                   |
| »» parent_id | string   | false | 该条答案对应问题的id(一般只会在答案中出现) |

## 问答删除

如果某些问答对与您的整体问题集不相关或质量较低，您可以通过 `POST /v1/vector/deleteByld`接口将其从数据集中移除，以避免对模型训练产生负面影响。

### 请求示例

```json
{
    "category":"default",
    "ids":[
        "a4ac6c2511e94a54b454f1daaa270ee5"
    ]
}
```

| 名称       | 位置   | 类型             | 必选   | 说明      |
| -------- | ---- | -------------- | ---- | ------- |
| category | body | string         | true | 指定的数据类别 |
| ids      | body | List< string > | true | 数据id集合  |

### 返回示列

```json
{
    "status": "success"
}
```

### 返回数据结构

| 名称     | 类型     | 必选   | 说明    |
| ------ | ------ | ---- | ----- |
| status | string | true | 服务状态码 |

## 看图说话

POST `/image/image2text`

上传一张图片返回图片的描述。

### 请求示例

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg
```

### 请求参数

| 名称     | 位置   | 类型             | 必选 | 说明       |
|---------|------|----------------|----|----------|
| body    | body | object         | 否  | none     |
| » file  | body | string(binary) | 是  | 所上传的图片文件 |

### 返回示例

> 成功

```json
{
  "classification": "叉车 加油泵 ",
  "caption": "一堆自行车停放在街边",
  "samUrl": "http://116.255.226.214:9000/sam/qlztvdkv240301172715.png?response-content-type=image%2F%2A&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=9OMV1OGIpDH29iDq1HWC%2F20240301%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240301T092715Z&X-Amz-Expires=7200&X-Amz-SignedHeaders=host&X-Amz-Signature=0199b49a10eb6a18f01c4cec8e10549642ee6953c0f00c308bfc644e55d86c66",
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称                | 类型     | 必选   | 约束   | 中文名 | 说明        |
|-------------------|--------|------|------|-----|-----------|
| » status          | string | true | none |     | 看图说话的调用状态 |
| » classification  | string | true | none |     | 识别出图片的类别  |
| » caption         | string | true | none |     | 返回图片的识别结果 |
| » samUrl          | string | true | none |     | 上传图片的切割结果 |

## 视频追踪

POST `/video/video2tracking`

上传一段视频实现视频追踪。

### 请求示例

```yaml
file: file://D:\Test\Datasets\Video\demo.mp4
```

### 请求参数

| 名称      | 位置   | 类型             | 必选 | 说明      |
|---------|------|----------------|----|---------|
| body    | body | object         | 否  | none    |
| » file  | body | string(binary) | 是  | 上传的视频文件 |

### 返回示例

> 成功

```json
{
  "data": "static/video/d01476eb-80de-4a7d-a5bb-566644c96a81.mp4",
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称        | 类型     | 必选   | 说明          |
|-----------|--------|------|-------------|
| » status  | string | true | 返回的结果状态     |
| » data    | string | true | 返回视频追踪的视频地址 |

## 图像增强

POST `/image/image2enhance`

上传一张图片可以将模糊的图片进行增强。

### 请求示例

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg
```

### 请求参数

| 名称      | 位置   | 类型             | 必选 | 说明      |
|---------|------|----------------|----|---------|
| body    | body | object         | 否  | none    |
| » file  | body | string(binary) | 是  | 上传的图片文件 |

### 返回示例

> 成功

```json
{
  "status": "success",
  "enhanceImageUrl": "http://116.255.226.214:9000/realesrgan/tfukxzrq240301172914.png?response-content-type=image%2F%2A&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=9OMV1OGIpDH29iDq1HWC%2F20240301%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240301T092921Z&X-Amz-Expires=7200&X-Amz-SignedHeaders=host&X-Amz-Signature=931b1c9d1b850095d58763db042bd982108dc0c2415985df37d6efa824f33fff"
}
```

### 返回结果

| 状态码  | 状态码含义 | 说明 |
|------|-------|----|
| 200  | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称                 | 类型     | 必选   | 说明          |
|--------------------|--------|------|-------------|
| » enhanceImageUrl  | string | true | 返回的结果状态     |
| » status           | string | true | 返回图像增强的图片地址 |

## 图生视频

POST `/image/image2video`

上传一张图片，然后根据该图片生成一个短视频。

### 请求示例

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg
```

### 请求参数

| 名称      | 位置   | 类型             | 必选 | 说明      |
|---------|------|----------------|----|---------|
| body    | body | object         | 否  | none    |
| » file  | body | string(binary) | 是  | 上传的图片文件 |

### 返回示例

> 成功

```json
{
  "svdVideoUrl": "static/img/svd/7ef10bdd-1c72-4091-a7aa-db651966f076.mp4",
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称             | 类型     | 必选   | 说明          |
|----------------|--------|------|-------------|
| » svdVideoUrl  | string | true | 返回视频生成的视频地址 |
| » status       | string | true | 返回的结果状态     |

## 视频增强

POST `/video/video2enhance`

上传一段视频实现视频插帧。

### 请求示例

```yaml
file: file://D:\Test\Datasets\Video\demo.mp4
```

### 请求参数

| 名称      | 位置   | 类型             | 必选 | 说明      |
|---------|------|----------------|----|---------|
| body    | body | object         | 否  | none    |
| » file  | body | string(binary) | 是  | 上传的视频文件 |

### 返回示例

> 成功

```json
{
  "data": "static/video/d01476eb-80de-4a7d-a5bb-566644c96a81.mp4",
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称        | 类型     | 必选   | 说明          |
|-----------|--------|------|-------------|
| » status  | string | true | 返回的结果状态     |
| » data    | string | true | 返回视频增强的视频地址 |

## 图文识别

POST `/image/image2ocr`

上传一张图片，识别该图片上的文字。

### 请求示例

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg
```

### 请求参数

| 名称    | 位置  | 类型            | 必选    | 说明   |
|--------|------|----------------|--------|---------|
| body   | body | object         | 否      | none    |
| » file | body | string(binary) | 是      | 上传图片文件|

### 返回示例

> 成功

```json
{
  "data": ["图文识别转换技术的发展极大地提高了人们处理信息的效率，使得信息更加便于存储、检索和分析。"],
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明   |
|-----|-------|------|
| 200 | OK    | 成功   |

### 返回数据结构

状态码 **200**

| 名称        | 类型             | 必选   | 说明        |
|-----------|----------------|------|-----------|
| » status  | string         | true | 返回的结果状态   |
| » data    | List< string > | true | 返回识别的文字数组 |

## 文本生成SQL

POST `/sql/text2sql`

选择一张表后输入需求，生成一个可执行的SQL语句。

### 请求示例

```json
{
  "demand":"帮我查一下京伦饭店的情况",
  "tables":"ai.hotel_agreement",
  "storage": "mysql"
}
```

### 请求参数

| 名称        | 位置  | 类型     | 必选 | 说明                                                                  |
|-----------|------|---------|----|---------------------------------------------------------------------|
| body      | body | object  | 否  | none                                                                |
| » demand  | body | string  | 是  | 用户需求                                                                |
| » tables  | body | string  | 是  | 用户选定的表,多表之间用逗号拼接(命名规则：数据库名.数据表名 例如：ai.hotel_agreement,ai.library)   |
| » storage | body | string  | 是  | 数据库配置名称                                                             |      

### 返回示例

> 成功

```json
{
  "data": {
    "sql": "SELECT * FROM hotel_agreement WHERE hotel_name LIKE '%京伦饭店%';",
    "demand": "帮我查一下京伦饭店的情况",
    "tables": "ai.hotel_agreement",
    "storage": "mysql"
  },
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明   |
|------|-------|------|
| 200  | OK    | 成功   |

### 返回数据结构

状态码 **200**

| 名称         | 类型       | 必选    | 说明                                                      |
|------------|----------|-------|---------------------------------------------------------|
| » status   | string   | true  | 返回的结果状态                                                 |
| » data     | object   | true  | 返回内容                                                    |
| »» sql     | string   | true  | 生成的SQL语句                                                |
| »» demand  | string   | true  | 用户需求                                                    |
| »» tables  | string   | true  | 用户选定的表,多表之间用逗号拼接(命名规则：数据库名.数据表名 例如：ai.hotel_agreement,ai.library) |
| »» storage | string   | true  | 数据库配置名称                                                 |   

## SQL生成文本

POST `/sql/sql2text`

传入SQL查询文本信息。

### 请求示例

```json
{
  "sql": " SELECT * FROM hotel_agreement WHERE hotel_name LIKE '%京伦饭店%'; ",
  "demand": "帮我查一下京伦饭店的情况",
  "tables": "ai.hotel_agreement",
  "storage": "mysql"
}
```

### 请求参数

| 名称        | 位置  | 类型    | 必选 | 说明                                                                  |
|-----------|------|---------|----|---------------------------------------------------------------------|
| body      | body | object  | 否  | none                                                                |
| » sql     | body | string  | 是  | 生成的SQL语句                                                            |
| » demand  | body | string  | 是  | 用户需求                                                                |
| » tables  | body | string  | 是  | 用户选定的表,多表之间用逗号拼接(命名规则：数据库名.数据表名 例如：ai.hotel_agreement,ai.library)   |
| » storage | body | string  | 是  | 数据库配置名称                                                             |

### 返回示例

> 成功

```json
{
  "data": "您好，关于您查询的京伦饭店的信息如下：\n\n- **城市**: 北京市\n- **协议价**: 650元/间（具体价格可能因房型和日期有所不同）\n- **距离集团**: 10.3公里\n- **酒店地址**: 北京市朝阳区建国门外大街3号\n- **联系方式**: 010-65002266转客房预定部\n- **数据来源**: 附件3：2024年中国电信集团总部及全国连锁协议酒店清单\n- **酒店名称**: 京伦饭店\n- **星级**: 四星\n- **房间类型**: 标准间\n- **备注**: 目前没有特别的备注信息\n\n如有其他问题，请随时告知。",
  "list": [
    {
      "city": "北京市",
      "unavailable_dates": "",
      "agreement_price": "650，600",
      "distance_from_group": "10.3",
      "hotel_address": "北京市朝阳区建国门外大街3号",
      "contact_info": "010-65002266转客房预定部",
      "data_source": "附件3：2024年中国电信集团总部及全国连锁协议酒店清单",
      "hotel_name": "京伦饭店",
      "tier": "档位一：600元以内/天标准-1人入住推荐（含单早）",
      "province": "北京",
      "applicable_brand": "",
      "star_rating": "四星",
      "id": 10,
      "remarks": "",
      "room_type": "标准间"
    }
  ],
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明   |
|------|-------|------|
| 200  | OK    | 成功   |

### 返回数据结构

状态码 **200**

| 名称     | 类型   | 必选 | 说明             |
| -------- | ------ | ---- | ---------------- |
| » status | string | true | 返回的结果状态   |
| » data   | string | true | 返回内容         |
| » list   | object | true | 数据表中询的内容 |

## 生成指令集

POST `/instruction/generate`  

上传pdf文件，从文件中提取可用于训练的指令集。  

### Body 请求参数

```yaml
fileToUpload: file://E:\file\知识图谱.PDF
```

### 请求参数

| 名称         | 位置 | 类型 | 必选 | 说明     |
| ------------ | ---- | ---- | ---- | -------- |
| fileToUpload | body | File | 是   | 上传文件 |

### 返回示例

> 成功  

```json
{
    "data": [
        {
            "instruction": "知识图谱的构建通常采用哪种方式？",
            "input": "",
            "output": "知识图谱的构建通常采用自底向上的方式进行。"
        },
        {
            "instruction": "如何利用知识图谱在金融领域实现业务场景的分析与决策，以及其在数据变现方面的优势？",
            "input": "",
            "output": "利用知识图谱在金融领域实现业务场景的分析与决策主要通过以下几个方面：首先，知识图谱能够帮助建立客户画像，通过对用户行为、偏好等多维度数据分析，形成详细的用户画像；其次，基于知识图谱可以进行精准营销获客，通过客户画像识别潜在客户，并向他们推送相关产品或服务，提高转化率；再次，知识图谱还能够用于发现异常交易模式，如信用卡套现、资金挪用等非法活动，提高风险控制能力。\n\n在数据变现方面的优势主要包括：知识图谱能够更好地表达、分析金融业务场景的交易全貌，使金融机构能够更高效地利用自身拥有的海量、多结构多维度的数据资源（符合大数据的“4V”特征），并将其转化为有价值的信息和服务；此外，基于知识图谱的智能运维系统能够将运维人员的经验和知识转化为可复用的知识库，提高工作效率，降低成本，进一步促进数据的价值提升和变现。因此，知识图谱在金融领域的应用有助于推动金融机构从成本中心向盈利中心转变。"
        },
        {
            "instruction": "知识图谱在金融领域中的风控应用具体包括哪些方面？",
            "input": "",
            "output": "知识图谱在金融领域中的风控应用具体包括信用风险评估、欺诈检测、交易监控和风险预警等方面。通过构建实体之间的关联关系，知识图谱能够帮助金融机构更准确地识别潜在的风险点，提高风险管理的效率和效果。"
        }
    ],
    "status": "success"
}
```

### 返回结果

| 状态码  | 状态码含义 | 说明 |
| ------- | ---------- | ---- |
| success | OK         | 成功 |

### 返回数据结构

状态码 **200**  

| 名称        | 类型   | 必选 | 说明 |
| ----------- | ------ | ---- | ---- |
| instruction | string | true | 问题 |
| input       | string | true |      |
| output      | string | true | 回答 |

根据您提供的 PDF 文件内容，我可以将其解析成如下格式的训练指令集：

```json
{
    "data": [
        {
            "instruction": "知识图谱的概念由谁提出，其目标是什么？",
            "input": "",
            "output": "知识图谱的概念由 Google 在 2012 年 5 月提出，其目标是改善搜索结果，描述真实世界中存在的各种实体和概念，以及这些实体、概念之间的关联关系。"
        },
        {
            "instruction": "传统知识图谱的架构特点是什么？",
            "input": "",
            "output": "传统知识图谱的架构包括自身的逻辑结构和体系架构，通常采用自底向上的方式构建。以实体概念为节点，以关系为边，从关系的视角描述客观世界。"
        },
        {
            "instruction": "基于知识图谱的智能运维系统有哪些优势？",
            "input": "",
            "output": "基于知识图谱的智能运维系统能够结合大数据和机器学习，不断完善知识图谱，成为运维大脑。其作用在于支持业务决策，将运维从成本中心转变为盈利中心。"
        }
    ],
    "status": "success"
}
```
