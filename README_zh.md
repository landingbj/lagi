简体中文| [English](README.md)

## **项目简介**

Lag[i] (联基) 是由北京联动北方科技有限公司精心打造的一款针对于大模型开源社区的强大力量和企业应用部署之间存在的显著差距而设计的解决方案。它是一款企业级的复合多模态大模型中间件，旨在桥接这一鸿沟，通过提供一个既安全又专业的平台，使得企业能够以低成本、高效率地方式定制并部署大模型。

支持但不限于以下大模型：

<div style="display: flex; flex-wrap: wrap; justify-content: flex-start; align-items: center;">
    <div style="display: flex; align-items: center; margin: 10px;">
        <img src="docs/images/logo/model/img_1.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Landing&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_2.jpeg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Vicuna&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_4.jpeg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">ChatGPT&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_3.jpeg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">GPTAzure&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_12.webp" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Gemini&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_5.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Qwen&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
    </div>
	</br>
 <div style="display: flex; align-items: center; margin: 10px;">
        <img src="docs/images/logo/model/img_6.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Baidu&nbsp;Ernie &nbsp;&nbsp; </span>
        <img src="docs/images/logo/model/img_7.jpg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">ChatGLM&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_8.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Moonshot&nbsp;AI&nbsp;</span>
        <img src="docs/images/logo/model/img_9.jpeg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align:   left;">Baichuan&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_10.jpeg"width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">IFLYTEK&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_11.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">SenseChat&nbsp;&nbsp;&nbsp;</span>
    </div>
		</br>
    <div style="display: flex; align-items: center; margin: 10px;">
        <img src="docs/images/logo/model/img_13.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Doubao&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp</span>
        <img src="docs/images/logo/model/img_14.jpeg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Deepseek&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/model/img_15.webp" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;width: 70px; text-align: left;">Claude&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
    </div>
	</br>
</div>

支持但不限于以下智能体平台：

<div style="display: flex; flex-wrap: wrap; justify-content: flex-start;">
    <div style="display: flex; align-items: center; margin: 10px;">
        <img src="docs/images/logo/img_4.jpeg" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;" >混元智能体平台&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/img_1.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;">扣子智能体平台&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/img_2.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;">文心智能体平台&nbsp;&nbsp;&nbsp;</span>
        <img src="docs/images/logo/img_3.png" width="20" style="margin: 10px;" height="20">
        <span style="font-size: 12px;">智谱智能体平台&nbsp;&nbsp;&nbsp;</span>
    </div>
</div>

## 在线Demo

