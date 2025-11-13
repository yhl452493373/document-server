#!/bin/sh
set -e

# -----------------------------
# 可选：启动 LibreOffice headless 监听服务（端口2002）用于文档转换
# -----------------------------
# 你可以根据需要开启或关闭
libreoffice --headless --accept="socket,host=0.0.0.0,port=${PORT_NUMBERS};urp;" --nologo --nofirststartwizard &
echo "LibreOffice headless started on port ${PORT_NUMBERS}"

# -----------------------------
# 启动 Java 应用
# -----------------------------
echo "Starting Java application..."
rm -rf /app/start.sh
cat > /app/start.sh <<EOF
#!/bin/sh
exec java -jar /app/application.jar \
    --server.port=${DOCUMENT_SERVER_PORT} \
    --spring.config.location=/app/application.yml \
    --jodconverter.local.port-numbers=${PORT_NUMBERS} \
    --jodconverter.local.max-tasks-per-process=${MAX_TASKS_PER_PROCESS} \
    --document.gramer.prefix="${GRAMER_PREFIX}" \
    --document.gramer.suffix="${GRAMER_SUFFIX}" \
    --document.gramer.customize-list="${GRAMER_CUSTOMIZE_LIST}" \
    --document.gramer.customize-list-string-delimiting="${GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING}"
EOF

chmod 755 /app/start.sh
sh /app/start.sh