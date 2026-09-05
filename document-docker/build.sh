#!/bin/bash
# 设置Maven路径
export PATH=$PATH:"/Volumes/Macintosh Data/Software/apache-maven-3.9.3/bin"

# 解析参数
SKIP_BUILD=false
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        -h|--help)
            echo "用法: $0 [选项]"
            echo ""
            echo "选项:"
            echo "  --skip-build   跳过 Maven 构建，使用已有的 jar"
            echo "  -h, --help     显示帮助信息"
            exit 0
            ;;
        *)
            echo "未知参数: $1"
            echo "使用 -h 或 --help 查看帮助"
            exit 1
            ;;
    esac
done

# 创建临时目录存放需要运行的jar
mkdir -p app

# 构建jar
if [ "$SKIP_BUILD" = false ]; then
    echo 构建jar
    mvn -f ../pom.xml clean package -Dmaven.test.skip=true
    echo jar构建完成
fi

# 复制两个服务的 jar 和配置
cp ../document-server/jodconverter-document-server/target/document-server-*.jar app/jodconverter-document-server.jar
cp ../document-server/jodconverter-document-server/target/application.yml app/application.yml

cp ../document-server/jodconverter-document-server2/target/document-server-*.jar app/jodconverter-document-server2.jar
cp ../document-server/jodconverter-document-server2/target/application.yml app/application2.yml

# 执行构建

#amd64
echo 开始构建amd64镜像...
docker buildx build --platform linux/amd64 -t yhl452493373/document-server:1.0 ./
rm -rf document-server-1.0.tar.gz
docker save yhl452493373/document-server:1.0 | gzip > document-server-1.0.tar.gz
echo amd64镜像构建成功.

#arm64
echo 开始构建arm64镜像...
docker buildx build --platform linux/arm64 -t yhl452493373/document-server:1.0.arm64 ./
rm -rf document-server-1.0.arm64.tar.gz
docker save yhl452493373/document-server:1.0.arm64 | gzip > document-server-1.0.arm64.tar.gz
echo arm64镜像构建成功.

# 清理临时文件
rm -rf app
