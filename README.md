# document-server 文书服务

+ api下为调用的接口，在需要处理文书的项目中引用，以`Http Invoker`配置Service后进行调用
    + 参考[在Spring Boot中使用Http Invoker](https://codeleading.com/article/15413828287/) 的`Client`部分

+ [jodconverter-document-server](document-server/jodconverter-document-server)下使用poi-tl处理word文件，使用JodConverter调用[LibreOffice](https://zh-cn.libreoffice.org/)来进行格式转换，`java -jar xxx.jar`启动
  + 参考：[springboot整合libreoffice（两种方式，使用本地和远程的libreoffice）；docker中同时部署应用和libreoffice](https://blog.csdn.net/qq_42882229/article/details/140917550)
  + 在Linux下，需要注意word文件的字体，必须在Linux中存在
  + 若出现格式问题，需先使用LibreOffice打开修复后，在进行转换

+ [docto-document-server](document-server/docto-document-server)下使用poi-tl处理word文件，使用docto调用`Microsoft Office`来转换格式，`java -jar xxx.jar`启动
    + 仅能运行在Windows，兼容性最好，比较慢
  
+ [jodconverter-document-server2](document-server/jodconverter-document-server2)下使用 word-generator（基于 docx4j）处理 Word 模板，使用 JodConverter 调用 [LibreOffice](https://zh-cn.libreoffice.org/) 进行格式转换，`java -jar xxx.jar`启动
  + 与 jodconverter-document-server 的区别：模板引擎使用 docx4j（word-generator）而非 poi-tl
  + 需要安装 LibreOffice
  + 在 Linux 下同样需要注意字体问题

+ [document-docker/fonts](document-docker/fonts)为linux下需要安装的中文字体

---

## linux下使用[jodconverter-document-server](document-server/jodconverter-document-server)

linux下后台运行方法（假设在/root/document-server）：

1. `document-server-2.0.0.jar`所在位置创建`start-server.sh`脚本

```shell
#!/bin/bash
# 设置终端标题
echo -ne "\033]0;文书转换服务\007"
# 自动获取脚本所在绝对目录（兼容软链接）
SCRIPT_DIR=$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" &>/dev/null && pwd)
# 进入脚本目录并启动服务
cd "$SCRIPT_DIR" || exit 1
exec java -jar ./document-server-2.0.0.jar --spring.config.location=./application.yml
```

2.创建`document-server.service`

```shell
sudo vim /etc/systemd/system/document-server.service
```

`document-server.service`内容

```ini
[Unit]
Description=Document Server Service
After=network.target

[Service]
Type=simple
User=root
ExecStart=/root/document-server/start-server.sh
StandardOutput=file:/root/document-server/document-server.log
StandardError=inherit
Restart=on-failure
RestartSec=5s

[Install]
WantedBy=multi-user.target
```

3.启动服务并开机自启
```shell
sudo systemctl daemon-reload
sudo systemctl enable document-server
sudo systemctl start document-server
```

---

## 构建Docker镜像以便于通过Docker容器运行

1.执行[build.sh](document-docker/build.sh)（会自动构建两个服务模块的 jar）

2.通过`docker load -i document-server-1.0.tar.gz`导入镜像，通过`docker ps | grep document-server`查看镜像版本

3.编写`docker-compose.yml`，在其中指定镜像启动，通过 `SERVER_VERSION` 环境变量选择服务版本
```yml
version: '3.8'
services:
  document-server:
    image: document-server:1.0
    container_name: document-server
    environment:
      - DISABLE_IPV6=true
      - CUSTOM_PORT=3000
      - CUSTOM_HTTPS_PORT=3001
      # 服务版本选择：1 (默认, poi-tl) 或 2 (word-generator, 格式兼容性更好)
      - SERVER_VERSION=1
      - DOCUMENT_SERVER_PORT=9004
      - PORT_NUMBERS=2001,2002,2003
      - MAX_TASKS_PER_PROCESS=100
      # 以下配置仅版本 1 需要（版本 2 无需这些配置）
      # 传递环境变量时，$ 是特殊符号，需要 $$ 来表示
      # linux 命令中，$ 也是特殊符号，需要在前面增加 \ 表示转义特殊符号
      # 如果需要传递 ${ ，则需要写成 \$${ 。其首先被 docker 解析为 \${ ，\${ 作为linux命令的一部分，$转义后，相当于字符串 ${
      # 如果不是类似 ${ ，如 {{ ，则无需转义
      - GRAMER_PREFIX="\$${"
      - GRAMER_SUFFIX="}"
      - GRAMER_CUSTOMIZE_LIST="%"
      - GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING="，"
    ports:
      # 文书转换服务端口，用于document-api远程调用document-server
      - 9004:9004
    volumes:
      # libreoffice的配置保存路径
      - ./config:/config
    restart: unless-stopped
    # 如果要使用现有网络而不创建新网络，则取消下面的注释
#    networks:
#      # 现有网络的名称
#      - exist_network

# 如果要使用现有网络而不创建新网络，则取消下面的注释
#networks:
#  # 现有网络的名称
#  exist_network:
#    external: true
```

### 服务版本说明

| SERVER_VERSION | 模板引擎 | 说明 |
|----------------|----------|------|
| `1` | poi-tl | 默认值，使用 poi-tl 模板语法，功能丰富（SpringEL、图表、嵌套表格等） |
| `2` | word-generator (docx4j) | 部分模仿 poi-tl 语法，转换后格式兼容性更好；基于 [word-generator-engine](https://gitea.yanghuanglin.cn/yhl452493373/word-generator-engine) 项目 |

`document-server` 的 `application.yml` 默认内容如下：
```yml
server:
  port: ${DOCUMENT_SERVER_PORT:9004}

document:
  gramer:
    default:
      prefix: '${'
      suffix: '}'
    prefix: ${GRAMER_PREFIX:${document.gramer.default.prefix}}
    suffix: ${GRAMER_SUFFIX:${document.gramer.default.suffix}}
    customize-list: ${GRAMER_CUSTOMIZE_LIST:%}
    customize-list-string-delimiting: ${GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING:，}
  min-inflate-ratio: 0.0
  spring-el: false

jodconverter:
  local:
    # 启动本地转换
    enabled: true
    # macOS下：program/soffice 的 program 所在目录 或 MacOS/soffice 的 MacOS 所在目录
    # windows下：program/soffice.exe 的 program 所在目录
    # linux下：program/soffice.bin 的 program 所在目录
    # 如果不配置，则自动查找
    #office-home: /Applications/LibreOffice.app/Contents
    # 一个端口表示一个常驻进程，默认只有一个进程，端口为2002
    port-numbers: ${PORT_NUMBERS:2002}
    # 每个进程最多处理多个任务，默认为200
    max-tasks-per-process: ${MAX_TASKS_PER_PROCESS:200}
```

`document-server2` 的 `application.yml` 默认内容如下：
```yml
server:
  port: ${DOCUMENT_SERVER_PORT:9004}

logging:
  level:
    org.docx4j: WARN

jodconverter:
  local:
    # 启动本地转换
    enabled: true
    # macOS下：program/soffice 的 program 所在目录 或 MacOS/soffice 的 MacOS 所在目录
    # windows下：program/soffice.exe 的 program 所在目录
    # linux下：program/soffice.bin 的 program 所在目录
    # 如果不配置，则自动查找
    #office-home: /Applications/LibreOffice.app/Contents
    # 一个端口表示一个常驻进程，默认只有一个进程，端口为2002
    port-numbers: ${PORT_NUMBERS:2002}
    # 每个进程最多处理多个任务，默认为200
    max-tasks-per-process: ${MAX_TASKS_PER_PROCESS:200}
```

### 客户端调用依赖

客户端项目需引入 `document-api`，根据后端不同还需引入对应的模板引擎包（用于构造 `PictureRenderData`、`PictureData` 等模板数据对象），通过 `HttpInvoker` 配置远程调用：

```xml
<!-- 接口定义，始终需要 -->
<dependency>
    <groupId>com.optima</groupId>
    <artifactId>document-api</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- 对接 poi-tl 后端（jodconverter-document-server 或 docto-document-server）时引入 -->
<dependency>
    <groupId>com.deepoove</groupId>
    <artifactId>poi-tl</artifactId>
    <version>1.12.2</version>
</dependency>

<!-- 对接 docx4j 后端（jodconverter-document-server2）时引入 -->
<dependency>
    <groupId>info.hncy</groupId>
    <artifactId>word-generator-model</artifactId>
    <version>1.0.0</version>
</dependency>
```

> 如果只使用 `convert`、`wordToPdf`、`docToDocx` 等不涉及模板数据的方法，仅引入 `document-api` 即可。

客户端配置示例：

```java
@Bean
public DocumentService documentService() {
    HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
    factory.setServiceUrl("http://localhost:9004/document-service");
    factory.setServiceInterface(DocumentService.class);
    factory.afterPropertiesSet();
    return (DocumentService) factory.getObject();
}
```

各服务端对比及客户端依赖：

| 服务 | 模板引擎 | 格式转换 | 客户端需引入 |
|------|----------|----------|-------------|
| `jodconverter-document-server` | poi-tl | JodConverter + LibreOffice | `document-api` + `poi-tl` |
| `jodconverter-document-server2` | word-generator (docx4j) | JodConverter + LibreOffice | `document-api` + `word-generator-model` |
| `docto-document-server` | poi-tl | docto + Microsoft Office | `document-api` + `poi-tl` |

---

## windows下才用[docto-document-server](document-server/docto-document-server)

通过`java -jar ./document-server-2.0.0.jar --spring.config.location=./application.yml`执行