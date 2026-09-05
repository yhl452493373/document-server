# document-api

文档操作接口定义模块，提供 `DocumentService` 接口和 `Gramer` 语法配置类。

客户端项目引入此包后，通过 `HttpInvoker` 配置远程调用后端服务。

## 引入依赖

### 必须引入

```xml
<!-- 接口定义，始终需要 -->
<dependency>
    <groupId>com.optima</groupId>
    <artifactId>document-api</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 按后端选择引入

`generateWord` 的 `Map<String, Object>` 数据模型中，图片等复杂类型必须与后端模板引擎匹配，因此客户端还需引入对应后端的包来构造这些对象。

**对接 poi-tl 后端**（`jodconverter-document-server` 或 `docto-document-server`）：

```xml
<dependency>
    <groupId>com.deepoove</groupId>
    <artifactId>poi-tl</artifactId>
    <version>1.12.2</version>
</dependency>
```

可用的数据模型类型：

| 类型 | 包路径 | 说明 |
|------|--------|------|
| `PictureRenderData` | `com.deepoove.poi.data` | 图片，通过 `Pictures.ofBytes(bytes).size(w, h).create()` 或 `Pictures.ofUrl(url).create()` 构造 |
| `TextRenderData` | `com.deepoove.poi.data` | 带样式文本 |
| `NumbericRenderData` | `com.deepoove.poi.data` | 编号列表 |
| `TableRenderData` | `com.deepoove.poi.data` | 动态表格 |
| `ChartRenderData` | `com.deepoove.poi.data` | 图表 |
| `DocxRenderData` | `com.deepoove.poi.data` | 嵌套文档 |

**对接 docx4j 后端**（`jodconverter-document-server2`）：

```xml
<dependency>
    <groupId>info.hncy</groupId>
    <artifactId>word-generator-model</artifactId>
    <version>1.0.0</version>
</dependency>
```

可用的数据模型类型：

| 类型 | 包路径 | 说明 |
|------|--------|------|
| `PictureData` | `info.hncy.word.generator.model` | 图片，通过 `PictureData.of(bytes).size(w, h)` 或 `PictureData.ofUrl(url)` 构造 |

此外 `byte[]`、`java.awt.Image`、`java.io.File` 也可作为图片值传入，会自动转换为 `PictureData`。

### 依赖总览

| 对接后端 | 必须引入 | 用途 |
|----------|----------|------|
| `jodconverter-document-server` | `document-api` + `poi-tl` | 构造 `PictureRenderData` 等模板数据 |
| `jodconverter-document-server2` | `document-api` + `word-generator-model` | 构造 `PictureData` 等模板数据 |
| `docto-document-server` | `document-api` + `poi-tl` | 构造 `PictureRenderData` 等模板数据 |

> 如果只使用 `convert`、`wordToPdf`、`docToDocx` 等不涉及模板数据模型的方法，仅引入 `document-api` 即可。

客户端项目需已有 `spring-web` 依赖（Spring Boot Web 项目默认包含），用于 `HttpInvokerProxyFactoryBean`。

## 客户端配置

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

注入后即可直接调用：

```java
@Autowired
private DocumentService documentService;
```

## 后端服务与接口支持

三个后端服务对 `DocumentService` 接口方法的支持情况不同，客户端应根据所使用的后端选择调用对应的方法。

| 方法 | jodconverter-document-server | jodconverter-document-server2 | docto-document-server |
|------|:---:|:---:|:---:|
| `generateWord` | ✓ | ✓ | ✓ |
| `wordToPdf` | ✓ | ✓ | ✓ |
| `wordToImage` | ✓ | ✓ | ✓ |
| `convert` | ✓ | ✓ | ✓ |
| `docToDocx` | ✓ | ✓ | ✓ |
| `xlsToXlsx` | ✓ | ✓ | ✓ |
| `gramer` | ✓ | ✗ | ✓ |

- `gramer()` 仅在使用 poi-tl 模板引擎的服务中有实际意义（`jodconverter-document-server` 和 `docto-document-server`）。`jodconverter-document-server2` 使用 docx4j，未实现此方法，调用会返回默认值，对模板处理无实际作用。

## 各后端部署说明

| 后端 | 模板引擎 | 格式转换 | 运行环境 |
|------|----------|----------|----------|
| `jodconverter-document-server` | poi-tl | JodConverter + LibreOffice | 跨平台 |
| `jodconverter-document-server2` | word-generator (docx4j) | JodConverter + LibreOffice | 跨平台 |
| `docto-document-server` | poi-tl | docto + Microsoft Office | 仅 Windows |

部署时只需启动其中一个后端服务，客户端通过 `HttpInvoker` 远程调用即可。服务端已包含所有运行时依赖，无需额外引入。
