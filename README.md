[简体中文](README_zh.md)| English

### **Introduction**

Lag[i] is a solution designed by Beijing Landing Technologies Co.,Ltd. specifically for the significant gap between the powerful forces of the large model open-source community and enterprise application deployment. It is an enterprise-level composite multimodal large model middleware aimed at bridging this gap, by providing a secure and professional platform, enabling enterprises to customize and deploy large models in a low-cost, efficient manner.

### Quick Start

For developers, we provide a simple way to compile and run the Lag[i] application. You can choose to use the Maven command-line tool to package, or run it through mainstream Integrated Development Environments (IDEs) like IntelliJ IDEA. Please ensure your JDK version meets at least the requirements of version 8.

#### Method 1: Using Maven Command

First, you need to clone the Lag[i] project repository and switch to the project directory:

```shell
git clone https://github.com/landingbj/lagi.git
cd lagi
```

Next, modify the [`src/main/resources/lagi.yml`](lagi-web/src/main/resources/lagi.yml) configuration file, replace the large language model API_KEY or SECRET_KEY with your own keys, and set the `enable` field of the model you wish to activate to `true` as needed. Detailed configuration methods can be seen in the [configuration documentation](docs/config_en.md).

```yaml
- name: gpt-test
  type: GPT
  enable: true
  priority: 1
  model: gpt-3.5-turbo-1106
  api_key: your-apikey
```

Then, use the Maven command to package the project, and the packaged war file will be generated in the `target` directory:

```shell
mvn package
```

Finally, deploy the generated war package to the Tomcat server. After starting Tomcat, you can view the specific page of Lag[i] by accessing the corresponding port through a browser.

#### Method 2: Using IDE

If you prefer to use an IDE for development, you can directly open the Lag[i] project with mainstream IDEs like IntelliJ IDEA. After publishing the project as a web project to the Tomcat server, you can view the interface of Lag[i] by accessing the respective port through a browser.

### Tutorial

To help you efficiently become familiar with and utilize Lag[i], we have prepared a comprehensive set of  [Tutorial presentations](docs/tutor_en.md). Through this tutorial, whether you are a beginner or an experienced developer, you can quickly get started. The tutorial also covers the setup of the Lag[i] basic environmental vector database, guiding you step by step from the download, installation, and configuration of Lag[i], to actual operation, allowing you to easily start learning and quickly master the operational skills of Lag[i].

### API

The Lag[i] project also provides multiple RESTful API interfaces, facilitating users to integrate AI services into their own applications. We have prepared detailed [API documentation](docs/API_en.md) for developers, which you can refer to understand how to use these APIs to enrich your application features.

### Integration

In order to help you better understand and use Lag[i], we provide you with detailed [Guide documentation](docs/guide_en.md). You can refer to this document to understand the structure and content of the project, so that you can quickly invoke relevant features and develop large model applications efficiently. It helps you understand and use a variety of AI features provided in the project. With this guide, you can easily integrate AI features such as text conversation, speech recognition, text-to-speech, and image generation into your applications, achieving a more intelligent and user-friendly interaction experience.

If you wish to integrate Lag[i] into your project, you can refer to our [Integration documentation](https://github.com/landingbj/lagi/blob/main/docs/guide_en.md#quick-integrate-into-your-existing-project). This document will guide you through the process of seamlessly integrating Lag[i] into your project. In addition, we provide frequently asked questions and best practices to help you avoid common pitfalls during integration, ensuring a smooth progress of your project. It covers comprehensive guidance from setting up the environment to implementing features. Whether you are a beginner or an experienced developer, you will be able to quickly integrate Lag[i] into your project.

### Extension

If you are not satisfied with the adapted large model for Lag[i], you can also refer to our [Extension documentation](docs/extend_en.md) to extend Lag[i], adapting it to your preferred large model. This document not only covers the methods for adapting and extending functional models and vector databases but also provides expansion examples to help you quickly grasp the methods for extending Lag[i], meeting your specific requirements.

### Security

To better integrate Lag[i] into your business,You can do this by adding the keywords you want to filter in the `sensitive_word.json` file,In the `priority_word.json` file, you specify which keywords to answer first.And set the stop keyword in the `stopping_word.json file`,Thus, the returned results of the dialogue are changed, the dialogue is guided in a specific direction, and the dialogue is automatically stopped when needed.

Example: Set the sensitive word filter, level has three values, 1: delete the entire sentence when the sensitive word is matched 2: replace with mask 3: erase (default). mask: mask string (default :...) . rules: represents a list of sensitive rules, where rule for each list element represents the regular expression matching the sensitive word, mask and level are used globally if not specified:   


```json
{
  "mask": "...",
  "level": 3,
  "rules": [
    {"rule":"OPENAI"},
    {"rule":"hello", "level": 2, "mask": "***"}
  ]
}

```
Example：Set priority keywords and stop keywords:
```json
[
  "openai"
]
```


### Online Demo

To allow users to intuitively experience the powerful functionality and convenience of Lag[i], we offer an online demo. You can visit and experience it through the following link: [https://lagi.landingbj.com](https://lagi.landingbj.com/).