简体中文| [English](README.md)

### **项目简介**

Lag[i]是由北京联动北方科技有限公司精心打造的一款针对于大模型开源社区的强大力量和企业应用部署之间存在的显著差距而设计的解决方案。它是一款企业级的复合多模态大模型中间件，旨在桥接这一鸿沟，通过提供一个既安全又专业的平台，使得企业能够以低成本、高效率地方式定制并部署大模型。

### 在线Demo

为了让用户能够直观感受到Lag[i]的强大功能和便捷性，我们提供了一个在线Demo。您可以通过以下链接访问并体验：[https://lagi.landingbj.com](https://lagi.landingbj.com/)。

### 快速开始

对于开发者而言，我们提供了简便的方法来编译和运行Lag[i]应用。您可以选择使用maven命令行工具进行封包，或者通过IntelliJ IDEA等主流的集成开发环境（IDE）进行运行。请确保您的JDK版本至少满足8的要求。

#### 方法一：使用maven命令

首先，您需要克隆Lag[i]项目的仓库，并切换到项目目录：

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

最后，将生成的war包部署到Tomcat服务器中。启动Tomcat后，通过浏览器访问对应的端口，即可查看Lag[i]的具体页面。

#### 方法二：使用IDE
如果您更习惯使用IDE进行开发，可以直接使用IntelliJ IDEA等主流IDE打开Lag[i]项目。将项目作为web工程发布到Tomcat服务器中后，就可以通过浏览器访问相应的端口，查看Lag[i]的界面。

### API接口

Lag[i]项目还提供了多个RESTful API接口，方便用户将AI服务集成到自己的应用中。我们为开发者准备了详尽的[API接口文档](docs/API_zh.md)，您可以参考文档了解如何使用这些API来丰富您的应用功能。

### 模型代码调用指南
为了帮助您更好地了解和使用 Lag[i]，我们为您提供了详尽的[模型代码调用指南](docs/guide_cn.md)。您可以参考文档了解项目的结构和内容，以便您能够快速调用相关功能，并高效地开发大模型应用。