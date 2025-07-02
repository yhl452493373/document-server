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

## windows下才用[docto-document-server](document-server/docto-document-server)

通过`java -jar ./document-server-2.0.0.jar --spring.config.location=./application.yml`执行