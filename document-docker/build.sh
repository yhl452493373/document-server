#!/bin/bash
# 设置Maven路径
export PATH=$PATH:/Volumes/Working/Software/apache-maven-3.9.3/bin

# 创建临时目录存放需要运行的jar
mkdir -p app

# 构建jar
echo 构建jar
mvn -f ../pom.xml clean package -Dmaven.test.skip=true
echo jar构建完成

# 复制所需文件
cp ../document-server/jodconverter-document-server/target/document-server-*.jar app/
cp ../document-server/jodconverter-document-server/target/application.yml app/

# 执行构建

#amd64
echo 开始构建amd64镜像...
docker buildx build --platform linux/amd64 -t document-server:1.0 ./
rm -rf document-server-1.0.tar.gz
docker save document-server:1.0 | gzip > document-server-1.0.tar.gz
echo amd64镜像构建成功.

#arm64
echo 开始构建arm64镜像...
docker buildx build --platform linux/arm64 -t document-server:1.0.arm64 ./
rm -rf document-server-1.0.arm64.tar.gz
docker save document-server:1.0.arm64 | gzip > document-server-1.0.arm64.tar.gz
echo arm64镜像构建成功.

# 清理临时文件
rm -rf app
