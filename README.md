[简体中文](README_zh.md)| English

### **Introduction**

Lag[i] is a solution designed by Beijing Landing Technologies Co.,Ltd. specifically for the significant gap between the powerful forces of the large model open-source community and enterprise application deployment. It is an enterprise-level composite multimodal large model middleware aimed at bridging this gap, by providing a secure and professional platform, enabling enterprises to customize and deploy large models in a low-cost, efficient manner.

### Online Demo

To allow users to intuitively experience the powerful functionality and convenience of Lag[i], we offer an online demo. You can visit and experience it through the following link: [https://lagi.landingbj.com](https://lagi.landingbj.com/).

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

### Use Tutorial

To help you quickly understand and utilize Lag[i], we have meticulously prepared an exhaustive [Use tutorial](docs/tutor_en.md). This guide will lead you from scratch through the process of downloading, installing, configuring, and running Lag[i], enabling you to master its usage methods swiftly.

### API

The Lag[i] project also provides multiple RESTful API interfaces, facilitating users to integrate AI services into their own applications. We have prepared detailed [API documentation](docs/API_en.md) for developers, which you can refer to understand how to use these APIs to enrich your application features.

### Integration

To help you better understand and use Lag[i], we have provided you with an exhaustive [Guide documentation](docs/guide_en.md). You can refer to the documentation to understand the structure and content of the project so that you can quickly invoke the relevant functionality and develop large model applications efficiently.  

If you wish to integrate Lag[i] into your project.You can refer to our [Integration documentation](https://github.com/landingbj/lagi/blob/main/docs/guide_en.md#quick-integrate-into-your-existing-project)，To help you quickly integrate Lag[i] into your project.

### Security

To better integrate Lag[i] into your business,You can do this by adding the keywords you want to filter in the `sensitive_word.json` file,In the `priority_word.json` file, you specify which keywords to answer first.And set the stop keyword in the `stopping_word.json file`,Thus, the returned results of the dialogue are changed, the dialogue is guided in a specific direction, and the dialogue is automatically stopped when needed.

Example: Set the keywords to filter `openai`:

```json
[
  "openai"
]
```

### Extension

If you are not satisfied with the large model that Lag[i] has adapted, you can also refer to our [Extension documentation](docs/extend_en.md) to extend Lag[i] to fit your like large model.