# Aula 1 de 25 · 0.0 Preparar celular e PC
# Guia da aula: 0.0-preparar.md

# --- Exemplo pronto: só rode ---
# 1. Mostrar: cada print escreve uma linha.
print("Olá! Eu sou o Mundo Vivo.")
print("~ ~ ~ ~ ~")
print("~ . o . ~")
print("~ ~ ~ ~ ~")
print("~ água   . terra   o criatura")

# --- ✏️ MUDAR ---
# ✏️ Sua vez: troque o None (quer dizer "nada
# ainda") por uma letra entre aspas, como "B".
minha_letra = None

# Conferir
assert minha_letra is not None, (
    "Ainda é None: falta a sua letra. Olhe a linha "
    "minha_letra. Tente \"B\", com aspas.")
print("Sua letra:", minha_letra)

# --- ✏️ MONTAR ---
# ✏️ Sua vez: monte a linha do meio do mapa, com
# a sua letra no lugar do "o". Junte textos com +.
linha_do_meio = None

# Conferir
assert linha_do_meio == "~ . " + minha_letra + " . ~", (
    "Ficou " + str(linha_do_meio) + ". O certo é ~ . "
    + minha_letra + " . ~, com espaços. Junte com +.")

# --- Final: o mapa com você no meio ---
# 1. Mostrar
print("~ ~ ~ ~ ~")
print(linha_do_meio)
print("~ ~ ~ ~ ~")
print("✅ Aula concluída!")
