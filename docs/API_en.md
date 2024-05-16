## Completions Interface

POST /v1/chat/completions

Enter a prompt to get an answer from the large model.

> Body request parameters

```json
{
  "model": "gpt-3.5-turbo-1106",
  "temperature": 0.8,
  "max_tokens": 500,
  "category": "xxxx",
  "messages": [
    {
      "role": "user",
      "content": "Check the number of units that have moved to the cloud in the big data center."
    }
  ]
}
```

### Request Parameters

| Name          | Position | Type     | Required | Description                                                  |
| ------------- | -------- | -------- | -------- | ------------------------------------------------------------ |
| body          | body     | object   | No       | none                                                         |
| » model       | body     | string   | No       | Model type                                                   |
| » temperature | body     | number   | Yes      | The sampling temperature to use                              |
| » max_tokens  | body     | integer  | Yes      | The maximum number of tokens that can be generated.          |
| » category    | body     | string   | No       | Data category                                                |
| » messages    | body     | [object] | Yes      | List of submitted messages                                   |
| »» role       | body     | string   | No       | user or assistant, user indicates user submission, assistant indicates model output |
| »» content    | body     | string   | No       | If the role is user, then context is the content entered by the user. If the role is assistant, then context is the output content of the large model. |

> Return example

> Success

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
        "content": "Knowledge graphs are a structured way to represent and organize human knowledge. They present knowledge points (i.e., facts, concepts, relationships, etc.) and their interconnections in a graphical manner, enabling better understanding and use of this knowledge by humans or machines. Knowledge graphs can help people better explore and understand knowledge in a specific domain and quickly find the information they need when necessary. In the context of this document, knowledge graphs are used to consolidate and store expert knowledge to support business decisions and transform into profit centers."
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

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name                 | Type     | Required | Description                                                  |
| -------------------- | -------- | -------- | ------------------------------------------------------------ |
| » id                 | string   | true     | Unique identifier                                            |
| » object             | string   | true     | Object type                                                  |
| » created            | integer  | true     | Unix timestamp (seconds) when the chat was created           |
| » choices            | [object] | true     | List of choices                                              |
| »» index             | integer  | false    | Index of the object                                          |
| »» message           | object   | false    | Returned message                                             |
| »»» role             | string   | true     | user or assistant, user indicates user submission, assistant indicates model output |
| »»» content          | string   | true     | If the role is user, then context is the content entered by the user. If the role is assistant, then context is the output content of the large model. |
| »» finish_reason     | string   | false    | Reason for model stop generating                             |
| » usage              | object   | true     | Usage statistics for the request                             |
| »» prompt_tokens     | integer  | true     | Number of tokens in the prompt.                              |
| »» completion_tokens | integer  | true     | Number of generated tokens                                   |
| »» total_tokens      | integer  | true     | Total number of tokens used in the request                   |


## Speech Recognition

POST /v1/asr

The speech recognition interface returns the text after recognition.

> Body request parameters

The request body passes binary audio data, so the `Content-Type` in the HTTPS request header must be set to `application/octet-stream`.

### Request Parameters

| Name         | Position | Type           | Required | Description |
| ------------ | -------- | -------------- | -------- | ----------- |
| Content-Type | header   | string         | No       | none        |
| body         | body     | string(binary) | No       | none        |

> Return example

> Success

```json
{
  "result": "What are you going to do if this situation occurs",
  "status": 20000000
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name     | Type    | Required | Description                |
| -------- | ------- | -------- | -------------------------- |
| » result | string  | true     | Speech recognition result. |
| » status | integer | true     | Service status code.       |


## Text-to-Speech

GET /v1/tts

Enter text to return a spoken audio file.

### Request Parameters

| Name | Position | Type   | Required | Description            |
| ---- | -------- | ------ | -------- | ---------------------- |
| text | query    | string | Yes      | Text to be synthesized |

### Response Result

- Successful response
  - The content of the `Content-Type` field in HTTPS Headers is `audio/mpeg`, indicating successful synthesis, with the synthesized audio data in the response body.
  - The response content is binary data of the synthesized audio.
- Failure response
  - No `Content-Type` field in HTTP Headers, or the content of the `Content-Type` field is `application/json`, indicating failure of synthesis, with error information in the response body.

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |


## Image Generation

POST /v1/images/generations

Enter a command to generate images and return images.

> Body request parameters

```json
{
  "prompt": "a pig"
}
```

### Request Parameters

| Name     | Position | Type   | Required | Description                |
| -------- | -------- | ------ | -------- | -------------------------- |
| body     | body     | object | No       | none                       |
| » prompt | body     | string | Yes      | Command to generate images |

> Return example

> Success

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

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name      | Type     | Required | Description                                        |
| --------- | -------- | -------- | -------------------------------------------------- |
| » created | integer  | true     | Unix timestamp (seconds) when the chat was created |
| » data    | [object] | true     | Generated image data                               |
| »» url    | string   | false    | Generated image address                            |


## Upload Private Training Files

POST /uploadFile/uploadLearningFile

Upload private training files, supporting txt, word, pdf formats.

> Body request parameters

```yaml
fileToUpload: file://D:/KnowledgeGraph.pdf

