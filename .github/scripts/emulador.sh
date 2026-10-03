#!/bin/bash
# Roda dentro do emulador (apk-emulador.yml): instala, abre, espera e
# confere se o jogo continua vivo e sem FATAL EXCEPTION.
set -u
PACOTE=com.emannuel.mundovivo.debug
ATIVIDADE=com.emannuel.mundovivo.android.AndroidLauncher

adb install -r mundo-vivo.apk
adb logcat -c
adb shell am start -n "$PACOTE/$ATIVIDADE"
sleep 20
pid=$(adb shell pidof "$PACOTE" | tr -d '\r')
adb logcat -d > logcat.txt
adb exec-out screencap -p > tela.png

if [ -z "$pid" ]; then
  echo "::error title=Emulador::o jogo não está rodando depois de 20 s"
  exit 1
fi
if grep -q "FATAL EXCEPTION" logcat.txt; then
  echo "::error title=Emulador::FATAL EXCEPTION no logcat"
  grep -A 20 "FATAL EXCEPTION" logcat.txt | head -n 40
  exit 1
fi
echo "::notice title=Emulador::o jogo abriu e continua vivo (pid $pid)"
