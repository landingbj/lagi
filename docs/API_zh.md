## 问答接口

POST /v1/chat/completions

输入prompt，获取大模型的回答。

> Body 请求参数

```json
{
  "model": "D:/Workspaces/Data/opt-350m",
  "temperature": 0.8,
  "max_tokens": 500,
  "category": "bj_gov",
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
| » temperature | body | number   | 是   | 使用什么样的采样温度                                         |
| » max_tokens  | body | integer  | 是   | 可以生成的最大token数。                                      |
| » category    | body | string   | 否   | 数据类别                                                     |
| » messages    | body | [object] | 是   | 提交的消息列表                                               |
| »» role       | body | string   | 否   | user或者assistant, user表示用户提交，assistant表示大模型输出 |
| »» content    | body | string   | 否   | 如果role是user，则context是用户输入的内容吗， 如果role是assistant，则context是大模型的输出内容 |

> 返回示例

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

| 状态码 | 状态码含义                                              | 说明 |
| ------ | ------------------------------------------------------- | ---- |
| 200    | OK | 成功 |

### 返回数据结构

状态码 **200**

| 名称                 | 类型     | 必选  | 说明                                                         |
| -------------------- | -------- | ----- | ------------------------------------------------------------ |
| » id                 | string   | true  | 唯一标识符                                                   |
| » object             | string   | true  | 对象类型                                                     |
| » created            | integer  | true  | 聊天完成创建时的Unix时间戳（秒）                             | 
| » choices            | [object] | true  | 选择的列表                                                   |
| »» index             | integer  | false | 对象的索引                                                   |
| »» message           | object   | false | 返回的消息                                                   |
| »»» role             | string   | true  | user或者assistant, user表示用户提交，assistant表示大模型输出 |
| »»» content          | string   | true  | 如果role是user，则context是用户输入的内容吗， 如果role是assistant，则context是大模型的输出内容 |
| »» finish_reason     | string   | false | 模型停止生成的原因                                           |
| » usage              | object   | true  | 完成请求的使用统计                                           |
| »» prompt_tokens     | integer  | true  | 提示中的令牌数量。                                           |
| »» completion_tokens | integer  | true  | 生成的令牌数量                                               |
| »» total_tokens      | integer  | true  | 请求中使用的总令牌数量                                       |



## 语音识别

POST /v1/asr

语音识别接口，返回识别后的文字。

> Body 请求参数

请求体传入的是二进制音频数据，因此在HTTPS请求头部中的`Content-Type`必须设置为`application/octet-stream`。

### 请求参数

| 名称         | 位置   | 类型           | 必选 | 说明 |
| ------------ | ------ | -------------- | ---- | ---- |
| Content-Type | header | string         | 否   | none |
| body         | body   | string(binary) | 否   | none |

> 返回示例

> 成功

```json
{
  "result": "万一出现了这种情况你准备怎么办",
  "status": 20000000
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
| ------ | ---------- | ---- |
| 200    | OK         | 成功 |

### 返回数据结构

状态码 **200**

| 名称     | 类型    | 必选 | 说明           |
| -------- | ------- | ---- | -------------- |
| » result | string  | true | 语音识别结果。 |
| » status | integer | true | 服务状态码。   |



## 文字转语音

GET /v1/tts

输入文字，返回播报的语音文件。

### 请求参数

| 名称 | 位置  | 类型   | 必选 | 说明         |
| ---- | ----- | ------ | ---- | ------------ |
| text | query | string | 是   | 待合成的文本 |

### 响应结果

- 成功响应
  - HTTPS Headers的`Content-Type`字段内容为`audio/mpeg`，表示合成成功，合成的语音数据在响应体中。
  - 响应内容为合成音频的二进制数据。
- 失败响应
  - HTTP Headers没有`Content-Type`字段，或者`Content-Type`字段内容为`application/json`，表示合成失败，错误信息在响应体中。

### 返回结果

| 状态码 | 状态码含义                                              | 说明 |
| ------ | ------------------------------------------------------- | ---- |
| 200    | OK | 成功 |



## 图片生成

POST /v1/images/generations

输入生成图片的指令返回图片。

> Body 请求参数

```json
{
  "prompt": "a pig"
}
```

### 请求参数

| 名称     | 位置 | 类型   | 必选 | 说明           |
| -------- | ---- | ------ | ---- | -------------- |
| body     | body | object | 否   | none           |
| » prompt | body | string | 是   | 生成图片的指令 |

> 返回示例

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

| 状态码 | 状态码含义                                              | 说明 |
| ------ | ------------------------------------------------------- | ---- |
| 200    | OK | 成功 |

### 返回数据结构

状态码 **200**

| 名称      | 类型     | 必选  | 说明                             |
| --------- | -------- | ----- | -------------------------------- |
| » created | integer  | true  | 聊天完成创建时的Unix时间戳（秒） |
| » data    | [object] | true  | 生成的图片数据                   |
| »» url    | string   | false | 生成的图片地址                   |



## 上传私训学习文件

POST /uploadFile/uploadLearningFile

上传私训学习文件，支持txt、word、pdf格式

> Body 请求参数

```yaml
fileToUpload: file://D:/知识图谱.pdf

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|category|query|string| 是 |指定的数据类别|
|body|body|object| 否 |none|
|» fileToUpload|body|string(binary)| 是 |所上传的私训文件|

> 返回示例

> 成功

```json
{
  "result": true
}
```

### 返回结果

|状态码|状态码含义|说明|
|---|---|---|
|200|OK|成功|

### 返回数据结构

状态码 **200**

|名称|类型|必选|说明|
|---|---|---|---|
|» result|boolean|true|上传私训文件的状态|



## 看图说话

POST /image/imageToText

上传一张图片返回图片的描述。

> Body 请求参数

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» file|body|string(binary)| 是 |所上传的图片文件|

> 返回示例

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

|状态码|状态码含义|说明|
|---|---|---|
|200|OK|成功|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» status|string|true|none||看图说话的调用状态|
|» classification|string|true|none||识别出图片的类别|
|» caption|string|true|none||返回图片的识别结果|
|» samUrl|string|true|none||上传图片的切割结果|



## 视频追踪

POST /video/motInference

上传一段视频实现视频追踪。

> Body 请求参数

```yaml
file: file://D:\Test\Datasets\Video\demo.mp4

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» file|body|string(binary)| 是 |上传的视频文件|

> 返回示例

> 成功

```json
{
  "data": "static/video/d01476eb-80de-4a7d-a5bb-566644c96a81.mp4",
  "status": "success"
}
```

### 返回结果

|状态码|状态码含义|说明|
|---|---|---|
|200|OK|成功|

### 返回数据结构

状态码 **200**

|名称|类型|必选|说明|
|---|---|---|---|
|» status|string|true|返回的结果状态|
|» data|string|true|返回视频追踪的视频地址|



## 图像增强

POST /image/enhanceImage

上传一张图片可以将模糊的图片进行增强。

> Body 请求参数

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» file|body|string(binary)| 是 |上传的图片文件|

> 返回示例

> 成功

```json
{
  "status": "success",
  "enhanceImageUrl": "http://116.255.226.214:9000/realesrgan/tfukxzrq240301172914.png?response-content-type=image%2F%2A&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=9OMV1OGIpDH29iDq1HWC%2F20240301%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240301T092921Z&X-Amz-Expires=7200&X-Amz-SignedHeaders=host&X-Amz-Signature=931b1c9d1b850095d58763db042bd982108dc0c2415985df37d6efa824f33fff"
}
```

### 返回结果

|状态码|状态码含义|说明|
|---|---|---|
|200|OK|成功|

### 返回数据结构

状态码 **200**

|名称|类型|必选|说明|
|---|---|---|---|
|» enhanceImageUrl|string|true|返回的结果状态|
|» status|string|true|返回图像增强的图片地址|



## 视频生成

POST /image/generateVideo

上传一张图片，然后根据该图片生成一个短视频。

> Body 请求参数

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» file|body|string(binary)| 是 |上传的图片文件|

> 返回示例

> 成功

```json
{
  "svdVideoUrl": "static/img/svd/7ef10bdd-1c72-4091-a7aa-db651966f076.mp4",
  "status": "success"
}
```

### 返回结果

|状态码|状态码含义|说明|
|---|---|---|
|200|OK|成功|

### 返回数据结构

状态码 **200**

|名称|类型|必选|说明|
|---|---|---|---|
|» svdVideoUrl|string|true|返回视频生成的视频地址|
|» status|string|true|返回的结果状态|



## 视频增强

POST /video/mmeditingInference

上传一段视频实现视频插帧。

> Body 请求参数

```yaml
file: file://D:\Test\Datasets\Video\demo.mp4

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|object| 否 |none|
|» file|body|string(binary)| 是 |上传的视频文件|

> 返回示例

> 成功

```json
{
  "data": "static/video/d01476eb-80de-4a7d-a5bb-566644c96a81.mp4",
  "status": "success"
}
```

### 返回结果

|状态码|状态码含义|说明|
|---|---|---|
|200|OK|成功|

### 返回数据结构

状态码 **200**

|名称|类型|必选|说明|
|---|---|---|---|
|» status|string|true|返回的结果状态|
|» data|string|true|返回视频增强的视频地址|

