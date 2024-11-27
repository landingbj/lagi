# API 接口文档

## 问答接口

POST `/chat/completions`

输入prompt，获取大模型的回答。

### Body 请求参数

```json
{
  "stream": true,
  "temperature": 0.8,
  "max_tokens": 2048,
  "category": "bj-telecom",
  "messages": [
    {
      "role": "user",
      "content": "员工着装"
    }
  ]
}
```

### 请求参数

| 名称          | 位置 | 类型     | 必选 | 说明                                                         |
| ------------- | ---- | -------- | ---- | ------------------------------------------------------------ |
| body          | body | object   | 否   | none                                                         |
| » model       | body | string   | 否   | 模型类型                                                     |
| » stream      | body | boolean  | 否   | 是否使用流式接口                                             |
| » temperature | body | number   | 是   | 用来控制语言模型生成文本的创造性和多样性<br/>的参数，范围再0-1之间，调低温度会让模型<br/>生成更加确定和高概率的文本，而调高温<br/>度则会增加文本的随机性和多样性。 |
| » max_tokens  | body | integer  | 是   | 可以生成的最大token数。                                      |
| » category    | body | string   | 否   | 数据类别                                                     |
| » messages    | body | [object] | 是   | 提交的消息列表                                               |
| »» role       | body | string   | 否   | user或者assistant, user表示用户提交，<br/>assistant表示大模型输出 |
| »» content    | body | string   | 否   | 请求内容                                                     |

### 返回示例

> 成功

