# Attachment

## Chroma Installation

If you want to use retrieval-augmented generation (RAG) functionality, you generally need to set up a vector database. Below is an example using Chroma to demonstrate the steps:

### Method 1: Python

First, ensure that a Python runtime environment is installed.

Install Chroma:

```bash
pip install chromadb
```

Create a database storage directory:

```bash
mkdir db_data
```

Start the database service:

```bash
# The --path parameter can specify the data persistence path
# By default, port 8000 is enabled
chroma run --path db_data
```

### Method 2: Docker

First, ensure that a Docker runtime environment is installed.

Run the following command to pull, install, and start Chroma:

```bash
# Start the Chroma database container
#
# This command is used to start a Chroma database service container in Docker. 
# It configures the container's runtime environment and external services through several parameters.
# Parameter explanation:
# -d: Run the container in the background and return the container ID.
# --name: Assign a name to the container for easier management and identification.
# -p: Maps the container's internal port to the host port. Here, port 8000 in the container is mapped to port 8000 on the host.
# -v: Binds the container's internal directory to the host directory for persistent data storage. Here, the host directory /mydata/docker/local/chroma/data is bound to /study/ai/chroma in the container.
# -e: Sets environment variables to configure the application's behavior inside the container. Two environment variables are set here:
#     IS_PERSISTENT=TRUE indicates that the database data will be stored persistently.
#     ANONYMIZED_TELEMETRY=TRUE allows the collection of anonymized telemetry data.
# chromadb/chroma:latest: Specifies the image used, chromadb/chroma with the latest version.
docker run -d \
    --name chromadb \
    -p 8000:8000 \
    -v /mydata/docker/local/chroma/data:/study/ai/chroma \
    -e IS_PERSISTENT=TRUE \
    -e ANONYMIZED_TELEMETRY=TRUE \
    chromadb/chroma:latest
```

### Modify Configuration

To use it in LinkMind, modify the `stores>vectors>url` field in the `lagi.yml` configuration file:

```yaml
vector:
  # Chroma is an AI-native open-source embedding database
  # The official website is https://www.trychroma.com/
  - name: chroma
    driver: ai.vector.impl.ChromaVectorStore
    default_category: default
    similarity_top_k: 10
    similarity_cutoff: 0.5
    parent_depth: 1
    child_depth: 1
    url: http://localhost:8000
```

## Quick Integration

### Method 1: Directly Import the JAR Package

You can directly use LinkMind(联智) by importing its JAR package to transform a traditional business into a large model-based business.

1. **Download the JAR package**: Download the LinkMind(联智) JAR package ([Click to download](https://downloads.saasai.top/lagi/lagi-core-1.0.2-jar-with-dependencies.jar)).

2. **Import the JAR package**: Copy the downloaded JAR package to the `lib` directory of your project.

3. **Build and run the project**: Build and run the project in your environment.

**Note**:

- The JAR contains a default configuration file (`lagi.yml`), but external configurations take precedence.
- If you need to modify the configuration file, download the [lagi.yml](https://github.com/landingbj/lagi/blob/main/lagi-web/src/main/resources/lagi.yml), place it in your project's `resources` directory, and start building the project.
- Refer to the [configuration documentation](config_en.md) for detailed configuration.

### Method 2: In Eclipse and IntelliJ

#### Integrating in Eclipse

1. **Import the project**:
   - Open Eclipse.
   - Select `File > Import...`.
   - Under `General > Existing Projects into Workspace`, select the LinkMind(联智) project folder.
   - Check the projects you want to import and click `Finish`.

2. **Build path and dependencies**:
   - In `Project Explorer`, locate your project.
   - Right-click on the project and select `Properties`.
   - Under `Java Build Path > Libraries`, click `Add External JARs...` and select all the JAR files in the `libs` directory of LinkMind(联智).

3. **Synchronize the project**:
   - Right-click on the project in `Project Explorer` and select `Build Project`.

#### Integrating in IntelliJ IDEA

1. **Import the project**:
   - Open IntelliJ IDEA.
   - Select `File > Open`.
   - Choose the LinkMind(联智) project folder.
   - Click `OK`.

2. **Add dependencies**:
   - In the `Project` window, locate your project.
   - Right-click the project and select `Add Dependency...`.
   - Choose `Module Dependency` or `Project Dependency` and select the JAR files for LinkMind(联智).

3. **Synchronize the project**:
   - Right-click the project in the `Project` window and select `Build Project`.

#### **Common Issues**

- **Eclipse or IntelliJ cannot recognize the JAR file**: Ensure the JAR file is not corrupted and its path is correctly configured in the build path.
- **Dependency conflicts**: Resolve conflicts by manually adjusting the project's build path or using the IDE's dependency resolution features.
- **Note**: These steps provide a general framework. Specific integration steps may vary based on project complexity and IDE versions.

### Method 3: Docker Image

Docker allows you to package your application into a deployable container. To simplify using LinkMind, we provide a pre-built Docker image.

**1. Pull the LinkMind(联智) Docker Image**

Command to pull the image:

```text
docker pull yinruoxi666/lagi-web:1.0.0
```

**2. Start the Docker Container**

Command to start the container:

```text
docker run -d --name lagi-web -p 8080:8080 yinruoxi666/lagi-web:1.0.0
```

You can also integrate LinkMind(联智) into your own project using Docker. Below are the basic steps:

**1. Prepare the LinkMind(联智) Project**

- Ensure the LinkMind(联智) project is built without errors.
- Use Maven or another build tool to package the LinkMind(联智) project into a WAR file.

**2. Create a Dockerfile and Add the Following Content**

```dockerfile
# Use the official Tomcat image as the base image
FROM tomcat:8.5.46-jdk8-openjdk

VOLUME /usr/local/tomcat/temp
VOLUME /usr/local/tomcat/lib
ADD ./target/lagi-1.0.0.war /usr/local/tomcat/webapps/

# Expose port 8080
EXPOSE 8080

# Start command
CMD ["catalina.sh", "run"]
```

Ensure `lagi-1.0.0.war` is replaced with the actual WAR file name.

**3. Build the Docker Image in the Project Root Directory**

```shell
docker build -t lagi-image .
```

**4. Run the Container Using the Following Command**

```shell
docker run -d -p 8080:8080 lagi-image
```

Ensure `your-image-name` is replaced with the image name used in Step 3.

**5. Integrate into Your Project**

In your project, create a Dockerfile to specify how to build a container with the LinkMind(联智) image.

```dockerfile
FROM lagi-image
# Add your project directory to the container
ADD . /app
# Expose the port
EXPOSE 8080
# Start command
CMD ["catalina.sh", "run"]
```

Build the Docker image:

```shell
docker build -t your-project-image .
```

Run the Docker container:

```dockerfile
docker run -d -p 8080:8080 your-project-image
```
