# 配置参考指南

系统首页展示名称，这个设置指定了将在系统首页显示的名称，这里是“ LinkMind”。

```yaml
system_title: LinkMind
```

模型配置

```yaml
# 这部分定义了中间件使用的模型配置。
models:
  # 单驱动模型情况下模型的配置。
  - name: chatgpt  # 后端服务的名称。
    type: OpenAI  # 所属公司，例如这里是OpenAI
    enable: false # 这个标志决定了后端服务是否启用。“true”表示已启用。
    model: gpt-3.5-turbo,gpt-4-turbo # 驱动支持的模型列表
    driver: ai.llm.adapter.impl.GPTAdapter # 模型驱动
    api_key: your-api-key # API密钥
  # 模型支持多驱动的配置
  - name: landing
    type: Landing
    enable: false
    drivers: # 多驱动配置.
      - model: turing,qa,tree,proxy # 驱动支持功能列表
        driver: ai.llm.adapter.impl.LandingAdapter # 驱动地址
      - model: image # 驱动支持功能列表
        driver: ai.image.adapter.impl.LandingImageAdapter # 驱动地址
        oss: landing # 用到的存储对象服务的名称
      - model: landing-tts,landing-asr
        driver: ai.audio.adapter.impl.LandingAudioAdapter 
      - model: video
        driver: ai.video.adapter.impl.LandingVideoAdapter 
        api_key: your-api-key # 驱动指定api_key
    api_key:  your-api-key # 驱动公用的api_key

```

存储功能配置

```yaml
# 这部分定义了中间件用到的存储设备配置。
stores:
  # 这部分是向量数据库的配置
  vectors: # 向量数据库配置列表
    - name: chroma # 向量数据库名称
      driver: ai.vector.impl.ChromaVectorStore # 向量数据库驱动
      default_category: default # 向量数据库存储的分类
      similarity_top_k: 10 # 向量数据库查询时使用的参数
      similarity_cutoff: 0.5
      parent_depth: 1
      child_depth: 1
      url: http://localhost:8000 # 向量数据库的存储配置
  # 这部分是对象存储服务的配置
  oss:
    - name: landing # 存储对象服务的名称
      driver: ai.oss.impl.LandingOSS  # 存储对象服务的驱动类
      bucket_name: lagi # 存储对象的 bucket_name
      enable: true # 是否开启该存储对象服务

    - name: alibaba
      driver: ai.oss.impl.AlibabaOSS
      access_key_id: your-access-key-id # 第三方存储对象的服务用到的 access key id
      access_key_secret: your-access-key-secret # 第三方存储对象的服务用到的 access key secret 
      bucket_name: ai-service-oss
      enable: true

  # 这部分是elasticsearch的配置
  text:
    - name: elasticsearch # 全文检索名称
      driver: ai.bigdata.impl.ElasticSearchAdapter
      host: localhost # 全文检索的elasticsearch地址
      port: 9200 # 全文检索的elasticsearch的端口号
      enable: false # 是否开启
  database: # 关系型数据库配置
    name: mysql # 数据库名称
    jdbcUrl: you-jdbc-url # 连接地址
    driverClassName: com.mysql.cj.jdbc.Driver # 驱动类
    username: your-username # 数据库用户名
    password: your-password # 数据库密码
  # 这部分是检索增强生成服务的配置
  rag:
      vector: chroma # 服务用到的向量数据库的名称
      fulltext: elasticsearch # 全文检索（可选填，如填写该配置，则开启该配置，不开启，直接注释即可）
      graph: landing # 图检索（可选填，如填写该配置，则开启该配置，不开启，直接注释即可）
      enable: true # 是否开启
      priority: 10 # 优先级，当该优先级大于模型时,则匹配不到上下文就只返回default中提示语
      default: "Please give prompt more precisely" # 如未匹配到上下文，则返回该提示语
      track: true # 开启文档跟踪
  # 这部分是美杜莎的加速推理服务的配置，可以通过预训练的medusa.json来预准备缓存，第一次flush置成true来初始化，后续可以改回false用做日常启停。
  medusa:
    enable: true # 是否开启
    algorithm: hash,llm,tree # 使用的算法
    reason_model: deepseek-r1 # 推理模型
    aheads: 1 # 预推理的请求数
    producer_thread_num: 1 # 生产者线程数
    consumer_thread_num: 2 # 消费者线程数
    cache_persistent_path: medusa_cache # 缓存持久化路径
    cache_persistent_batch_size: 2 # 缓存持久化批次大小
    flush: true # 缓存是否每次启动时都重新加载
```