```json
{
  "id": "cmpl-1102e99eff3d44c082f1b16979b1c478",
  "object": "chat.completion",
  "created": 2243912,
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "在北京市电信公司，员工着装有着明确的规范，旨在保持良好的职业形象和办公环境的整洁。以下是一些详细的规定：\n\n1. 员工在工作场所需穿着整洁、合体的服装，不能有污迹、折皱、开线、掉扣。\n\n2. 着装要求衣物纽扣要扣齐，拉链要拉好，不能卷衣袖或裤腿。\n\n3. 皮鞋必须保持清洁，不允许赤脚或穿拖鞋。\n\n4. 女员工着装要淡雅且得体，避免过分华丽，不能穿裙装进入机房，夏季着装需注意遮体，裙摆应及膝，不能穿超短裙、短裤、拖鞋、吊带装和无袖无领背心。\n\n5. 男员工在正式场合需要佩戴领带，领带颜色要与西装和衬衫搭配，不能歪斜或松弛。夏季可着带领T恤或短袖正装衬衫，但不能穿圆领大背心和短裤。\n\n6. 营业厅员工需统一着工服上岗，工作结束后需将工服留在营业厅内。\n\n此外，关于仪容和仪表，公司也有相应的规定：\n\n1. 员工需保持头发干净整洁，不使用浓重的护发品。\n\n2. 须经常修剪指甲，保持干净，不能蓄长指甲或涂指甲油。\n\n3. 员工应注重个人卫生，避免汗味和异味。上班时不可戴有色眼镜，不能饮酒和吃有异味的食品，保持口腔清洁。\n\n4. 女员工上班时妆容应淡雅，发型整洁，不能蓬乱或烫染怪异的发型和发色。\n\n5. 男员工不能蓄胡须和留长发，鬓角不能过大，头发也不能烫发。\n\n违反这些规定的行为将受到处罚，公司会不定期进行检查，违规者将被扣发本人的绩效工资100元，并扣发直接上级主管领导的绩效工资200元。",
        "filename": [
          "关于规范北京市电信公司员工着装及仪容、仪表的通知.pdf"
        ],
        "filepath": [
          "202410101004103089.pdf"
        ],
        "context": "中国电信集团北京市电信有限公司文件 北京电信人发[2003]17 号 关于规范北京市电信公司 员工着装及仪容、仪表的通知 公司各部门： 为规范员工着装及礼仪行为，保证公司有一个健康、整洁的办公环境，体现北京市电信公司朝气蓬勃、积极向上的精神风貌，现对公司员工着装及仪容、仪表规范做出如下规定，请公司全体员工严格遵照执行。 一． 着装规范 （一）员工在工作场所应保持服装整洁、合体，不得有污迹、折皱、开线、掉扣。 （二）着装应将钮扣扣齐，拉链拉好，不可卷衣袖、裤腿。 （三）皮鞋应保持亮洁，不准赤脚、穿拖鞋。 （四）女员工着装应淡雅得体，不得过分华丽，不能穿裙装进入机房。夏季着装不能过于暴露，穿裙子应及膝，不能穿超短裙、短裤、拖鞋、吊带装及无袖无领背心。 （五）男员工佩戴领带时应注意与西装、衬衫颜色搭配，不得歪斜松弛。夏季可着带领 T 恤或正装短袖衬衣，不能穿圆领大背心及短裤。 （六）营业厅员工应统一着工服上岗，下班后不能将工服穿离营业厅。 二． 仪容、仪表规范 （一） 员工必须保持头发整洁卫生，不能擦浓重的护发用品。 （二） 须勤剪指甲，保持指甲干净，不得蓄长甲，涂指甲油。 （三） 注意个人卫生，无汗味、异味。上班时不能带有色眼镜，不得饮酒、吃带有异味的食品，保持口腔卫生。 （四） 女员工上班时不可浓妆艳抹，头发应梳理整齐，不能蓬乱或烫怪异发型及染怪异发色。 （五） 男员工不许蓄胡须及留长发，不能留大鬓角、烫发。 三． 处罚规定 公司将不定期对员工遵守本规定的情况进行抽查，如发现违反本规定者，将给予扣发本人绩效工资 100 元并扣发直接上级主管领导绩效工资 200 元的处罚。 二○ ○ 三年五月三十日 主题词： 着装 规范 通知 发：信元公司 印发单位：中国电信集团北京市电信有限公司总经理办公室 2003 年6 月2 日印发 ",
        "contextChunkIds": [
					"3d8f74e76c794c8ca60ca64155cab8d9",
					"48aaee358f1f410589cd54358722aec3"
				]
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 623,
    "completion_tokens": 442,
    "total_tokens": 1065
  }
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称                      | 类型       | 必选    | 说明                                               |
|-------------------------|----------|-------|--------------------------------------------------|
| » id                    | string   | true  | 唯一标识符                                            |
| » object                | string   | true  | 对象类型                                             |
| » created               | integer  | true  | 聊天完成创建时的Unix时间戳（秒）                               | 
| » choices               | [object] | true  | 选择的列表                                            |
| »» index                | integer  | false | 对象的索引                                            |
| »» message              | object   | false | 返回的消息                                            |
| »»» contextChunkIds     | [object] | false | 上下文对应的索引id列表                                     |
| »»» role                | string   | true  | user或assistant, user表示用户提交，<br/>assistant表示大模型输出 |
| »»» content             | string   | true  | 大模型的输出内容                                         |
| »»» context             | string   | false | 向量数据库查出来的上下文信息                                   |
| »» finish_reason        | string   | false | 模型停止生成的原因                                        |
| » usage                 | object   | true  | 完成请求的使用统计                                        |
| »» prompt_tokens        | integer  | true  | 提示中的令牌数量。                                        |
| »» completion_tokens    | integer  | true  | 生成的令牌数量                                          |
| »» total_tokens         | integer  | true  | 请求中使用的总令牌数量                                      |



## 订会议室识别

POST `/chat/extractAddMeetingInfo`

根据用户的订会议室请求，提取相关信息并转换为对应的json。用户如果不是一次把所有信息全部输入，需要多次调用该接口。除了第一个请求以外，每次调用该接口都应该把之前已经获取到的会议信息带上。

### Body 请求参数

```json
{
    "message": "会议开始时间17点30",
    "meetingInfo": {
        "meetingAddress": "酒仙桥",
        "date": "2024-09-28",
        "duration": "60",
        "attendance": "5"
    }
}
```

### 请求参数

| 名称              | 位置 | 类型   | 必选 | 说明                                                       |
| ----------------- | ---- | ------ | ---- | ---------------------------------------------------------- |
| body              | body | object | 否   | none                                                       |
| » message         | body | string | 是   | 用户输入的消息                                             |
| » meetingInfo     | body | object | 否   | 用户已经输入的会议信息，如果是第一个请求，该字段应该为空。 |
| »» meetingAddress | body | string | 否   | 会议地点                                                   |
| »» date           | body | string | 否   | 会议开始日期                                               |
| »» startTime      | body | string | 否   | 会议开始时间                                               |
| »» duration       | body | string | 否   | 会议时长                                                   |
| »» attendance     | body | string | 否   | 会议人数                                                   |

### 返回示例

> 成功

```json
{
    "meetingInfo": {
        "meetingAddress": "酒仙桥",
        "date": "2024-09-28",
        "startTime": "17:30",
        "duration": "60",
        "attendance": "5"
    },
    "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
| ------ | ---------- | ---- |
| 200    | OK         | 成功 |

### 返回数据结构

状态码 **200**

| 名称              | 类型    | 必选 | 说明                     |
| ----------------- | ------- | ---- | ------------------------ |
| » status          | boolean | 是   | 响应状态                 |
| » meetingInfo     | object  | 否   | 从用户输入提取的会议信息 |
| »» meetingAddress | string  | 否   | 会议地点                 |
| »» date           | string  | 否   | 会议开始日期             |
| »» startTime      | string  | 否   | 会议开始时间             |
| »» duration       | string  | 否   | 会议时长                 |
| »» attendance     | string  | 否   | 会议人数                 |

## 获取文件列表

GET /uploadFile/getUploadFileList

### 请求参数

| 名称       | 位置  | 类型   | 必选 | 说明       |
| ---------- | ----- | ------ | ---- | ---------- |
| category   | query | string | 否   | 数据类别   |
| pageSize   | query | string | 否   | 分页的大小 |
| pageNumber | query | string | 否   | 分页的序号 |

> 返回示例

```json
{
  "totalRow": 698,
  "pageNumber": 1,
  "data": [
    {
      "fileId": "2fb4748945684162b071590d2765650b",
      "filename": "14.中电信京人力[2015]2号 关于印发《中国电信股份有限公司北京分公司员工退休管理办法》的通知.pdf",
      "filepath": "202410132337313037.pdf",
      "category": "bj-telecom"
    },
    {
      "fileId": "272582a7e0e04b419448b6e648c44c13",
      "filename": "2.员工因私出国（境）管理办法.doc",
      "filepath": "202410132337336925.doc",
      "category": "bj-telecom"
    }
  ],
  "totalPage": 349,
  "pageSize": 2,
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义                                              | 说明 | 数据模型 |
| ------ | ------------------------------------------------------- | ---- | -------- |
| 200    | [OK](https://tools.ietf.org/html/rfc7231#section-6.3.1) | none | Inline   |

### 返回数据结构

状态码 **200**

| 名称         | 类型     | 必选 | 约束 | 中文名 | 说明         |
| ------------ | -------- | ---- | ---- | ------ | ------------ |
| » totalRow   | integer  | true | none |        | 数据总数     |
| » pageNumber | integer  | true | none |        | 分页的序号   |
| » data       | [object] | true | none |        | none         |
| »» fileId    | string   | true | none |        | 文件ID       |
| »» filename  | string   | true | none |        | 文件名       |
| »» filepath  | string   | true | none |        | 文件相对路径 |
| »» category  | string   | true | none |        | 数据类别     |
| » totalPage  | integer  | true | none |        | 分页总数     |
| » pageSize   | integer  | true | none |        | 分页的大小   |
| » status     | string   | true | none |        | 接口返回状态 |

## 删除上传文件

POST /uploadFile/deleteFile

> Body 请求参数

```json
[
  "b957969f382147b4b05592c1bfc719f2",
  "07c7bbed732f47068ba8170d6858a4da",
  "fd8c04b93f21408fafa57ffb9d26faa4",
  "5f2dc303b5c74af2a5affeb513a93392"
]
```

### 请求参数

| 名称 | 位置 | 类型   | 必选 | 说明 |
| ---- | ---- | ------ | ---- | ---- |
| body | body | object | 否   | none |

> 返回示例

```json
{
  "status": "success"
}
```

### 返回结果

| 状态码 | 状态码含义                                              | 说明 | 数据模型 |
| ------ | ------------------------------------------------------- | ---- | -------- |
| 200    | [OK](https://tools.ietf.org/html/rfc7231#section-6.3.1) | none | Inline   |

### 返回数据结构

状态码 **200**

| 名称     | 类型   | 必选 | 约束 | 中文名   | 说明 |
| -------- | ------ | ---- | ---- | -------- | ---- |
| » status | string | true | none | 返回状态 | none |


## 上传私训学习文件

POST `/training/upload`

上传私训学习文件，支持txt、word、pdf格式

### Body 请求参数

```yaml
fileToUpload: file://D:/知识图谱.pdf
```

### 请求参数

| 名称              | 位置    | 类型             | 必选 | 说明       |
|-----------------|-------|----------------|----|----------|
| category        | query | string         | 是  | 指定的数据类别  |
| fileId        | query | string         | 是  | 指定的文件ID  |
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

## 根据上下文查询页码

POST `/pdf/cropRect`

输入上下文，定位页码。

### Body 请求参数

```json
{
  "category": "bj-telecom",
  "chunkData": [
    {
      "chunkId": "3d8f74e76c794c8ca60ca64155cab8d9",
      "result": "— 1 —中电信京〔2024〕165 号关于印发中国电信北京公司员工积分管理办法（2024 版）的通知公司各部门、各区分公司、信元公司、正通公司：为进一步完善员工积分体系，打造多样化的积分渠道，强化职业发展价值贡献导向，激发员工的活力和内生动力，促进公司战略落地和优秀人才快速成长，根据集团公司员工职业发展建设的有关要求，公司对《中国电信北京公司员工积分管理办法（试行）》（中电信京〔2017〕190 号）进行了修订。《中国电信北京公司员工积分管理办法（2024 版）》作为员工职业发展管理办法的重要配套文件，现予以印发，请遵照执行。"
    },
     {
      "chunkId": "48aaee358f1f410589cd54358722aec3",
      "result": "— 1 —中电信京〔2024〕165 号关于印发中国电信北京公司员工积分管理办法（2024 版）的通知公司各部门、各区分公司、信元公司、正通公司：为进一步完善员工积分体系，打造多样化的积分渠道，强化职业发展价值贡献导向，激发员工的活力和内生动力，促进公司战略落地和优秀人才快速成长，根据集团公司员工职业发展建设的有关要求，公司对《中国电信北京公司员工积分管理办法（试行）》（中电信京〔2017〕190 号）进行了修订。《中国电信北京公司员工积分管理办法（2024 版）》作为员工职业发展管理办法的重要配套文件，现予以印发，请遵照执行。"
    }
  ]
}
```

### 请求参数

| 名称             | 位置 | 类型     | 必选 | 说明        |
|----------------| ---- | -------- | ---- |-----------|
| body           | body | object   | 否   | none      | 
| » category     | body | string   | 否   | 数据类别      |
| » chunkData    | body | [object] | 是   | 提交的消息列表   |
| »» chunkId     | body | string   | 否   | 上下文对应索引id |
| »» result      | body | string   | 否   | 上下文内容     |

### 返回示例

> 成功

```json
{
    "message": "success",
    "data": [
        {
            "filename": "（中电信京〔2024〕165号）《关于印发中国电信北京公司员工积分管理办法（2024版）的通知》.pdf",
            "filePath": "202411261109007472.pdf",
            "rects": [
                {
                    "page": 1,
                    "rect": [
                        0,
                        -75,
                        1200,
                        1160
                    ]
                }
            ]
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

| 名称                | 类型          | 必选    | 说明      |
|-------------------|-------------|-------|---------|
| » message         | string      | true  | 状态码     |
| » data            | [object]    | true  | 对象类型    |
| »» filename       | integer     | true  | 文件名称    | 
| »» filePath       | [object]    | true  | 文件相对路径  |
| »»» rects         | [object]     | false | 对象的索引列表 |
| »»»» page         | integer      | false | 页码      |
| »»»» rect         | [object]     | true  | 定位信息    |

## 根据页码朔源图片

POST `/pdf/cropByRect`

输入上下文，定位页码。

### Body 请求参数

```json
{
    "category": "bj-telecom",
    "chunkData": [
        {
           "filePath": "202411251507080663.pdf",
           "rects": [
                {
                    "page": 3,
                    "rect": [
                        0,
                        -275,
                        1200,
                        1174
                    ]
                },
                {
                    "page": 4,
                    "rect": [
                        0,
                        -275,
                        1200,
                        1172
                    ]
                },
                {
                    "page": 5,
                    "rect": [
                        0,
                        -275,
                        1200,
                        1174
                    ]
                }
            ]
        }
    ]
}
```

### 请求参数

| 名称          | 位置 | 类型        | 必选 | 说明       |
|-------------| ---- |-----------| ---- |----------|
| body        | body | object    | 否   | none     | 
| » category  | body | string    | 否   | 数据类别     |
| » chunkData | body | [object]  | 是   | 提交的消息列表  |
| »» filePath | body | string    | 否   | 上下文对应索引id |
| »» rects    | body | [object]  | 否   | 上下文内容    |
| »»» page    | body | integer   | 否   | 上下文对应索引id |
| »»» rect    | body | [object]  | 否   | 定位信息     |

### 返回示例

> 成功

```json
{
    "message": "success",
    "data": [
        "static/crop/3_0_0_595_842.png",
        "static/crop/4_0_0_595_842.png",
        "static/crop/5_0_0_595_842.png"
    ]
}
```

### 返回结果

| 状态码 | 状态码含义 | 说明 |
|-----|-------|----|
| 200 | OK    | 成功 |

### 返回数据结构

状态码 **200**

| 名称                | 类型        | 必选    | 说明      |
|-------------------|-----------|-------|---------|
| » message         | string    | true  | 状态码     |
| » data            | [object]  | true  | 相对路径列表  |

