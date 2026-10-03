"""Medições do diagnóstico CLEAR-X (só biblioteca padrão).

    python3 docs/clear-x/medir.py

Palavras visíveis: tudo o que aparece numa aula sem abrir nenhum
<details> (blocos de código contam, links contam pelo texto, URLs não).
É a mesma função que o aprender/verificar.py usa para o teto.
"""
import os
import re
import statistics
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.dirname(
    os.path.abspath(__file__))))
sys.path.insert(0, os.path.join(RAIZ, "aprender"))
from verificar import palavras_visiveis  # noqa: E402

AULAS = sorted(n for n in os.listdir(os.path.join(RAIZ, "aprender"))
               if re.match(r"\d\.\d-.*\.md$", n))
contagem = {}
for nome in AULAS:
    with open(os.path.join(RAIZ, "aprender", nome), encoding="utf-8") as f:
        contagem[nome[:-3]] = palavras_visiveis(f.read())
for aula, n in contagem.items():
    print("%-16s %4d" % (aula, n))
valores = sorted(contagem.values())
print("mínimo %d · mediana %d · máximo %d"
      % (valores[0], statistics.median(valores), valores[-1]))
