# Aula 4 de 17 · 0.3 Funções que mudam a criatura
# Guia da aula: 0.3-funcoes.md

# --- Exemplo pronto: leia e rode ---
# def cria uma função: um bloco com nome,
# que você usa quantas vezes quiser.
def desenhar(c):
    mapa = [
        ["~", "~", "~", "~", "~", "~", "~"],
        ["~", ".", ".", ".", ".", ".", "~"],
        ["~", "~", "~", "~", "~", "~", "~"],
    ]
    mapa[c["y"]][c["x"]] = "o"
    for linha in mapa:
        texto = ""
        for tile in linha:
            texto = texto + tile + " "
        print(texto)
    print("~ água  . terra  o criatura")
    print("fome:", c["hunger"], "com fome?", com_fome(c))


def com_fome(c):
    return c["hunger"] >= 5   # devolve True ou False


def andar_direita(c):
    c["x"] = c["x"] + 1


c = {"x": 1, "y": 1, "hunger": 3}
desenhar(c)
andar_direita(c)
desenhar(c)

# --- ✏️ MUDAR ---
# Quando o tempo passa, a fome sobe 1.
# Troque o pass pela linha que faz isso.


def passar_tempo(c):
    pass


passar_tempo(c)
assert c["hunger"] == 4, (
    "Fome " + str(c["hunger"]) + ", não 4. Faltou somar 1?")

# --- ✏️ MONTAR ---
# Escreva comer(c): a fome cai 3, mas nunca
# fica menor que 0. Use um if.


def comer(c):
    pass


c["hunger"] = 5
comer(c)
assert c["hunger"] == 2, (
    "Após comer: " + str(c["hunger"]) + ", não 2. Tirou 3?")
c["hunger"] = 1
comer(c)
assert c["hunger"] == 0, (
    "Ficou " + str(c["hunger"]) + ": fome não é negativa.")

desenhar(c)
print("✅ Aula concluída!")
