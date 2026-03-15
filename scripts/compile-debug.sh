#!/usr/bin/env bash
# 使用 Android Studio 自带 JBR 运行 Gradle，便于在终端/Cursor 中做编译检查（解决未配置 JAVA_HOME 时无法构建的问题）

set -e
cd "$(dirname "$0")/.."

if [ -z "$JAVA_HOME" ]; then
  # macOS 默认 Android Studio 路径
  if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  elif [ -d "/Applications/Android Studio.app/Contents/jre/Contents/Home" ]; then
    export JAVA_HOME="/Applications/Android Studio.app/Contents/jre/Contents/Home"
  else
    echo "JAVA_HOME 未设置且未找到 Android Studio JBR，请先安装 JDK 或设置 JAVA_HOME"
    exit 1
  fi
fi

./gradlew compileDebugKotlin "$@"
