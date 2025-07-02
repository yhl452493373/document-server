# document

文书服务

+ api下为调用的接口，在需要处理文书的项目中引用，以`Http Invoker`配置Service后进行调用
    + 参考[在Spring Boot中使用Http Invoker](https://codeleading.com/article/15413828287/) 的`Client`部分

+ [jod-document-server](document-server/jod-document-server)下使用poi-tl处理word文件，使用jacob处理调用[LibreOffice](https://zh-cn.libreoffice.org/)来进行格式转换，`java -jar xxx.jar`启动
  + 参考：[springboot整合libreoffice（两种方式，使用本地和远程的libreoffice）；docker中同时部署应用和libreoffice](https://blog.csdn.net/qq_42882229/article/details/140917550)
  + 在Linux下，需要注意word文件的字体，必须在Linux中存在
  + 若出现格式问题，需先使用LibreOffice打开修复后，在进行转换

+ [docto-document-server](document-server/docto-document-server)下使用poi-tl处理word文件，使用docto调用`Microsoft Office`来转换格式，`java -jar xxx.jar`启动
    + 仅能运行在Windows，兼容性最好，比较慢
  
+ [document-fonts](document-fonts)为linux下需要安装的中文字体