为了让用户能够直观感受到Lag[i] (联基) 的强大功能和便捷性，我们提供了一个在线Demo。您可以通过以下链接访问并体验：[https://lagi.landingbj.com](https://lagi.landingbj.com/)。

## 快速开始

对于开发者而言，我们提供了简便的方法来编译和运行Lag[i] (联基) 应用。您可以选择使用maven工具进行封包，或者通过IntelliJ IDEA等主流的集成开发环境（IDE）运行。请确保您的JDK版本至少满足不小于8的要求。

### 方法一：使用maven

首先，您需要克隆Lag[i] (联基) 项目的仓库，并切换到项目目录：

```shell
git clone https://github.com/landingbj/lagi.git
cd lagi
```

接下来，修改[`src/main/resources/lagi.yml`](lagi-web/src/main/resources/lagi.yml)配置文件，将其中的大语言模型API_KEY或SECRET_KEY替换为您自己的密钥，并根据需要将启用的模型的`enable`字段设置为`true`。详细的配置方法可参见[配置文档](docs/config_zh.md)。

```yaml
- name: gpt-test
  type: GPT
  enable: true
  priority: 1
  model: gpt-3.5-turbo-1106
  api_key: your-apikey
```

然后，使用maven命令进行项目封包，封包完成后的war文件将会在`target`目录下生成：

```shell
mvn package
```

最后，将生成的war包部署到Tomcat服务器中。启动Tomcat后，通过浏览器访问对应的端口，即可查看Lag[i] (联基) 的具体页面。

### 方法二：使用IDE

如果您更习惯使用IDE进行开发，可以直接使用IntelliJ IDEA等主流IDE打开Lag[i] (联基) 项目。将项目作为web工程发布到Tomcat服务器中后，就可以通过浏览器访问相应的端口，查看Lag[i] (联基) 的界面。

### 方法三：Docker 

- 镜像名称：`yinruoxi666/lagi-web:1.0.0`

- 拉取命令：

  ```bash
  docker pull yinruoxi666/lagi-web:1.0.0
  ```

- 启动容器：

  ```bash
  docker run -d --name lagi-web -p 8080:8080 yinruoxi666/lagi-web:1.0.0
  ```

## 教学演示

为了帮助您高效地熟悉并运用 Lag[i] (联基) ，我们准备了一套全面的[教学演示](docs/tutor_zh.md)。通过该教程，无论您是初学者还是有经验的开发者，都能快速上手。该教程还涵盖了 Lag[i] (联基) 基础环境向量数据库的搭建，手把手地引导您从 Lag[i] (联基) 的下载、安装、配置到实际运行，让您轻松入门，快速掌握 Lag[i] (联基) 的操作技巧。

## API接口

Lag[i] (联基) 项目还提供了多个RESTful API接口，方便用户将AI服务集成到自己的应用中。我们为开发者准备了详尽的[API接口文档](docs/API_zh.md)，您可以参考文档了解如何使用这些API来丰富您的应用功能。

## 开发集成

为了帮助您更好地了解和使用 Lag[i] (联基) ，我们为您提供了详尽的[指南](docs/guide_zh.md)，您可以参考该文档了解项目的结构和内容，以便您能够快速调用相关功能，并高效地开发大模型应用。帮助您理解并使用项目中提供的各种 AI 功能。通过这份指南，您可以轻松地将文本对话、语音识别、文字转语音、图片生成等 AI 功能集成到您的应用程序中，实现更智能、更人性化的交互体验。

如果您希望将Lag[i] (联基) 集成到您的项目中，您可以参考我们的[集成文档](docs/guide_zh.md#快速集成进您的项目)。该文档将助您轻松实现Lag[i] (联基) 的快速集成。此外，我们还提供了常见问题解答和最佳实践，帮助您在集成过程中避免常见陷阱，确保项目顺利进行。它涵盖了从环境搭建到功能实现的全方位指导。无论您是初学者还是有经验的开发者，都能快速将lag[i] (联基) 集成进您的项目中。

## 自由扩展 

如您对Lag[i] (联基) 已适配的大模型不满意，您还可以参考我们的[扩展文档](docs/extend_zh.md)，来对Lag[i] (联基) 进行扩展，适配您喜欢的大模型。该文档不仅涵盖了功能模型的适配扩展和向量数据库的扩展方法，还提供了扩展示列，帮助您快速掌握Lag[i] (联基) 扩展的方法，以满足您的特定需求。

若您在使用Lag[i] (联基) 时，对其已适配的向量数据库感到不够满意，您也可以参考我们的[扩展文档](docs/extend_cn.md#数据库扩展)，来对Lag[i] (联基) 进行扩展，适配您喜欢的向量数据库。从而满足您多样化的业务需求，提升系统的整体性能和可靠性，为您带来更加丰富和高效的数据管理体验。

## 安全过滤

为了更好的将Lag[i] (联基) 融入到您的业务中来，您可以通过在 [sensitive_word.json](lagi-web/src/main/resources/sensitive_word.json) 文件中添加您需要过滤的关键词，在 [priority_word.json](lagi-web/src/main/resources/priority_word.json) 文件中指定优先回答的关键词，以及在 [stopping_word.json](lagi-web/src/main/resources/stopping_word.json) 文件中设置停止关键词，来改变对话的返回结果，引导对话朝着特定的方向发展，并在需要时自动停止对话。

示例：

设置敏感词过滤, level有3个值，1:当匹配到敏感词时删除整句 2:替换为遮罩 3：擦除(默认值)。 mask：遮罩字符串(默认值:...)。 rules：代表的敏感规则列表， 其中每个列表元素的rule代表敏感词匹配的正则表达式,mask和level如不指明会使用全局的 :   

如匹配到`OPENAI`字样时，擦除`OPENAI`字样，如匹配到`hello`字样时，用`***`替换.如匹配到`people`字样时，用`...`替换。

```json
{
  "mask": "...",
  "level": 3,
  "rules": [
    {"rule":"OPENAI"},
    {"rule":"hello", "level": 2, "mask": "***"},
    {"rule":"people", "level": 2}
  ]
}
```

示例：设置优先级关键词、停止关键词

```json
[
  "openai"
]
```

## 下载资源

我们为用户提供了预打包的应用程序，方便直接下载和使用：

- **Jar 文件**：Lag[i] (联基)的核心依赖库。
  - 文件名：`lagi-core-1.0.2-jar-with-dependencies.jar`
  - 文件大小：250.5 MB
  - 下载链接：[点击这里下载](https://downloads.saasai.top/lagi/lagi-core-1.0.2-jar-with-dependencies.jar)

- **War 文件**：Lag[i] (联基)的Web应用，可直接部署到Web容器。
  - 文件名：`lagi-web.war`
  - 文件大小：279 MB
  - 下载链接：[点击这里下载](https://downloads.saasai.top/lagi/lagi-web.war)


感谢您对 Lag[i] (联基) 的关注与支持！如有任何问题或建议，请随时与我们联系。