# Aula 1 de 17 · 0.0 Preparar celular e PC
# Guia da aula: 0.0-preparar.md

# --- Exemplo pronto: só rode ---
print("Olá! Eu sou o Mundo Vivo.")
print("~ ~ ~ ~ ~")
print("~ . o . ~")
print("~ ~ ~ ~ ~")
print("~ água   . terra   o criatura")

# --- ✏️ MUDAR ---
# Escolha uma letra para ser você no mapa.
# Troque o None pela letra, entre aspas.
# Exemplo: minha_letra = "B"
minha_letra = None

assert minha_letra is not None, (
    "Troque o None da seção MUDAR por uma letra entre "
    "aspas, como \"B\".")
print("Sua letra:", minha_letra)

# --- ✏️ MONTAR ---
# Monte a linha do meio do mapa com a sua
# letra no lugar do "o". Junte textos com +.
linha_do_meio = None

assert linha_do_meio == "~ . " + minha_letra + " . ~", (
    "A linha do meio deveria ser  ~ . " + minha_letra + " "
    ". ~  (com os espaços). Junte os pedaços com +.")

# --- Final: o mapa com você no meio ---
print("~ ~ ~ ~ ~")
print(linha_do_meio)
print("~ ~ ~ ~ ~")
print("✅ Aula concluída!")