中间件功能配置

```yaml
# 大模型使用的功能配置
functions:
  # embedding 服务配置
  embedding:
    - backend: qwen
      type: Qwen
      api_key: your-api-key
  
  # 聊天对话、文本生成功能的配置列表
  chat:
    - backend: chatgpt # 后端使用的模型配置的名称
      model: gpt-4-turbo # 模型名
      enable: true # 是否开启
      stream: true # 是否使用流
      priority: 200 # 优先级

    - backend: chatglm
      model: glm-3-turbo
      enable: false
      stream: false
      priority: 10
  
  # 翻译功能的配置列表
  translate:
    - backend: ernie # 后端使用的模型配置的名称
      model: translate # 模型名
      enable: false # 是否开启
      priority: 10 # 优先级
  
  # 语音转文字功能配置列表
  speech2text:
    - backend: qwen  # 后端使用的模型配置的名称
      model: asr
      enable: true
      priority: 10
  
  # 文字转语音功能配置列表
  text2speech:
    - backend: landing # 后端使用的模型配置的名称
      model: tts
      enable: true
      priority: 10
  
  # 声音克隆功能配置列表
  speech2clone:
    - backend: doubao # 后端使用的模型配置的名称
      model: openspeech
      enable: true
      priority: 10
      others: your-speak-id

  # 文字生成图片功能配置列表
  text2image:
    - backend: spark # 后端使用的模型配置的名称
      model: tti
      enable: true
      priority: 10
    - backend: ernie
      model: Stable-Diffusion-XL
      enable: true
      priority: 5
  # 图片生成文字功能配置列表
  image2text:
    - backend: ernie # 后端使用的模型配置的名称
      model: Fuyu-8B
      enable: true
      priority: 10
  # 图片增强功能配置列表
  image2enhance:
    - backend: ernie # 后端使用的模型配置的名称
      model: enhance
      enable: true
      priority: 10
  # 文本生成视频功能配置列表
  text2video:
    - backend: landing # 后端使用的模型配置的名称
      model: video
      enable: true
      priority: 10
  # 图片生成视频功能配置列表
  image2video:
    - backend: qwen # 后端使用的模型配置的名称
      model: vision
      enable: true
      priority: 10
  # 图片OCR配置列表
  image2ocr:
    - backend: qwen
      model: ocr
      enable: true
      priority: 10
  # 视频追踪功能配置列表
  video2track:
    - backend: landing # 后端使用的模型配置的名称
      model: video
      enable: true
      priority: 10
  # 视屏增强功能配置列表
  video2enhance:
    - backend: qwen # 后端使用的模型配置的名称
      model: vision
      enable: true
      priority: 10
  # 文档OCR配置列表
  doc2ocr:
    - backend: qwen
      model: ocr
      enable: true
      priority: 10
  # 文件指令配置列表
  doc2instruct:
    - backend: landing
      model: cascade
      enable: true
      priority: 10
  # sql指令配置表
  text2sql:
    - backend: landing
      model: qwen-turbo # 模型名称
      enable: true # 是否启用
      priority: 10
```

路由政策配置

