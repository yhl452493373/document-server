#!/bin/sh
set -e

# -----------------------------
# 临时目录权限设置、清理
# -----------------------------
chmod 1777 /tmp
rm -rf /tmp/.jodconverter_* || true

# -----------------------------
# 启动 Java 应用
# -----------------------------
echo "Starting Java application..."

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
exec /app/start.sh