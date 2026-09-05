# docto-document-server

基于 **poi-tl + docto** 的文档处理服务，通过 `HttpInvoker` 对外暴露 `DocumentService` 接口。

使用 [docto](https://github.com/tobya/DocTo) 调用 **Microsoft Office** 进行格式转换，兼容性最好，但**仅能运行在 Windows**。

## 功能

| 方法 | 说明 |
|------|------|
| `generateWord` | 根据 docx 模板 + 数据模型生成 Word 文档（poi-tl） |
| `wordToPdf` | Word 转 PDF（可选清除模板占位符） |
| `wordToImage` | Word 转图片（先转 PDF 再通过 PDFBox 渲染） |
| `convert` | 通用格式转换（docx→pdf、doc→docx、xls→xlsx 等） |
| `docToDocx` | doc 转 docx |
| `xlsToXlsx` | xls 转 xlsx |
| `gramer` | 获取模板语法配置 |

## 前置条件

1. **Windows 系统**（依赖 Microsoft Office）
2. 安装 Microsoft Office（Word、Excel、PowerPoint）
3. 下载 [docto.exe](https://github.com/tobya/DocTo/releases)，放到指定目录（默认 `C:\DocumentServer\docto.exe`）

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
| `poi-tl` | 1.12.2 | Word 模板引擎 |

### 从父模块 `document-server` 继承的依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| `document-api` | 2.0.0 | 服务接口定义 |
| `spring-boot-starter-web` | 2.7.18 | Web 支持 |
| `spring-boot-starter-aop` | 2.7.18 | AOP 支持 |
| `fastjson` | 2.0.58 | JSON 处理 |
| `fastjson2` | 2.0.57 | JSON 处理 |
| `hutool-json` | 5.8.39 | JSON 工具 |
| `pdfbox-tools` | 2.0.25 | PDF 转图片 |
| `log4j-to-slf4j` | 2.17.2 | 日志桥接 |
| `lombok` | 1.18.38 | 编译期代码生成 |

## 配置

```yaml
server:
  port: 9004

document:
  gramer:
    default:
      prefix: '${'
      suffix: '}'
    prefix: ${GRAMER_PREFIX:${document.gramer.default.prefix}}
    suffix: ${GRAMER_SUFFIX:${document.gramer.default.suffix}}
    customize-list: ${GRAMER_CUSTOMIZE_LIST:%}
    customize-list-string-delimiting: ${GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING:，}
  # docto.exe 路径
  doc-to-program: C:\\DocumentServer\\docto.exe
  min-inflate-ratio: 0.0
```

### 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `document.gramer.prefix` | `${` | 模板语法前缀 |
| `document.gramer.suffix` | `}` | 模板语法后缀 |
| `document.gramer.customize-list` | `%` | 自定义列表标签 |
| `document.gramer.customize-list-string-delimiting` | `，` | 列表字符串分隔符 |
| `document.doc-to-program` | `C:\DocumentServer\docto.exe` | docto.exe 路径 |
| `document.min-inflate-ratio` | `0.0` | POI 压缩检测比例（0 表示关闭） |

## 与其他模块的区别

| 模块 | 模板引擎 | 格式转换 | 运行环境 |
|------|----------|----------|----------|
| `jodconverter-document-server` | poi-tl | JodConverter + LibreOffice | 跨平台 |
| `jodconverter-document-server2` | word-generator (docx4j) | JodConverter + LibreOffice | 跨平台 |
| `docto-document-server` | poi-tl | docto + Microsoft Office | 仅 Windows |
