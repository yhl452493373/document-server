# document-docker

Docker 镜像构建目录，将两个文档服务（`jodconverter-document-server` 和 `jodconverter-document-server2`）打包到同一镜像中，运行时通过环境变量选择启动哪个服务。

## 目录结构

```
document-docker/
├── build.sh           # 构建脚本（Maven 构建 + Docker 镜像打包）
├── Dockerfile         # 镜像定义
├── entrypoint.sh      # 容器启动入口，根据 SERVER_VERSION 选择服务
├── install-fonts.sh   # 宿主机字体安装脚本（可选，用于非 Docker 部署）
├── fonts/             # 中文字体文件
│   ├── truetype/      # 常见中文字体（宋体、黑体、楷体、仿宋、微软雅黑等）
│   ├── cesi/          # CESI 字体
│   ├── gb/            # 国标字体
│   └── wps-office/    # WPS/方正字体
├── document-server-1.0.tar.gz         # amd64 镜像归档
└── document-server-1.0.arm64.tar.gz   # arm64 镜像归档
```

## LibreOffice 安装方案

本项目提供两种 LibreOffice 安装方案：

### 主方案：官方 deb 包（推荐）

- **版本**：26.8.0（最新稳定版）
- **文件**：`Dockerfile` + `build.sh`
- **特点**：
  - 首次构建时从镜像站下载官方 deb 包，后续使用缓存
  - 安装路径：`/opt/libreoffice26.8`
  - 通过符号链接映射到 `/usr/lib/libreoffice`，便于 JODConverter 自动识别
  - 需要手动处理部分依赖（已在 Dockerfile 中配置）

### 备选方案：PPA 源

- **版本**：26.2.5.2（非最新版）
- **文件**：`Dockerfile.ppa` + `build.sh.ppa`
- **特点**：
  - 构建简单，无需手动下载
  - 安装路径：`/usr/lib/libreoffice`（与 apt 安装一致）
  - 自动处理依赖
  - PPA 目前未提供 26.8.0 版本

如需切换到 PPA 方案，将文件重命名即可：
```bash
mv Dockerfile Dockerfile.official
mv build.sh build.sh.official
mv Dockerfile.ppa Dockerfile
mv build.sh.ppa build.sh
```

## 构建

执行 [build.sh](build.sh)：

```bash
./build.sh
```

脚本会自动完成：
1. Maven 构建两个服务模块的 jar
2. 复制 jar 和配置文件到临时 `app/` 目录（server2 的配置重命名为 `application2.yml`）
3. 分别构建 amd64 和 arm64 架构的 Docker 镜像
4. 导出镜像为 `.tar.gz` 文件

如果 jar 已存在，可跳过 Maven 构建：

```bash
./build.sh --skip-build
```

## 导入镜像

```bash
# amd64
docker load -i document-server-1.0.tar.gz

# arm64
docker load -i document-server-1.0.arm64.tar.gz
```

## 运行

通过 `docker-compose.yml` 启动：

```yaml
version: '3.8'
services:
  document-server:
    image: yhl452493373/document-server:1.0
    container_name: document-server
    environment:
      # 服务版本选择：1 (默认, poi-tl) 或 2 (docx4j)
      - SERVER_VERSION=1
      - DOCUMENT_SERVER_PORT=9004
      - PORT_NUMBERS=2001,2002,2003
      - MAX_TASKS_PER_PROCESS=100
      # 以下配置仅版本 1 需要（版本 2 无需这些配置）
      # 传递环境变量时，$ 是特殊符号，需要 $$ 来表示
      # linux 命令中，$ 也是特殊符号，需要在前面增加 \ 表示转义特殊符号
      # 如果需要传递 ${ ，则需要写成 \$${
      - GRAMER_PREFIX="\$${"
      - GRAMER_SUFFIX="}"
      - GRAMER_CUSTOMIZE_LIST="%"
      - GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING="，"
    ports:
      - 9004:9004
    volumes:
      - ./config:/config
    restart: unless-stopped
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `SERVER_VERSION` | `1` | 服务版本：`1` = poi-tl，`2` = docx4j |
| `DOCUMENT_SERVER_PORT` | `9004` | 服务端口 |
| `PORT_NUMBERS` | `2002` | LibreOffice 进程端口，逗号分隔，每个端口一个常驻进程 |
| `MAX_TASKS_PER_PROCESS` | `200` | 每个进程最多处理任务数 |
| `GRAMER_PREFIX` | `${` | 模板语法前缀（仅版本 1） |
| `GRAMER_SUFFIX` | `}` | 模板语法后缀（仅版本 1） |
| `GRAMER_CUSTOMIZE_LIST` | `%` | 自定义列表标签（仅版本 1） |
| `GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING` | `，` | 列表字符串分隔符（仅版本 1） |

## 宿主机字体安装

如果非 Docker 部署，可执行 [install-fonts.sh](install-fonts.sh) 将字体安装到 Linux 宿主机：

```bash
./install-fonts.sh
```
