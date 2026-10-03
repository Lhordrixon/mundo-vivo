# Aula 3 de 17 · 0.2 A criatura é um dicionário
# Guia da aula: 0.2-criatura.md

# --- Exemplo pronto: leia e rode ---
# Um dicionário guarda pares  nome: valor.
# Os nomes são os mesmos que o jogo usa.
c = {
    "x": 1,         # coluna
    "y": 2,         # linha
    "hunger": 3,    # fome, de 0 a 10
    "health": 10,   # vida, de 0 a 10
}
print("Fome da criatura:", c["hunger"])
print("Vida da criatura:", c["health"])

# --- ✏️ MUDAR ---
# A criatura anda 2 tiles para a direita:
# o x aumenta 2. Escreva a linha abaixo:


assert c["x"] == 3, (
    "x deveria ser 3, mas está " + str(c["x"])
    + ". Some 2 em c[\"x\"].")

# --- ✏️ MONTAR ---
# Crie a criatura "outra": coluna 5, linha 1,
# fome 8 e vida 10. Use as mesmas chaves de c.
outra = None

assert outra is not None, (
    "Troque o None da seção MONTAR por um "
    "dicionário, igual ao c lá em cima.")
for chave in ["x", "y", "hunger", "health"]:
    assert chave in outra, (
        "Faltou a chave \"" + chave + "\" em outra.")
assert outra["x"] == 5 and outra["y"] == 1, (
    "outra deveria estar na coluna 5, linha 1.")
assert outra["hunger"] == 8, "A fome de outra é 8."

# --- Final: as duas criaturas no mapa ---
mapa = [
    ["~", "~", "~", "~", "~", "~", "~"],
    ["~", ".", ".", ".", ".", ".", "~"],
    ["~", ".", ".", ".", ".", ".", "~"],
    ["~", "~", "~", "~", "~", "~", "~"],
]
mapa[c["y"]][c["x"]] = "o"
mapa[outra["y"]][outra["x"]] = "o"
for linha in mapa:
    texto = ""
    for tile in linha:
        texto = texto + tile + " "
    print(texto)
print("~ água  . terra  o criatura")
print("✅ Aula concluída!")