```

### Request Parameters

| Name           | Position | Type           | Required | Description                              |
| -------------- | -------- | -------------- | -------- | ---------------------------------------- |
| category       | query    | string         | Yes      | Specified data category                  |
| body           | body     | object         | No       | none                                     |
| » fileToUpload | body     | string(binary) | Yes      | The private training file being uploaded |

> Return example

> Success

```json
{
  "result": true
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name     | Type    | Required | Description                |
|----------|---------|----------|----------------------------|
| » result | boolean | true     | Status of uploading private training file |


## Image Captioning

POST /image/imageToText

Upload an image and return a description of the image.

> Body request parameters

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg

```

### Request Parameters

| Name   | Position | Type           | Required | Description                   |
| ------ | -------- | -------------- | -------- | ----------------------------- |
| body   | body     | object         | No       | none                          |
| » file | body     | string(binary) | Yes      | The image file being uploaded |

> Return example

> Success

```json
{
  "classification": "Forklift Fuel Pump ",
  "caption": "A bunch of bicycles parked on the street side",
  "samUrl": "http://116.255.226.214:9000/sam/qlztvdkv240301172715.png?response-content-type=image%2F%2A&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=9OMV1OGIpDH29iDq1HWC%2F20240301%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20240301T092715Z&X-Amz-Expires=7200&X-Amz-SignedHeaders=host&X-Amz-Signature=0199b49a10eb6a18f01c4cec8e10549642ee6953c0f00c308bfc644e55d86c66",
  "status": "success"
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name             | Type   | Required | Description                          |
| ---------------- | ------ | -------- | ------------------------------------ |
| » status         | string | true     | Status of the image captioning call  |
| » classification | string | true     | Identified categories of the image   |
| » caption        | string | true     | Recognized description of the image  |
| » samUrl         | string | true     | Uploaded image's segmentation result |


## Video Tracking

POST /video/motInference

Upload a video for video tracking.

> Body request parameters

```yaml
file: file://D:\Test\Datasets\Video\demo.mp4

```

### Request Parameters

| Name   | Position | Type           | Required | Description                   |
| ------ | -------- | -------------- | -------- | ----------------------------- |
| body   | body     | object         | No       | none                          |
| » file | body     | string(binary) | Yes      | The video file being uploaded |

> Return example

> Success

```json
{
  "data": "static/video/d01476eb-80de-4a7d-a5bb-566644c96a81.mp4",
  "status": "success"
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name     | Type   | Required | Description                  |
| -------- | ------ | -------- | ---------------------------- |
| » status | string | true     | Status of the result         |
| » data   | string | true     | Address of the tracked video |


## Image Enhancement

POST /image/enhanceImage

Upload an image to enhance a blurry image.

> Body request parameters

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg

```

### Request Parameters

| Name   | Position | Type           | Required | Description                   |
| ------ | -------- | -------------- | -------- | ----------------------------- |
| body   | body     | object         | No       | none                          |
| » file | body     | string(binary) | Yes      | The image file being uploaded |

> Return example

> Success

```json
{
  "status": "success",
  "enhanceImageUrl": "http://116.255.226.214:9000/realesrgan/tfukxzrq240301172914.png?response-content-type=image%2F%2A&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=9OMV1OGIpDH29iDq1HWC%2F20240301%2Fus-east-1%2Fs3%2Faws4_request

&X-Amz-Date=20240301T092921Z&X-Amz-Expires=7200&X-Amz-SignedHeaders=host&X-Amz-Signature=931b1c9d1b850095d58763db042bd982108dc0c2415985df37d6efa824f33fff"
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name              | Type   | Required | Description                   |
| ----------------- | ------ | -------- | ----------------------------- |
| » enhanceImageUrl | string | true     | Address of the enhanced image |
| » status          | string | true     | Status of the result          |


## Video Generation

POST /image/generateVideo

Upload an image and generate a short video based on that image.

> Body request parameters

```yaml
file: file://D:\Test\Datasets\Image\kppziguz230716233346.jpg
```

### Request Parameters

| Name   | Position | Type           | Required | Description                   |
| ------ | -------- | -------------- | -------- | ----------------------------- |
| body   | body     | object         | No       | none                          |
| » file | body     | string(binary) | Yes      | The image file being uploaded |

> Return example

> Success

```json
{
  "svdVideoUrl": "static/img/svd/7ef10bdd-1c72-4091-a7aa-db651966f076.mp4",
  "status": "success"
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name          | Type   | Required | Description                    |
| ------------- | ------ | -------- | ------------------------------ |
| » svdVideoUrl | string | true     | Address of the generated video |
| » status      | string | true     | Status of the result           |


## Video Enhancement

POST /video/mmeditingInference

Upload a video for video frame interpolation.

> Body request parameters

```yaml
file: file://D:\Test\Datasets\Video\demo.mp4

```

### Request Parameters

| Name   | Position | Type           | Required | Description                   |
| ------ | -------- | -------------- | -------- | ----------------------------- |
| body   | body     | object         | No       | none                          |
| » file | body     | string(binary) | Yes      | The video file being uploaded |

> Return example

> Success

```json
{
  "data": "static/video/d01476eb-80de-4a7d-a5bb-566644c96a81.mp4",
  "status": "success"
}
```

### Return Result

| Status Code | Meaning | Description |
| ----------- | ------- | ----------- |
| 200         | OK      | Success     |

### Return Data Structure

Status Code **200**

| Name     | Type   | Required | Description                   |
| -------- | ------ | -------- | ----------------------------- |
| » status | string | true     | Status of the result          |
| » data   | string | true     | Address of the enhanced video |
