# document

文书服务

api下为调用的接口，在需要处理文书的项目中引用，以`fegin方式`配置Service后进行调用

server下为使用jacob处理word文书，使用openoffice来进行pdf转换，`java -jar xxx.jar`启动

tl-server下仅进行word转pdf，使用的docto调用office来转换，`java -jar xxx.jar`启动