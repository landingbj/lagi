系统首页展示名称，这个设置指定了将在系统首页显示的名称，这里是“Lagi”。

```yaml
system_title: Lagi
```

大语言模型（LLM）配置

```yaml
# 这部分定义了大语言模型（LLM）的配置。
LLM:
  # 这指定了将用于LLM的后端服务。
  backends:
    - name: gpt-test # 后端服务的名称。
      type: GPT # 后端服务的类型，这里是GPT。
      enable: false # 这个标志决定了后端服务是否启用。“true”表示已启用。
      priority: 1 # 设置了后端服务的优先级。
      model: gpt-3.5-turbo-1106 # 模型版本
      api_key: your-api-key # API密钥
   
   - name: vicuna-test1
      type: Vicuna
      enable: true
      priority: 100
      model: /mnt/data/vicuna-13b-v1.5-16k
      # 私有化部署的大模型，需要指定模型服务的API地址。
      api_address: http://localhost:8090/v1/chat/completions
```

语音识别（ASR）配置

```yaml
# 这部分定义了语音识别（ASR）的配置。
ASR:
  # 列出了将用于ASR的后端服务。每个后端服务都有一个名称、类型、启用标志和优先级。
  backends:
    - name: asr-test1
      type: Landing
      enable: true
      priority: 10
```

文本转语音（TTS）配置

```yaml
# 这部分定义了文本转语音（TTS）的配置。
TTS:
  # 与ASR类似，它列出了具有各自配置的后端服务。
  backends:
    - name: tts-test1
      type: Landing
      enable: true
      priority: 10
```

图像生成配置

```yaml
# 这部分定义了图像生成服务的配置。
image_generation:
  # 列出了将用于图像生成的后端服务。每个后端服务都有一个名称、类型、启用标志和优先级。
  backends:
    - name: image-generation-test1
      type: Landing
      enable: true
      priority: 10
```

图像描述配置 

```yaml
# 这部分定义了为图像生成描述文本（标题或说明）的服务的配置。
image_captioning:
  # 与其他部分类似，它列出了具有各自配置的后端服务。
  backends:
    - name: image-captioning-test1
      type: Landing
      enable: true
      priority: 10
```

图像增强配置

```yaml
# 这部分定义了用于提高或改善图像质量的服务的配置。
image_enhance:
  # 列出了用于图像增强的后端服务。
  backends:
    - name: image-enhance-test1
      type: Landing
      enable: true
      priority: 10
```

视频生成配置 

```yaml
# 这部分定义了用于生成视频的服务的配置。
video_generation:
  # 列出了用于视频生成的后端服务。
  backends:
    - name: video-generation-test1
      type: Landing
      enable: true
      priority: 10
```

视频跟踪配置

```yaml
# 这部分定义了用于在视频中跟踪对象或特征的服务的配置。
video_track:
  # 列出了用于视频跟踪的后端服务。
  backends:
    - name: video-track-test1
      type: Landing
      enable: true
      priority: 10
```

视频增强配置

```yaml
# 这部分定义了用于提高或改善视频质量的服务的配置。
video_enhance:
  # 列出了用于视频增强的后端服务。
  backends:
    - name: video-enhance-test1
      type: Landing
      enable: true
      priority: 10
```



