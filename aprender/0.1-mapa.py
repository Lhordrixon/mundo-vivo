# Aula 2 de 17 · 0.1 O mapa é uma lista de listas
# Guia da aula: 0.1-mapa.md

# --- Exemplo pronto: leia e rode ---
# Cada linha do mapa é uma lista de tiles.
# O mapa é uma lista dessas linhas.
mapa = [
    ["~", "~", "~", "~", "~", "~", "~", "~"],
    ["~", ".", ".", ".", "~", "~", "~", "~"],
    ["~", ".", "^", ".", ".", ".", "~", "~"],
    ["~", ".", ".", ".", ".", "~", "~", "~"],
    ["~", "~", "~", "~", "~", "~", "~", "~"],
]

# Desenha o mapa: uma linha da lista por vez.
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
print("~ água  . terra  ^ montanha")
print("Linha 2, coluna 2:", mapa[2][2])

# --- ✏️ MUDAR ---
# Coloque uma criatura "o" na linha 2,
# coluna 4. Escreva a linha logo abaixo:


assert mapa[2][4] == "o", (
    "Na linha 2, coluna 4 ainda está \"" + mapa[2][4]
    + "\". Use  mapa[linha][coluna] = \"o\"")

# --- ✏️ MONTAR ---
# Conte quantos "~" (água) existem no mapa.
# Dica: um for dentro de outro, e um if.
agua = 0


assert agua == 28, (
    "Contei " + str(agua) + " tiles de água, mas são 28. "
    "Para cada \"~\", some 1 em agua.")

# --- Final: o mesmo desenho de cima ---
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
print("~ água  . terra  ^ montanha  o criatura")
print("Água:", agua, "tiles")
print("✅ Aula concluída!")
