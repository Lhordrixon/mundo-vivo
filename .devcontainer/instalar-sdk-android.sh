#!/usr/bin/env bash
# Instala o mínimo do SDK do Android para o Gradle configurar o projeto.
# Roda uma vez, quando o Codespace é criado. Leva alguns minutos.
set -euo pipefail

SDK="${ANDROID_HOME:-$HOME/android-sdk}"
FERRAMENTAS="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

if [ -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "SDK do Android já instalado em $SDK"
else
  if ! command -v unzip > /dev/null; then
    sudo apt-get update -qq && sudo apt-get install -y -qq unzip
  fi
  echo "Baixando as ferramentas do SDK do Android..."
  mkdir -p "$SDK/cmdline-tools"
  curl -fsSL -o /tmp/cmdline-tools.zip "$FERRAMENTAS"
  unzip -q /tmp/cmdline-tools.zip -d "$SDK/cmdline-tools"
  mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm /tmp/cmdline-tools.zip
fi

SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
yes | "$SDKMANAGER" --sdk_root="$SDK" --licenses > /dev/null || true
"$SDKMANAGER" --sdk_root="$SDK" "platforms;android-36" "platform-tools"

echo "Pronto. Teste com: ./gradlew core:test"
