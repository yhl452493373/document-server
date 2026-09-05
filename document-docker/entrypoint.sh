#!/bin/sh
set -e

# -----------------------------
# 临时目录权限设置、清理
# -----------------------------
chmod 1777 /tmp
rm -rf /tmp/.jodconverter_* || true

# -----------------------------
# 根据 SERVER_VERSION 选择 jar 和配置
# -----------------------------
if [ "$SERVER_VERSION" = "2" ]; then
    JAR_FILE="/app/jodconverter-document-server2.jar"
    CONFIG_FILE="/app/application2.yml"
    # server2 版本使用 docx4j，无 gramer 配置
    EXTRA_ARGS=""
else
    JAR_FILE="/app/jodconverter-document-server.jar"
    CONFIG_FILE="/app/application.yml"
    # 原版本使用 poi-tl，有 gramer 配置
    EXTRA_ARGS="--document.gramer.prefix=\"${GRAMER_PREFIX}\" \
--document.gramer.suffix=\"${GRAMER_SUFFIX}\" \
--document.gramer.customize-list=\"${GRAMER_CUSTOMIZE_LIST}\" \
--document.gramer.customize-list-string-delimiting=\"${GRAMER_CUSTOMIZE_LIST_STRING_DELIMITING}\""
fi

echo "使用服务版本: $SERVER_VERSION"
echo "JAR: $JAR_FILE"

# -----------------------------
# 启动 Java 应用
# -----------------------------
echo "Starting Java application..."

cat > /app/start.sh <<EOF
#!/bin/sh
exec java -jar $JAR_FILE \
    --server.port=${DOCUMENT_SERVER_PORT} \
    --spring.config.location=$CONFIG_FILE \
    --jodconverter.local.port-numbers=${PORT_NUMBERS} \
    --jodconverter.local.max-tasks-per-process=${MAX_TASKS_PER_PROCESS} \
    $EXTRA_ARGS
EOF

chmod 755 /app/start.sh
exec /app/start.sh
