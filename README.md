# document-server 文书服务

+ api下为调用的接口，在需要处理文书的项目中引用，以`Http Invoker`配置Service后进行调用
    + 参考[在Spring Boot中使用Http Invoker](https://codeleading.com/article/15413828287/) 的`Client`部分

+ [jod-document-server](document-server/jod-document-server)下使用poi-tl处理word文件，使用jacob处理调用[LibreOffice](https://zh-cn.libreoffice.org/)来进行格式转换，`java -jar xxx.jar`启动
  + 参考：[springboot整合libreoffice（两种方式，使用本地和远程的libreoffice）；docker中同时部署应用和libreoffice](https://blog.csdn.net/qq_42882229/article/details/140917550)
  + 在Linux下，需要注意word文件的字体，必须在Linux中存在
  + 若出现格式问题，需先使用LibreOffice打开修复后，在进行转换

+ [docto-document-server](document-server/docto-document-server)下使用poi-tl处理word文件，使用docto调用`Microsoft Office`来转换格式，`java -jar xxx.jar`启动
    + 仅能运行在Windows，兼容性最好，比较慢
  
+ [document-fonts](document-fonts)为linux下需要安装的中文字体

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

1.先通过maven打包[jodconverter-document-server](document-server/jodconverter-document-server)

2.执行[build.sh](document-docker/build.sh)

3.通过`docker load -i document-server-1.0.tar.gz`导入镜像，通过`docker ps | grep document-server`查看镜像版本

4.编写`docker-compose.yml`，在其中指定镜像启动
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
      - DOCUMENT_SERVER_PORT=9004
      - PORT_NUMBERS=2001,2002,2003
      - MAX_TASKS_PER_PROCESS=100
    ports:
      # 用于通过http访问libreoffice
      - 3000:3000
      # 用于通过https访问libreoffice
      - 3001:3001
      # 文书转换服务端口，用于document-api远程调用document-server
      - 9004:9004
    volumes:
      # libreoffice的配置保存路径
      - ./config:/config
      # document-server的配置，如不指定，则以默认为准。建议通过环境变量修改关键配置
      - ./application.yml:/app/application.yml
    restart: unless-stopped
```

`application.yml`默认内容如下：
```yml
server:
  port: ${DOCUMENT_SERVER_PORT:9004}

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

---

## windows下才用[docto-document-server](document-server/docto-document-server)

通过`java -jar ./document-server-2.0.0.jar --spring.config.location=./application.yml`执行