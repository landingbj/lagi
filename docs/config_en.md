# Configuration Reference Guide

System Homepage Display Name: This setting specifies the name displayed on the system homepage, which is "LinkMind".

```yaml
system_title: LinkMind
```

Model Configuration

```yaml
# This section defines the model configuration used by the middleware.
models:
  # Configuration for a single-driver model.
  - name: chatgpt  # Name of the backend service.
    type: OpenAI  # Associated company, e.g., OpenAI here.
    enable: false # This flag determines whether the backend service is enabled. "true" means enabled.
    model: gpt-3.5-turbo,gpt-4-turbo # List of models supported by the driver.
    driver: ai.llm.adapter.impl.GPTAdapter # Model driver.
    api_key: your-api-key # API key.
  # Configuration for multi-driver models.
  - name: landing
    type: Landing
    enable: false
    drivers: # Multi-driver configuration.
      - model: turing,qa,tree,proxy # Driver Model List
        driver: ai.llm.adapter.impl.LandingAdapter # Driver address.
      - model: image # List of features supported by the driver.
        driver: ai.image.adapter.impl.LandingImageAdapter # Driver address.
        oss: landing # Name of the object storage service used.
      - model: landing-tts,landing-asr
        driver: ai.audio.adapter.impl.LandingAudioAdapter 
      - model: video
        driver: ai.video.adapter.impl.LandingVideoAdapter 
        api_key: your-api-key # API key specified for the driver.
    api_key: your-api-key # Shared API key for the drivers.
```

Storage Configuration

```yaml
# This section defines the storage device configuration used by the middleware.
stores:
  # Configuration for the vector database.
  vectors: # List of vector database configurations.
    - name: chroma # Name of the vector database.
      driver: ai.vector.impl.ChromaVectorStore # Vector database driver.
      default_category: default # Category for vector database storage.
      similarity_top_k: 10 # Parameter used for vector database queries.
      similarity_cutoff: 0.5 # Will cut off those results whose similarity to the query vector is less than 0.5.
      parent_depth: 1
      child_depth: 1
      url: http://localhost:8000 # Storage configuration of the vector database.
  # Configuration for object storage services.
  oss:
    - name: landing # Name of the object storage service.
      driver: ai.oss.impl.LandingOSS  # Object storage service driver class.
      bucket_name: lagi # Bucket name for object storage.
      enable: true # Determines if the object storage service is enabled.

    - name: alibaba
      driver: ai.oss.impl.AlibabaOSS
      access_key_id: your-access-key-id # Access key ID for third-party object storage services.
      access_key_secret: your-access-key-secret # Access key secret for third-party object storage services.
      bucket_name: ai-service-oss
      enable: true

  # Configuration for Elasticsearch.
  text:
    - name: elasticsearch # Name of the full-text search.
      driver: ai.bigdata.impl.ElasticSearchAdapter
      host: localhost # Address of Elasticsearch for full-text search.
      port: 9200 # Port of Elasticsearch for full-text search.
      enable: false # Determines if it is enabled.
  database: # Relational database configuration.
    name: mysql # Database name.
    jdbcUrl: you-jdbc-url # Connection address.
    driverClassName: com.mysql.cj.jdbc.Driver # Driver class.
    username: your-username # Database username.
    password: your-password # Database password.
  # Configuration for Retrieval-Augmented Generation (RAG) service.
  rag:
      vector: chroma # Name of the vector database used by the service.
      fulltext: elasticsearch # Full-text search (optional; if configured, this service is enabled. To disable, simply comment out this line).
      graph: landing # Graph search (optional; if configured, this service is enabled. To disable, simply comment out this line).
      enable: true # Determines if it is enabled.
      priority: 10 # Priority; if this priority exceeds the model's, it will return the default prompt if no context is matched.
      default: "Please give prompt more precisely" # Default prompt returned when no context is matched.
      track: true # Enables document tracking.
      
  # This section contains the configuration for Medusa's accelerated inference service. 
  # You can use the pre-trained `medusa.model` to prepopulate the cache. 
  # Set `flush` to true for the initial run to initialize it; afterward, you can change it back to false for routine start/stop operations.
  # Full download link for the `medusa.model` file: https://downloads.landingbj.com/lagi/medusa.model
  medusa:
      enable: true # Whether to enable
      algorithm: hash,llm,tree # Algorithms to use
      reason_model: deepseek-r1 # Inference model
      aheads: 1 # Number of pre-inference requests
      producer_thread_num: 1 # Number of producer threads
      consumer_thread_num: 2 # Number of consumer threads
      cache_persistent_path: medusa_cache # Cache persistence path
      cache_persistent_batch_size: 2 # Cache persistence batch size
      cache_hit_window: 16    # size of the sliding window for cache hits
      cache_hit_ratio: 0.3    # minimum cache hit ratio
      temperature_tolerance: 0.1  # tolerance for the temperature parameter on cache hits
      flush: true # Whether to reload the cache on every startup
```

Middleware Functionality Configuration