```yaml
functions:
  policy:
    #  handle配置 目前有parallel、failover、failover 3种值， parallel表示并行调用，failover表示故障转移, polling表示负载轮询调用, 场景解释：
    #  1. 当请求中未强制指定模型, 或指定的模型无效时 ，parallel、failover、failover 3种策略生效
    #  2. 当指定handle为 parallel 配置的模型并行执行， 返回响应最快且优先级最高的模型调用结果
    #  3. 当指定handle为 failover 配置的模型串行执行， 模型按优先级串行执行， 串行执行过程中任意模型返回成功， 后面的模型不再执行。
    #  4. 当指定handle为 failover 配置的模型轮询执行， 请求会根据请求的ip、浏览器指纹 等额外信息， 均衡分配请求给对应的模型执行。
    #  5. 当所有的模型都返回失败时, 会设置 http 请求的状态码为 600-608。 body里为具体的错误信息。 (错误码错误信息实际为最后一个模型调用失败的信息)
    #  错误码： 
    #     600 请求参数不合法
    #     601 授权错误
    #     602 权限被拒绝
    #     603 资源不存在
    #     604 访问频率限制
    #     605 模型内部错误
    #     606 其他错误
    #     607 超时
    #     608 没有可用的模型
    handle: failover
    grace_time: 20 # 故障后重试间隔时间
    maxgen: 3 # 故障后最大重试次数 默认为 Integer.MAX_VALUE
```

智能体配置

```yaml
# 这部分表示模型支持的智能体配置
agents:
  - name: qq # 智能体的名称
    api_key: your-api-key # 智能体用到的 api key
    driver: ai.agent.social.QQAgent # 智能体驱动

  - name: wechat
    api_key: your-api-key
    driver: ai.agent.social.WechatAgent

  - name: ding
    api_key: your-api-key
    driver: ai.agent.social.DingAgent
    
  - name: weather_agent
    driver: ai.agent.customer.WeatherAgent
    token: your-token
    app_id: weather_agent

  - name: oil_price_agent
    driver: ai.agent.customer.OilPriceAgent
    token: your-token
    app_id: oil_price_agent

  - name: bmi_agent
    driver: ai.agent.customer.BmiAgent
    token: your-token
    app_id: bmi_agent

  - name: food_calorie_agent
    driver: ai.agent.customer.FoodCalorieAgent
    token: your-token
    app_id: food_calorie_agent

  - name: dishonest_person_search_agent
    driver: ai.agent.customer.DishonestPersonSearchAgent
    token: your-token
    app_id: dishonest_person_search_agent

  - name: high_speed_ticket_agent
    driver: ai.agent.customer.HighSpeedTicketAgent
    app_id: high_speed_ticket_agent

  - name: history_in_today_agent
    driver: ai.agent.customer.HistoryInToDayAgent
    app_id: history_in_today_agent

  - name: youdao_agent
    driver: ai.agent.customer.YouDaoAgent
    app_id: your-app-id
    token: your-token

  - name: image_gen_agent
    driver: ai.agent.customer.ImageGenAgent
    app_id: your-app-id
    endpoint: http://127.0.0.1:8080
    token: image_gen_agent
    
# 这部分表示智能体实际作业的配置
workers:
  - name: qq-robot # 作业名称
    agent: qq # 工作的智能体名称
    worker: ai.worker.social.RobotWorker # 作业驱动

  - name: wechat-robot
    agent: wechat
    worker: ai.worker.social.RobotWorker

  - name: ding-robot
    agent: ding
    worker: ai.worker.social.RobotWorker

# 路由配置
routers:
  - name: best
    # rule: (weather_agent&food_calorie_agent)  # A|B ->轮询，A或B，表示在A和B之间随机轮询；
    # A,B ->故障转移，首先A，如果A失败，然后B；
    # A&B ->并行，同时调用A和B，选择合适的结果只有一个
    # 该规则可以组合为((A&B&C),(E|F))，这意味着首先同时调用ABC，如果失败，则随机调用E或F
    rule: (weather_agent&food_calorie_agent)  # A|B ->轮询，A或B，表示在A和B内随机轮询；

    # %是表示的通配符。
    # 如果指定，则调用该代理
    # 如果只给出%，则%将由调用时的参数决定。
  - name: pass
    rule: '%'
```



