# =============================================================================
# LibreOffice 安装方案说明：
# 
# 本 Dockerfile 使用官方 deb 包安装 LibreOffice（版本 26.8.0）
# - 官方 deb 包下载地址：https://mirrors.ustc.edu.cn/tdf/libreoffice/stable/
# - 安装路径：/opt/libreoffice26.8
# - 通过环境变量 OFFICE_HOME 指定安装路径，供 JODConverter 使用
#
# 备选方案：
# - PPA 方案（版本 26.2.5.2）：见 Dockerfile.ppa 和 build.sh.ppa
# - PPA 方案构建更简单，但版本较旧
# =============================================================================

# 基础镜像：Eclipse Temurin JRE 8（多架构支持，Ubuntu Noble）
FROM eclipse-temurin:8-jre-noble

# 设置环境变量
ENV TZ=Asia/Shanghai
ENV LANG=zh_CN.UTF-8
ENV LANGUAGE=zh_CN.UTF-8
ENV LC_ALL=zh_CN.UTF-8

# 安装依赖 + 字体支持
RUN rm -f /etc/apt/sources.list.d/* && \
    ARCH=$(dpkg --print-architecture) && \
    if [ "$ARCH" = "arm64" ]; then \
        echo "deb https://mirrors.ustc.edu.cn/ubuntu-ports noble main restricted universe multiverse" > /etc/apt/sources.list && \
        echo "deb https://mirrors.ustc.edu.cn/ubuntu-ports noble-security main restricted universe multiverse" >> /etc/apt/sources.list && \
        echo "deb https://mirrors.ustc.edu.cn/ubuntu-ports noble-updates main restricted universe multiverse" >> /etc/apt/sources.list; \
    else \
        echo "deb https://mirrors.ustc.edu.cn/ubuntu noble main restricted universe multiverse" > /etc/apt/sources.list && \
        echo "deb https://mirrors.ustc.edu.cn/ubuntu noble-security main restricted universe multiverse" >> /etc/apt/sources.list && \
        echo "deb https://mirrors.ustc.edu.cn/ubuntu noble-updates main restricted universe multiverse" >> /etc/apt/sources.list; \
    fi && \
    apt update && \
    echo "ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true" | debconf-set-selections && \
    apt install -y --no-install-recommends \
        ttf-mscorefonts-installer \
        fonts-dejavu \
        fontconfig \
        locales \
        tzdata \
        ca-certificates && \
    locale-gen zh_CN.UTF-8 && \
    dpkg-reconfigure --frontend noninteractive locales && \
    rm -rf /var/lib/apt/lists/*

# 安装 LibreOffice（从构建脚本预下载的官方 deb 包）
# 版本：26.8.0（最新稳定版）
# 安装路径：/opt/libreoffice26.8
COPY app/libreoffice.tar.gz /tmp/libreoffice.tar.gz
RUN cd /tmp && \
    tar -xzf libreoffice.tar.gz && \
    dpkg -i LibreOffice_*_Linux_*_deb/DEBS/*.deb && \
    apt-get install -f -y && \
    # 安装官方 deb 包缺失的依赖
    apt update && \
    apt install -y --no-install-recommends \
        libssl3 \
        libnss3 \
        libcairo2 \
        libxrender1 \
        libxext6 \
        libx11-6 \
        libcups2 \
        libdbus-glib-1-2 \
        libxcb1 \
        libx11-xcb1 \
        libxcb-shm0 \
        libxcb-render0 \
        libxcb-shape0 \
        libxinerama1 \
        libreoffice-java-common && \
    # 创建 libssl3.so 符号链接（支持双架构）
    ARCH=$(dpkg --print-architecture) && \
    if [ "$ARCH" = "arm64" ]; then \
        ln -sf /usr/lib/aarch64-linux-gnu/libssl.so.3 /usr/lib/aarch64-linux-gnu/libssl3.so; \
    else \
        ln -sf /usr/lib/x86_64-linux-gnu/libssl.so.3 /usr/lib/x86_64-linux-gnu/libssl3.so; \
    fi && \
    # 清理 LibreOffice：仅保留无头格式转换能力（Writer/Calc/Draw/Impress）
    # 删除 Python 运行时（不需要 Python 脚本支持）
    rm -rf /opt/libreoffice26.8/program/python-core-* \
           /opt/libreoffice26.8/program/libpython* && \
    # 删除数据库连接器（不需要数据库连接）
    rm -f /opt/libreoffice26.8/program/libpostgresql-sdbc-impllo.so \
          /opt/libreoffice26.8/program/libmysqlclo.so \
          /opt/libreoffice26.8/program/libmysqljdbclo.so \
          /opt/libreoffice26.8/program/libhsqldblo.so \
          /opt/libreoffice26.8/program/libfirebirdlo.so \
          /opt/libreoffice26.8/program/liblibreofficekitgtk.so \
          /opt/libreoffice26.8/program/libldapbe2lo.so \
           /opt/libreoffice26.8/program/libbase* && \
    # 删除 share 下所有 UI 和非转换相关组件
    rm -rf /opt/libreoffice26.8/share/config/images_* \
           /opt/libreoffice26.8/share/extensions/dict-* \
           /opt/libreoffice26.8/share/extensions/wiki-publisher \
           /opt/libreoffice26.8/share/extensions/nlpsolver \
           /opt/libreoffice26.8/share/Extensions/report-builder \
           /opt/libreoffice26.8/share/gallery \
           /opt/libreoffice26.8/share/template \
           /opt/libreoffice26.8/share/wizards \
           /opt/libreoffice26.8/share/fonts \
           /opt/libreoffice26.8/share/autocorr \
           /opt/libreoffice26.8/share/autotext \
           /opt/libreoffice26.8/share/fingerprint \
           /opt/libreoffice26.8/share/firebird \
           /opt/libreoffice26.8/share/glade \
           /opt/libreoffice26.8/share/labels \
           /opt/libreoffice26.8/share/palette \
           /opt/libreoffice26.8/share/skia \
           /opt/libreoffice26.8/share/svx \
           /opt/libreoffice26.8/share/theme_definitions \
           /opt/libreoffice26.8/share/themes \
           /opt/libreoffice26.8/share/tipoftheday \
           /opt/libreoffice26.8/share/toolbarmode \
           /opt/libreoffice26.8/share/wordbook \
           /opt/libreoffice26.8/share/xdg \
           /opt/libreoffice26.8/share/xpdfimport \
           /opt/libreoffice26.8/share/basic \
           /opt/libreoffice26.8/share/Scripts/python \
           /opt/libreoffice26.8/share/Scripts/java/NLPSolver \
           /opt/libreoffice26.8/help \
           /opt/libreoffice26.8/readmes \
           /opt/libreoffice26.8/LICENSE \
           /opt/libreoffice26.8/LICENSE.html \
           /opt/libreoffice26.8/CREDITS.fodt \
           /opt/libreoffice26.8/NOTICE && \
    # 清理 apt 缓存、文档、man
    apt autoremove -y && \
    apt clean && \
    rm -rf /tmp/libreoffice* /var/lib/apt/lists/* \
           /usr/share/doc/* /usr/share/man/* /usr/share/info/* \
           /usr/share/locale/?? /usr/share/locale/??_* \
           /usr/share/groff/* /usr/share/lintian/*

# 设置工作目录
WORKDIR /app
RUN mkdir -p /app/logs

# 拷贝应用文件（两个服务的 jar 和配置）
COPY app/jodconverter-document-server.jar /app/jodconverter-document-server.jar
COPY app/jodconverter-document-server2.jar /app/jodconverter-document-server2.jar
COPY app/application.yml /app/application.yml
COPY app/application2.yml /app/application2.yml

# 拷贝字体文件并刷新字体缓存
COPY fonts/truetype /usr/share/fonts/truetype
COPY fonts/cesi /usr/share/fonts/cesi
COPY fonts/gb /usr/share/fonts/gb
COPY fonts/wps-office /usr/share/fonts/wps-office
RUN fc-cache -fv

# 入口
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# 环境变量配置
# SERVER_VERSION: 1 (默认, poi-tl) 或 2 (docx4j)
ENV SERVER_VERSION=1
ENV DOCUMENT_SERVER_PORT=9004
ENV PORT_NUMBERS=2002
ENV MAX_TASKS_PER_PROCESS=200
ENV GRAMER_PREFIX="\\\${"
ENV GRAMER_SUFFIX="}"
ENV GRAMER_CUSTOMIZE_LIST="%"
ENV GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING="，"
# LibreOffice 安装路径（官方 deb 包安装在 /opt）
ENV OFFICE_HOME=/opt/libreoffice26.8

# 暴露端口
EXPOSE 9004

# 启动入口
ENTRYPOINT ["/entrypoint.sh"]
