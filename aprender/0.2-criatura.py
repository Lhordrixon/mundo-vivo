# Aula 3 de 25 · 0.2 A criatura é um dicionário
# Guia da aula: 0.2-criatura.md

# --- Exemplo pronto: leia e rode ---
# 1. Preparar: um dicionário guarda pares
# nome: valor. Os nomes são os que o jogo usa.
c = {
    "x": 1,         # coluna
    "y": 2,         # linha
    "hunger": 3,    # fome, de 0 a 10
    "health": 10,   # vida, de 0 a 10
}
# 2. Mostrar
print("Fome da criatura:", c["hunger"])
print("Vida da criatura:", c["health"])

# --- ✏️ MUDAR ---
# ✏️ Sua vez: a criatura anda 2 tiles para a
# direita. Troque o ... por uma linha: x soma 2.
...

# Conferir
assert c["x"] == 3, (
    "x está " + str(c["x"]) + ", não 3. Andar soma 2 no "
    "x. Olhe c[\"x\"]. Tente c[\"x\"] = c[\"x\"] + ...")

# --- ✏️ MONTAR ---
# ✏️ Sua vez: crie "outra": coluna 5, linha 1,
# fome 8 e vida 10, com as mesmas chaves de c.
outra = None

# Conferir
assert outra is not None, (
    "outra ainda é None. Ela é um dicionário igual ao c. "
    "Olhe o c lá em cima e tente de novo.")
assert outra is not c, (
    "outra = c não cria outra criatura. Escreva {...}.")
for chave in ["x", "y", "hunger", "health"]:
    assert chave in outra, (
        "Faltou a chave \"" + chave + "\" em outra.")
assert outra["x"] == 5 and outra["y"] == 1, (
    "outra deveria estar na coluna 5, linha 1.")
assert outra["hunger"] == 8 and outra["health"] == 10, (
    "outra tem fome 8 e vida 10.")

# --- Final: as duas criaturas no mapa ---
# 1. Preparar
mapa = [
    ["~", "~", "~", "~", "~", "~", "~"],
    ["~", ".", ".", ".", ".", ".", "~"],
    ["~", ".", ".", ".", ".", ".", "~"],
    ["~", "~", "~", "~", "~", "~", "~"],
]
mapa[c["y"]][c["x"]] = "o"
mapa[outra["y"]][outra["x"]] = "o"
# 2. Repetir
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
# 3. Mostrar
print("~ água  . terra  o criatura")
print("✅ Aula concluída!")
