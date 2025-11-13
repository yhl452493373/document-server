# 基础镜像：Eclipse Temurin JRE 8（多架构支持，Ubuntu Noble）
FROM eclipse-temurin:8-jre-noble

# 设置环境变量
ENV TZ=Asia/Shanghai
ENV LANG=zh_CN.UTF-8
ENV LANGUAGE=zh_CN.UTF-8
ENV LC_ALL=zh_CN.UTF-8

# 安装 LibreOffice + 字体支持
RUN rm -f /etc/apt/sources.list.d/* && \
    echo "deb https://mirrors.ustc.edu.cn/ubuntu/ noble main restricted universe multiverse" > /etc/apt/sources.list && \
    echo "deb https://mirrors.ustc.edu.cn/ubuntu/ noble-security main restricted universe multiverse" >> /etc/apt/sources.list && \
    apt update && \
    apt install -y --no-install-recommends \
        libreoffice \
        libreoffice-java-common \
        fonts-dejavu \
        fonts-noto-cjk \
        fontconfig \
        locales \
        tzdata \
        ca-certificates && \
    locale-gen zh_CN.UTF-8 && \
    rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app
RUN mkdir -p /app/logs

# 拷贝应用文件、配置、字体
COPY app/document-server-*.jar /app/application.jar
COPY app/application.yml /app/application.yml
COPY fonts /usr/share/fonts/truetype
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# 环境变量配置
ENV DOCUMENT_SERVER_PORT=9004
ENV PORT_NUMBERS=2002
ENV MAX_TASKS_PER_PROCESS=200
ENV GRAMER_PREFIX="\\\${"
ENV GRAMER_SUFFIX="}"
ENV GRAMER_CUSTOMIZE_LIST="%"
ENV GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING="，"

# 暴露端口
EXPOSE 9004

# 启动入口
ENTRYPOINT ["/entrypoint.sh"]