```yaml
# Functionality configuration for large models.
functions:
  # Embedding service configuration.
  embedding:
    - backend: qwen
      type: Qwen
      api_key: your-api-key
  
  # Configuration list for chat and text generation functions.
  chat:
    - backend: chatgpt # Name of the backend model configuration.
      model: gpt-4-turbo # Model name.
      enable: true # Determines if it is enabled.
      stream: true # Determines if streaming is used.
      priority: 200 # Priority.

    - backend: chatglm
      model: glm-3-turbo
      enable: false
      stream: false
      priority: 10
  
  # Configuration list for translation functions.
  translate:
    - backend: ernie # Name of the backend model configuration.
      model: translate # Model name.
      enable: false # Determines if it is enabled.
      priority: 10 # Priority.
  
  # Configuration list for speech-to-text functions.
  speech2text:
    - backend: qwen  # Name of the backend model configuration.
      model: asr
      enable: true
      priority: 10
  
  # Configuration list for text-to-speech functions.
  text2speech:
    - backend: landing # Name of the backend model configuration.
      model: tts
      enable: true
      priority: 10
  
  # Configuration list for voice cloning functions.
  speech2clone:
    - backend: doubao # Name of the backend model configuration.
      model: openspeech
      enable: true
      priority: 10
      others: your-speak-id

  # Configuration list for text-to-image functions.
  text2image:
    - backend: spark # Name of the backend model configuration.
      model: tti
      enable: true
      priority: 10
    - backend: ernie
      model: Stable-Diffusion-XL
      enable: true
      priority: 5
  # Configuration list for image-to-text functions.
  image2text:
    - backend: ernie # Name of the backend model configuration.
      model: Fuyu-8B
      enable: true
      priority: 10
  # Configuration list for image enhancement functions.
  image2enhance:
    - backend: ernie # Name of the backend model configuration.
      model: enhance
      enable: true
      priority: 10
  # Configuration list for text-to-video functions.
  text2video:
    - backend: landing # Name of the backend model configuration.
      model: video
      enable: true
      priority: 10
  # Configuration list for image-to-video functions.
  image2video:
    - backend: qwen # Name of the backend model configuration.
      model: vision
      enable: true
      priority: 10
  # Configuration list for image OCR functions.
  image2ocr:
    - backend: qwen
      model: ocr
      enable: true
      priority: 10
  # Configuration list for video tracking functions.
  video2track:
    - backend: landing # Name of the backend model configuration.
      model: video
      enable: true
      priority: 10
  # Configuration list for video enhancement functions.
  video2enhance:
    - backend: qwen # Name of the backend model configuration.
      model: vision
      enable: true
      priority: 10
  # Configuration list for document OCR functions.
  doc2ocr:
    - backend: qwen
      model: ocr
      enable: true
      priority: 10
  # Configuration list for document instruction functions.
  doc2instruct:
    - backend: landing
      model: cascade
      enable: true
      priority: 10
  # Configuration list for SQL instruction functions.
  text2sql:
    - backend: landing
      model: qwen-turbo # Model name.
      enable: true # Determines if it is enabled.
      priority: 10
```

Routing Policy Configuration

```yaml
functions:
  policy:
    # Handle configuration currently supports parallel, failover, and polling:
    #  1. If no model is explicitly specified in the request or the specified model is invalid, the three strategies apply.
    #  2. "parallel" executes configured models concurrently, returning the fastest and highest-priority result.
    #  3. "failover" executes models sequentially by priority, stopping when a successful result is obtained.
    #  4. "polling" distributes requests evenly among models using parameters such as IP and browser fingerprints.
    #  5. If all models fail, the HTTP status code is set to 600–608, with the body containing detailed error information.
    #     Error codes:
    #       600 Invalid request parameters.
    #       601 Authorization error.
    #       602 Permission denied.
    #       603 Resource not found.
    #       604 Rate limit exceeded.
    #       605 Model internal error.
    #       606 Other errors.
    #       607 Timeout.
    #       608 No available model.
    handle: failover #parallel #failover
    grace_time: 20 # Retry interval after failure.
    maxgen: 3 # Maximum retries after failure (default is Integer.MAX_VALUE).
```

Agent Configuration

```yaml
# This section represents agent configurations supported by the models.
agents:
  - name: qq # Agent name.
    api_key: your-api-key # API key used by the agent.
    driver: ai.agent.social.QQAgent # Agent driver.

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
    
# This section represents configurations for agent workers.
workers:
  - name: qq-robot # Worker name.
    agent: qq # Name of the agent it works with.
    worker: ai.worker.social.RobotWorker # Worker driver.

  - name: wechat-robot
    agent: wechat
    worker: ai.worker.social.RobotWorker

  - name: ding-robot
    agent: ding
    worker: ai.worker.social.RobotWorker

# Routing configuration.
routers:
  - name: best
    # Rule: (weather_agent&food_calorie_agent)
    # A|B -> Polling, A or B indicates random polling between A and B.
    # A,B -> Failover, starting with A; if A fails, then B.
    # A&B -> Parallel execution, calling A and B simultaneously and selecting the most appropriate single result.
    # This rule can combine into ((A&B&C),(E|F)), meaning first simultaneously call A, B, and C, and if they fail, randomly call E or F.
    rule: (weather_agent&food_calorie_agent)

    # % represents a wildcard.
    # If specified, the call will use the given agent.
    # If only "%" is given, the % will be determined by the parameters passed during the call.
  - name: pass
    rule: '%'

```

MCP configuration

```yaml
mcps:
  servers:
    - name: baidu_search_mcp  # MCP service name
      url: http://appbuilder.baidu.com/v2/ai_search/mcp/sse?api_key=Bearer+your_api_key  # MCP service URL
```
