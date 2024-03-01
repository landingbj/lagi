System homepage display name, this setting specifies the name that will be displayed on the system homepage, which is "Lagi".

```yaml
system_title: Lagi
```

Large Language Model (LLM) Configuration

```yaml
# This section defines the configuration for the Large Language Model (LLM).
LLM:
  # This specifies the backend services to be used for the LLM.
  backends:
    - name: gpt-test # The name of the backend service.
      type: GPT # The type of backend service, here it is GPT.
      enable: false # This flag determines whether the backend service is enabled. "true" means it is enabled.
      priority: 1 # Sets the priority of the backend service.
      model: gpt-3.5-turbo-1106 # Model version
      api_key: your-api-key # API key
   
   - name: vicuna-test1
      type: Vicuna
      enable: true
      priority: 100
      model: /mnt/data/vicuna-13b-v1.5-16k
      # For privately deployed large models, it is necessary to specify the API address of the model service.
      api_address: http://localhost:8090/v1/chat/completions
```

Speech Recognition (ASR) Configuration

```yaml
# This section defines the configuration for Speech Recognition (ASR).
ASR:
  # Lists the backend services to be used for ASR. Each backend service has a name, type, enable flag, and priority.
  backends:
    - name: asr-test1
      type: Landing
      enable: true
      priority: 10
```

Text-to-Speech (TTS) Configuration

```yaml
# This section defines the configuration for Text-to-Speech (TTS).
TTS:
  # Similar to ASR, it lists the backend services with their respective configurations.
  backends:
    - name: tts-test1
      type: Landing
      enable: true
      priority: 10
```

Image Generation Configuration

```yaml
# This section defines the configuration for the image generation service.
image_generation:
  # Lists the backend services to be used for image generation. Each backend service has a name, type, enable flag, and priority.
  backends:
    - name: image-generation-test1
      type: Landing
      enable: true
      priority: 10
```

Image Captioning Configuration

```yaml
# This section defines the configuration for services that generate descriptive text (titles or explanations) for images.
image_captioning:
  # Similar to other sections, it lists the backend services with their respective configurations.
  backends:
    - name: image-captioning-test1
      type: Landing
      enable: true
      priority: 10
```

Image Enhancement Configuration

```yaml
# This section defines the configuration for services used to improve or enhance image quality.
image_enhance:
  # Lists the backend services for image enhancement.
  backends:
    - name: image-enhance-test1
      type: Landing
      enable: true
      priority: 10
```

Video Generation Configuration

```yaml
# This section defines the configuration for services used for video generation.
video_generation:
  # Lists the backend services for video generation.
  backends:
    - name: video-generation-test1
      type: Landing
      enable: true
      priority: 10
```

Video Tracking Configuration

```yaml
# This section defines the configuration for services used to track objects or features in videos.
video_track:
  # Lists the backend services for video tracking.
  backends:
    - name: video-track-test1
      type: Landing
      enable: true
      priority: 10
```

Video Enhancement Configuration

```yaml
# This section defines the configuration for services used to improve or enhance video quality.
video_enhance:
  # Lists the backend services for video enhancement.
  backends:
    - name: video-enhance-test1
      type: Landing
      enable: true
      priority: 10
```