# jodconverter-document-server2

基于 **docx4j + JodConverter** 的文档处理服务，通过 `HttpInvoker` 对外暴露 `DocumentService` 接口。

与 `jodconverter-document-server` 的区别：本模块使用 `word-generator`（基于 docx4j）处理 Word 模板，而非 poi-tl。

## 功能

| 方法 | 说明 |
|------|------|
| `generateWord` | 根据 docx 模板 + 数据模型生成 Word 文档 |
| `wordToPdf` | Word 转 PDF（可选清除模板占位符） |
| `wordToImage` | Word 转图片（先转 PDF 再通过 PDFBox 渲染） |
| `convert` | 通用格式转换（docx→pdf、doc→docx、xls→xlsx 等） |
| `docToDocx` | doc 转 docx |
| `xlsToXlsx` | xls 转 xlsx |

## 前置条件

需要安装 [LibreOffice](https://zh-cn.libreoffice.org/)，JodConverter 通过调用 LibreOffice 完成格式转换。

## 引入方式

### 1. 作为独立服务部署

直接 `java -jar` 启动，默认端口 9004。

### 2. 在客户端项目中远程调用

客户端项目需引入 `document-api`，通过 `HttpInvoker` 配置远程调用：

```xml
<dependency>
    <groupId>com.optima</groupId>
    <artifactId>document-api</artifactId>
    <version>2.0.0</version>
</dependency>
```

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

## 依赖清单

### 服务端自身依赖（已包含在 jar 中，部署时无需额外引入）

| 依赖 | 版本 | 说明 |
|------|------|------|
| `jodconverter-local` | 4.4.9 | LibreOffice 文档转换 |
| `jodconverter-spring-boot-starter` | 4.4.9 | Spring Boot 自动配置 |
| `word-generator` | 1.0.0 | Word 模板引擎（本地 jar，位于 `lib/`） |
| `word-generator-model` | 1.0.0 | Word 模板模型（本地 jar，位于 `lib/`） |
| `docx4j-core` | 8.3.14 | docx4j 核心 |
| `docx4j-JAXB-Internal` | 8.3.14 | docx4j JAXB 实现 |
| `pdfbox-tools` | 2.0.25 | PDF 转图片 |

### 从父模块 `document-server` 继承的依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| `document-api` | 2.0.0 | 服务接口定义 |
| `spring-boot-starter-web` | 2.7.18 | Web 支持 |
| `spring-boot-starter-aop` | 2.7.18 | AOP 支持 |
| `fastjson` | 2.0.58 | JSON 处理 |
| `fastjson2` | 2.0.57 | JSON 处理 |
| `hutool-json` | 5.8.39 | JSON 工具 |
| `log4j-to-slf4j` | 2.17.2 | 日志桥接 |
| `lombok` | 1.18.38 | 编译期代码生成 |

## 配置

```yaml
server:
  port: ${DOCUMENT_SERVER_PORT:9004}

jodconverter:
  local:
    enabled: true
    # LibreOffice 安装路径（不配置则自动查找）
    #office-home: /Applications/LibreOffice.app/Contents
    # LibreOffice 进程端口，每个端口一个进程
    port-numbers: ${PORT_NUMBERS:2002}
    # 每个进程最多处理任务数
    max-tasks-per-process: ${MAX_TASKS_PER_PROCESS:200}
```
