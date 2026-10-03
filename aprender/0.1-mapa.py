# Aula 2 de 17 · 0.1 O mapa é uma lista de listas
# Guia da aula: 0.1-mapa.md

# --- Exemplo pronto: leia e rode ---
# 1. Preparar: cada linha do mapa é uma lista de
# tiles. O mapa é uma lista dessas linhas.
mapa = [
    ["~", "~", "~", "~", "~", "~", "~", "~"],
    ["~", ".", ".", ".", "~", "~", "~", "~"],
    ["~", ".", "^", ".", ".", ".", "~", "~"],
    ["~", ".", ".", ".", ".", "~", "~", "~"],
    ["~", "~", "~", "~", "~", "~", "~", "~"],
]

# 2. Repetir: desenha uma linha da lista por vez.
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
# 3. Mostrar
print("~ água  . terra  ^ montanha")
print("Linha 2, coluna 2:", mapa[2][2])

# --- ✏️ MUDAR ---
# ✏️ Sua vez: ponha uma criatura "o" na linha 2,
# coluna 4. Troque o ... por uma linha só.
...

# Conferir
assert mapa[2][4] == "o", (
    "Na linha 2, coluna 4 está \"" + mapa[2][4] + "\". "
    "Conte do 0, na ordem mapa[linha][coluna].")

# --- ✏️ MONTAR ---
# ✏️ Sua vez: conte os "~" (água) e guarde o total
# em agua. Use um for dentro de outro e um if.
# Escreva logo abaixo do agua = 0, sem apagá-lo.
agua = 0

# Conferir
assert agua == 28, (
    "Contei " + str(agua) + ", mas são 28. Cada \"~\" soma "
    "1 em agua. Olhe o if dentro do for. Tente de novo.")

# --- Final: o mesmo desenho de cima ---
# 1. Repetir
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
# 2. Mostrar
print("~ água  . terra  ^ montanha  o criatura")
print("Água:", agua, "tiles")
print("✅ Aula concluída!")
