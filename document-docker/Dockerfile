FROM lscr.io/linuxserver/libreoffice:latest

RUN echo "https://mirrors.tencent.com/alpine/v$(cut -d'.' -f1,2 /etc/alpine-release)/main/" > /etc/apk/repositories
RUN echo "https://mirrors.tencent.com/alpine/v$(cut -d'.' -f1,2 /etc/alpine-release)/community/" >> /etc/apk/repositories

RUN apk update
RUN apk add --no-cache openjdk17-jre
RUN rm -rf /var/cache/apk/*

WORKDIR /app
RUN mkdir -p /app/logs

COPY app/document-server-*.jar /app/application.jar
COPY app/application.yml /app/application.yml

COPY fonts /usr/share/fonts/truetype

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

#see https://hub.docker.com/r/linuxserver/libreoffice
ENV PUID=1000
ENV PGID=1000
ENV TZ=Asia/Shanghai
ENV LC_ALL=zh_CN.UTF-8
ENV TITLE="Libre Office"
ENV CUSTOM_PORT=3000
ENV CUSTOM_HTTPS_PORT1=3001

ENV DOCUMENT_SERVER_PORT=9004
ENV PORT_NUMBERS=2002
ENV MAX_TASKS_PER_PROCESS=200

ENTRYPOINT ["/entrypoint.sh"]