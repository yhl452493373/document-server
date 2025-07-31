#!/bin/sh

# 确保目录存在
mkdir -p /etc/services.d/java

# 创建Java服务启动脚本
cat > /etc/services.d/java/run <<EOF
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

# 设置可执行权限
chmod +x /etc/services.d/java/run

# 启动s6-overlay（它会自动管理所有服务）
exec /init